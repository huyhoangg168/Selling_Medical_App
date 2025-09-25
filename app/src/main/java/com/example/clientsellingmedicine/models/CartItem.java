package com.example.clientsellingmedicine.models;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {
    private int id_user;
    private int id_product;
    private int quantity;
}
