package com.example.clientsellingmedicine.DTO;

public class UserLogin {
    private String phone;
    private String password;
    private String captchaToken;

    public UserLogin() {
    }

    public UserLogin(String phone, String password) {
        this.phone = phone;
        this.password = password;
        this.captchaToken = null;
    }

    public UserLogin(String phone, String password, String captchaToken) {
        this.phone = phone;
        this.password = password;
        this.captchaToken = captchaToken;
    }

    public String getPhone() { return phone; }
    public String getPassword() { return password; }
}
