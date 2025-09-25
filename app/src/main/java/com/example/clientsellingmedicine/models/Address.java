package com.example.clientsellingmedicine.models;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Address  {
    private Integer id;
    private Integer id_user;
    private String user_name;
    private String phone;
    private String ward;
    private String district;
    private String province;
    private String type;
    private Boolean isDefault;
    private String specificAddress;
    private Integer status;
}