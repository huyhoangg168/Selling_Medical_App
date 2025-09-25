package com.example.clientsellingmedicine.DTO;


import java.io.Serializable;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO implements Serializable {

    private Integer id;
    private String code;
    private UserDTO user;
    private String paymentMethod;
    private Integer totalCouponDiscount;
    private Integer totalProductDiscount;
    private Integer totalDiscount;
    private Date orderTime;
    private String note;
    private Integer point;
    private Integer total;
    private Integer status;
    private String userAddress;
    private RedeemedCouponDTO redeemed_coupons;
}
