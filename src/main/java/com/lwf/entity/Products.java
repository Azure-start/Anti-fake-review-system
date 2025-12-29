package com.lwf.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("products")
public class Products {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("merchant_id")
    private Long merchantId;          // 商家ID

    @TableField("merchant_address")
    private String merchantAddress;   // 商家地址

    @TableField("name")
    private String name;              // 商品名称

    @TableField("description")
    private String description;       // 商品描述

    @TableField("price")
    private BigDecimal price;         // 价格

    @TableField("image")
    private String image;             // 主图

    @TableField("images")
    private String images;            // 商品图集(JSON)

    @TableField("specs")
    private String specs;             // 商品规格(JSON)

    @TableField("rating")
    private BigDecimal rating;        // 评分

    @TableField("sales")
    private Integer sales;            // 销量

    @TableField("stock")
    private Integer stock;            // 库存

    @TableField("status")
    private String status;            // 状态: pending, approved, rejected, onSale, offShelf

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private Date createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private Date updatedAt;

    // 添加商家信息字段（不映射到数据库）
    @TableField(exist = false)
    private String shopName;

}