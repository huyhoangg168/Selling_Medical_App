package com.example.clientsellingmedicine.models;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Integer id;
    private String role;
    private String phone;
    private String email;
    private String username;
    private String password;
    private String rank;
    private Integer point;
    private Date birthday;
    private Integer gender;
    private String image;
    private Integer status;
}