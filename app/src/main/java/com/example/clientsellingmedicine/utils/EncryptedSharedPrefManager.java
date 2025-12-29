package com.example.clientsellingmedicine.utils;

import static com.example.clientsellingmedicine.utils.Constants.KEY_TOKEN;
import static com.example.clientsellingmedicine.utils.Constants.KEY_USER;
import static com.example.clientsellingmedicine.utils.Constants.KEY_FIREBASE_TOKEN;
import static com.example.clientsellingmedicine.utils.Constants.KEY_CART_ITEMS_CHECKED;
import static com.example.clientsellingmedicine.utils.Constants.KEY_NOTIFICATE;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import com.example.clientsellingmedicine.DTO.Token;
import com.example.clientsellingmedicine.DTO.UserDTO;
import com.google.gson.Gson;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class EncryptedSharedPrefManager {

    private static final String ENCRYPTED_PREF_NAME = "ENCRYPTED_PREF";

    private static SharedPreferences getEncryptedPrefs(Context context)
            throws GeneralSecurityException, IOException {

        MasterKey masterKey = new MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build();

        return EncryptedSharedPreferences.create(
                context,
                ENCRYPTED_PREF_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        );
    }

    /* ================= TOKEN ================= */

    public static void saveToken(Context context, Token token) {
        try {
            String json = new Gson().toJson(token);
            getEncryptedPrefs(context)
                    .edit()
                    .putString(KEY_TOKEN, json)
                    .apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Token loadToken(Context context) {
        try {
            SharedPreferences prefs = getEncryptedPrefs(context);
            String json = prefs.getString(KEY_TOKEN, null);
            if (json == null) return null;
            return new Gson().fromJson(json, Token.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // [THÊM MỚI] Xóa riêng Token
    public static void removeToken(Context context) {
        try {
            getEncryptedPrefs(context)
                    .edit()
                    .remove(KEY_TOKEN)
                    .apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /* ================= USER ================= */

    public static void saveUser(Context context, UserDTO user) {
        try {
            String json = new Gson().toJson(user);
            getEncryptedPrefs(context)
                    .edit()
                    .putString(KEY_USER, json)
                    .apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static UserDTO loadUser(Context context) {
        try {
            SharedPreferences prefs = getEncryptedPrefs(context);
            String json = prefs.getString(KEY_USER, null);
            if (json == null) return null;
            return new Gson().fromJson(json, UserDTO.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // [THÊM MỚI] Xóa riêng User
    public static void removeUser(Context context) {
        try {
            getEncryptedPrefs(context)
                    .edit()
                    .remove(KEY_USER)
                    .apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* ================= CLEAR ALL ================= */
    public static void clearAll(Context context) {
        try {
            SharedPreferences prefs = getEncryptedPrefs(context);
            prefs.edit().clear().apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* ================= FIREBASE TOKEN ================= */

    public static void saveFirebaseToken(Context context, Token token) {
        try {
            String json = new Gson().toJson(token);
            getEncryptedPrefs(context)
                    .edit()
                    .putString(KEY_FIREBASE_TOKEN, json)
                    .apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Token loadFirebaseToken(Context context) {
        try {
            SharedPreferences prefs = getEncryptedPrefs(context);
            String json = prefs.getString(KEY_FIREBASE_TOKEN, null);
            if (json == null) return null;
            return new Gson().fromJson(json, Token.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /* ================= CART ITEMS ================= */

    public static <T> void saveCartItems(Context context, List<T> cartItems) {
        try {
            String json = new Gson().toJson(cartItems);
            getEncryptedPrefs(context)
                    .edit()
                    .putString(KEY_CART_ITEMS_CHECKED, json)
                    .apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static <T> List<T> loadCartItems(Context context, Type type) {
        try {
            SharedPreferences prefs = getEncryptedPrefs(context);
            String json = prefs.getString(KEY_CART_ITEMS_CHECKED, null);
            if (json == null) return new ArrayList<>();
            return new Gson().fromJson(json, type);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public static void clearCartItems(Context context) {
        try {
            getEncryptedPrefs(context)
                    .edit()
                    .remove(KEY_CART_ITEMS_CHECKED)
                    .apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* ================= NOTIFICATIONS ================= */

    public static <T> void saveNotifications(Context context, List<T> notifications) {
        try {
            String json = new Gson().toJson(notifications);
            getEncryptedPrefs(context)
                    .edit()
                    .putString(KEY_NOTIFICATE, json)
                    .apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static <T> List<T> loadNotifications(Context context, Type type) {
        try {
            SharedPreferences prefs = getEncryptedPrefs(context);
            String json = prefs.getString(KEY_NOTIFICATE, null);
            if (json == null) return new ArrayList<>();
            return new Gson().fromJson(json, type);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}
