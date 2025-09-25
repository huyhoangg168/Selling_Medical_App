package com.example.clientsellingmedicine.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetail {
    private Integer discountPrice;
    private Integer productPrice;
    private Integer quantity;
    private Integer productId;
    private Integer orderId;
}