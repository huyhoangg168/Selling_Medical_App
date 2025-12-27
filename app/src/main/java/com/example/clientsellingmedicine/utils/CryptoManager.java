package com.example.clientsellingmedicine.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.nio.charset.StandardCharsets;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import javax.crypto.Cipher;

public class CryptoManager {

    private static final String PREF_FILE_NAME = "secure_app_prefs";
    private static final String KEY_ALIAS = "aes_key_data"; // Key dùng để mã hóa data
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final String RSA_ALIAS = "MyRsaKeyAlias";
    private static final String ANDROID_KEYSTORE = "AndroidKeyStore";
    private static final int TAG_LENGTH_BIT = 128;
    private static final int IV_LENGTH_BYTE = 12;

    private SharedPreferences sharedPreferences;
    private Context context;

    public CryptoManager(Context context) {
        this.context = context;
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            sharedPreferences = EncryptedSharedPreferences.create(
                    context,
                    PREF_FILE_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Bước 1: Tạo cặp khóa RSA nếu chưa có.
     * Private Key nằm cứng trong Hardware Keystore, không ai lấy ra được.
     * @return Chuỗi Public Key (Base64) để gửi lên Server.
     */
    public String generateAndGetRSAPublicKey() {
        try {
            KeyStore keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
            keyStore.load(null);

            if (!keyStore.containsAlias(RSA_ALIAS)) {
                // Tạo mới nếu chưa có
                KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(
                        KeyProperties.KEY_ALGORITHM_RSA, ANDROID_KEYSTORE);

                KeyGenParameterSpec spec = new KeyGenParameterSpec.Builder(
                        RSA_ALIAS,
                        KeyProperties.PURPOSE_DECRYPT) // Chỉ dùng để giải mã (Server mã hóa -> App giải mã)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1) // Chuẩn padding phổ biến
                        .setKeySize(2048)
                        .build();

                keyPairGenerator.initialize(spec);
                keyPairGenerator.generateKeyPair();
            }

            // Lấy Public Key ra
            PublicKey publicKey = keyStore.getCertificate(RSA_ALIAS).getPublicKey();
            return Base64.encodeToString(publicKey.getEncoded(), Base64.NO_WRAP);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Bước 3: Nhận AES Key bị mã hóa từ Server -> Dùng RSA Private Key giải mã
     * -> Lưu AES Key gốc vào SharedPreferences
     */
    public boolean decryptAndSaveServerKey(String encryptedBase64AESKey) {
        try {
            KeyStore keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
            keyStore.load(null);

            // Lấy Private Key từ Keystore
            PrivateKey privateKey = (PrivateKey) keyStore.getKey(RSA_ALIAS, null);

            // Giải mã RSA
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);

            byte[] encryptedBytes = Base64.decode(encryptedBase64AESKey, Base64.NO_WRAP);
            byte[] decryptedAesKey = cipher.doFinal(encryptedBytes);

            // Lưu AES Key (plaintext bytes) vào EncryptedSharedPreferences
            // Lưu ý: Ta convert bytes sang Base64 để lưu vào Prefs cho đồng nhất logic cũ
            String aesKeyToSave = Base64.encodeToString(decryptedAesKey, Base64.NO_WRAP);
            saveKey(aesKeyToSave);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- QUẢN LÝ KEY (TẠM THỜI ĐỂ TEST) ---
    // Sau này Backend gửi Key về thì dùng hàm saveKey
    public void checkOrGenerateKey() {
        if (sharedPreferences.getString(KEY_ALIAS, null) == null) {
            // Tự sinh key test nếu chưa có (Sau này thay bằng logic nhận từ server)
            try {
                javax.crypto.KeyGenerator keyGen = javax.crypto.KeyGenerator.getInstance("AES");
                keyGen.init(256);
                SecretKey secretKey = keyGen.generateKey();
                saveKey(Base64.encodeToString(secretKey.getEncoded(), Base64.DEFAULT));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void saveKey(String base64Key) {
        sharedPreferences.edit().putString(KEY_ALIAS, base64Key).apply();
    }

    private SecretKey getUserKey() {
        String base64Key = sharedPreferences.getString(KEY_ALIAS, null);
        if (base64Key == null) return null;
        byte[] decodedKey = Base64.decode(base64Key, Base64.DEFAULT);
        return new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
    }
    // --------------------------------------

    // 1. MÃ HÓA (Dùng khi tạo Order)
    public String encrypt(String plaintext) {
        if (plaintext == null || plaintext.isEmpty()) return plaintext;
        try {
            checkOrGenerateKey(); // Đảm bảo có key
            SecretKey secretKey = getUserKey();

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            byte[] iv = cipher.getIV();
            byte[] cipherText = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            // Ghép IV + CipherText
            byte[] combined = new byte[iv.length + cipherText.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(cipherText, 0, combined, iv.length, cipherText.length);

            return "AES_ENCRYPTED:" + Base64.encodeToString(combined, Base64.NO_WRAP);
        } catch (Exception e) {
            e.printStackTrace();
            return plaintext; // Fail thì trả về gốc
        }
    }

    // 2. GIẢI MÃ (Dùng khi xem Order)
    public String decrypt(String encryptedData) {
        if (encryptedData == null || !encryptedData.startsWith("AES_ENCRYPTED:")) {
            return encryptedData; // Không phải data mã hóa thì trả về nguyên gốc
        }

        try {
            String base64Payload = encryptedData.replace("AES_ENCRYPTED:", "");
            byte[] combined = Base64.decode(base64Payload, Base64.NO_WRAP);

            SecretKey secretKey = getUserKey();

            byte[] iv = new byte[IV_LENGTH_BYTE];
            byte[] cipherText = new byte[combined.length - IV_LENGTH_BYTE];
            System.arraycopy(combined, 0, iv, 0, IV_LENGTH_BYTE);
            System.arraycopy(combined, IV_LENGTH_BYTE, cipherText, 0, cipherText.length);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);

            return new String(cipher.doFinal(cipherText), StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
            return "[Lỗi bảo mật]"; // Báo lỗi nếu key sai hoặc data bị sửa
        }
    }
}