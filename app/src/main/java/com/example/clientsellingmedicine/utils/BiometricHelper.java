package com.example.clientsellingmedicine.utils;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import java.util.concurrent.Executor;

public class BiometricHelper {

    // Interface để trả về kết quả cho màn hình gọi
    public interface BiometricCallback {
        void onSuccess(BiometricPrompt.AuthenticationResult result);
        void onFailure();
    }

    /**
     * Kiểm tra thiết bị có hỗ trợ vân tay/khuôn mặt không
     */
    public static boolean isBiometricAvailable(Context context) {
        BiometricManager biometricManager = BiometricManager.from(context);
        switch (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                return true;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                return false;
        }
        return false;
    }

    /**
     * Hàm gọi hiển thị hộp thoại xác thực (Dùng cho Activity)
     */
    public static void authenticate(FragmentActivity activity, BiometricCallback callback) {
        if (!isBiometricAvailable(activity)) {
            Toast.makeText(activity, "Thiết bị không hỗ trợ hoặc chưa cài đặt vân tay!", Toast.LENGTH_SHORT).show();
            return;
        }
        showPrompt(activity, null, callback);
    }

    /**
     * Hàm gọi hiển thị hộp thoại xác thực (Dùng cho Fragment)
     */
    public static void authenticate(Fragment fragment, BiometricCallback callback) {
        if (!isBiometricAvailable(fragment.getContext())) {
            Toast.makeText(fragment.getContext(), "Thiết bị không hỗ trợ hoặc chưa cài đặt vân tay!", Toast.LENGTH_SHORT).show();
            return;
        }
        showPrompt(null, fragment, callback);
    }

    private static void showPrompt(FragmentActivity activity, Fragment fragment, BiometricCallback callback) {
        Executor executor;
        Context context;

        if (activity != null) {
            executor = ContextCompat.getMainExecutor(activity);
            context = activity;
        } else {
            executor = ContextCompat.getMainExecutor(fragment.getContext());
            context = fragment.getContext();
        }

        BiometricPrompt.AuthenticationCallback authenticationCallback = new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                // Người dùng bấm nút Cancel hoặc quá số lần thử
                Toast.makeText(context, "Lỗi xác thực: " + errString, Toast.LENGTH_SHORT).show();
                callback.onFailure();
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                // Xác thực thành công
                callback.onSuccess(result);
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                // Vân tay không khớp (nhưng chưa bị khóa, cho thử lại)
                Toast.makeText(context, "Vân tay không khớp, vui lòng thử lại!", Toast.LENGTH_SHORT).show();
            }
        };

        BiometricPrompt biometricPrompt;
        if (activity != null) {
            biometricPrompt = new BiometricPrompt(activity, executor, authenticationCallback);
        } else {
            biometricPrompt = new BiometricPrompt(fragment, executor, authenticationCallback);
        }

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Xác thực vân tay")
                .setSubtitle("Chạm vào cảm biến để tiếp tục")
                .setNegativeButtonText("Hủy")
                .build();

        biometricPrompt.authenticate(promptInfo);
    }
}