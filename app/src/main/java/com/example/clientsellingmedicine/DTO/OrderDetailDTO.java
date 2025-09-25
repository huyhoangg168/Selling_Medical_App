package com.example.clientsellingmedicine.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailDTO {
    private Integer discountPrice;
    private Integer productPrice;
    private Integer quantity;
    private Product product;
    //private OrderDTO orders;
}
