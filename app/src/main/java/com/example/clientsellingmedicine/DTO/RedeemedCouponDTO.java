package com.example.clientsellingmedicine.DTO;

import java.io.Serializable;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RedeemedCouponDTO implements Serializable {
    private Integer id;
    private CouponDTO coupon;
    private String code;
    private UserDTO user;
    private Date expiryDate;
    private Integer status;
}