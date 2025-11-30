package com.lwf.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("users")
public class Users {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("address")
    private String address;           // 钱包地址

    @TableField("role")
    private String role;              // 用户角色: user, merchant, admin

    @TableField("display_name")
    private String displayName;       // 显示名称

    @TableField("shop_name")
    private String shopName;          // 店铺名称

    @TableField("shop_description")
    private String shopDescription;   // 店铺描述

    @TableField("shop_logo")
    private String shopLogo;          // 店铺logo

    @TableField("shop_status")
    private String shopStatus;        // 店铺状态: pending, approved, rejected

    @TableField("reputation_score")
    private Integer reputationScore;  // 信誉分

    @TableField("balance")
    private BigDecimal balance;       // 余额

    @TableField("total_reviews")
    private Integer totalReviews;     // 总评价数

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private Date createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private Date updatedAt;
}