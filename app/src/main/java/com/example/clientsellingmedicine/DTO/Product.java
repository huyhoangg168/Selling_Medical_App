package com.example.clientsellingmedicine.DTO;

import java.io.Serializable;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Product implements Serializable {
    private Integer id;
    private String name;
    private String description;
    private Integer discountPercent;
    private Integer price;
    private Integer quantity;
    private String image;
    private Integer status;
    private Unit unit;
    private Category category;

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        Product otherProduct = (Product) obj;
        if (id == null) {
            return otherProduct.id == null;
        }

        return id.equals(otherProduct.id);
    }
}
