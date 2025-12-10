package com.example.clientsellingmedicine.DTO;

public class ProductRequest {
    private String name;
    private String description;
    private Integer discountPercent;
    private Integer price;
    private Integer quantity;
    private String image;
    private Integer id_category;
    private Integer id_unit;
    private Integer status;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getDiscountPercent() { return discountPercent; }
    public void setDiscountPercent(Integer discountPercent) { this.discountPercent = discountPercent; }

    public Integer getPrice() { return price; }
    public void setPrice(Integer price) { this.price = price; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public Integer getId_category() { return id_category; }
    public void setId_category(Integer id_category) { this.id_category = id_category; }

    public Integer getId_unit() { return id_unit; }
    public void setId_unit(Integer id_unit) { this.id_unit = id_unit; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}

