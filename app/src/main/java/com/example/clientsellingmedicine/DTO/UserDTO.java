package com.example.clientsellingmedicine.DTO;


import java.io.Serializable;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO implements Serializable {
    private Integer id;
    private String role;
    private String phone;
    private String email;
    private String username;
    private String rank;
    private Integer point;
    private Date birthday;
    private Integer gender;
    private String image;
}
