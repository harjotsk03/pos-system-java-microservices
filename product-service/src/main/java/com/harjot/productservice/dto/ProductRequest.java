package com.harjot.productservice.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class ProductRequest {
    private String sku;
    private String name;
    private String description;
    private BigDecimal price;
    private String category;
}
