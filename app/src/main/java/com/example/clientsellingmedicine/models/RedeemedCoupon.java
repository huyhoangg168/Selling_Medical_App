package com.example.clientsellingmedicine.models;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RedeemedCoupon {
    private Integer id;
    private Integer id_coupon;
    private Integer id_user;
    private String code;
    private Date expiryDate;
    private Integer status;
}
