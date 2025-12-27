package com.example.clientsellingmedicine.utils;

import static com.example.clientsellingmedicine.utils.Constants.KEY_TOKEN;
import static com.example.clientsellingmedicine.utils.Constants.KEY_USER;

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

    public static void clearAll(Context context) {
        try {
            SharedPreferences prefs = getEncryptedPrefs(context);
            prefs.edit().clear().apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
