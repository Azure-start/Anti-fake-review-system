package com.lwf.entity.dto;

import lombok.Data;

@Data
public class ProductQueryDTO {
    private Integer page = 1;
    private Integer pageSize = 12;
    private String keyword;
    private String category;
    private String sortBy = "created_at";
    private String sortOrder = "desc";
}