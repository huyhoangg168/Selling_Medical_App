package com.example.clientsellingmedicine.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Coupon {
    private Integer id;
    private String description;
    private Integer point;
    private Integer discountPercent;
    private Integer usageDays;
    private String image;
    private Integer status;
}
