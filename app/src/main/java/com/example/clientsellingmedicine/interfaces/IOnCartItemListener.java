package com.example.clientsellingmedicine.interfaces;

import com.example.clientsellingmedicine.DTO.CartItemDTO;
import com.example.clientsellingmedicine.DTO.Total;
import com.example.clientsellingmedicine.models.CartItem;

public interface IOnCartItemListener {
    void setValueOfMasterCheckbox(boolean isChecked);

    void setStatusOfDeleteText(boolean isShowed);

    void getTotal(Total total);

    void updateCartItemQuantity(CartItemDTO cartItem);
}
