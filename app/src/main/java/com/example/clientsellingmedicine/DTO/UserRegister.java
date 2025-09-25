package com.example.clientsellingmedicine.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRegister {
    private String phone;
    private String password;
    private String confirmPassword;
}
