package com.example.clientsellingmedicine.DTO;

import com.example.clientsellingmedicine.models.Order;

import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderWithDetails implements Serializable {
    private List<CartItemDTO> listCartItem;
    private List<OrderDetailDTO> listOrderItem;
    private Order order;
}
