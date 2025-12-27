package com.example.clientsellingmedicine.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.clientsellingmedicine.DTO.Token;
import com.example.clientsellingmedicine.DTO.UserDTO;
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class SharedPref {

    public static void saveData(Context context, List<?> objectsList, String prefsName, String key) {
        Gson gson = new Gson();
        String json = gson.toJson(objectsList);
        SharedPreferences sharedPreferences = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, json);
        editor.apply();
    }

    public static <T> List<T> loadData(Context context, String prefsName, String key, Type type) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE);
        String json = sharedPreferences.getString(key, null);
        if (json == null) {
            return new ArrayList<>();
        }
        Gson gson = new Gson();
        return gson.fromJson(json, type);
    }

    public static void clearData(Context context, String prefsName) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

    public static void removeData(Context context, String prefsName, String key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(key);
        editor.apply();
    }

    // ---  HÀM XỬ LÝ BOOLEAN CHO VÂN TAY ---

    public static void saveBoolean(Context context, String prefsName, String key, boolean value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public static boolean getBoolean(Context context, String prefsName, String key, boolean defaultValue) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(key, defaultValue);
    }

    // --- THÊM MỚI: HÀM LƯU STRING (Dùng cho Firebase Token) ---
    public static void saveString(Context context, String prefsName, String key, String value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static String getString(Context context, String prefsName, String key, String defaultValue) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE);
        return sharedPreferences.getString(key, defaultValue);
    }
}
