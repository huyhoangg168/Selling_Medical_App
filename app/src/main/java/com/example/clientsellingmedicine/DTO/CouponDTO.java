package com.example.clientsellingmedicine.DTO;

import java.io.Serializable;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CouponDTO implements Serializable {
    private Integer id;
    private String description;
    private Integer point;
    private Integer discountPercent;
    private Integer usageDays;
    private String image;
    private Integer status;
}
