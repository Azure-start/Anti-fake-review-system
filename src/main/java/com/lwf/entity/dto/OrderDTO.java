package com.lwf.entity.dto;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class OrderDTO {
    private Long productId;
    private String userAddress;
    private BigDecimal amount;
    private String productName;
    private Integer quantity = 1;
}