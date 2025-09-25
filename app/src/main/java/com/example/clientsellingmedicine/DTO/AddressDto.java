package com.example.clientsellingmedicine.DTO;

import com.example.clientsellingmedicine.models.User;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddressDto implements Serializable {
    private Integer id;
    private Integer id_user;
    private String user_name;
    private String phone;
    private String ward;
    private String district;
    private String province;
    private String type;
    private Boolean is_default;
    private String specific_address;
    private Integer status;
}