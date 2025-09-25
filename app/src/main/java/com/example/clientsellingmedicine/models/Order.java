package com.example.clientsellingmedicine.models;

import java.io.Serializable;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order  implements Serializable {
    private Integer id;
    private String code;
    private Integer userId;
    private Integer redeemedCouponId;
    private String paymentMethod;
    private Integer totalCouponDiscount;
    private Integer totalProductDiscount;
    private Date orderTime;
    private String note;
    private Integer point;
    private Integer total;
    private Integer status;
    private String userAddress;
}
