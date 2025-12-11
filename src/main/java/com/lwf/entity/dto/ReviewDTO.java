package com.lwf.entity.dto;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

@Data
public class ReviewDTO {
    private Long productId;

    @Min(value = 1, message = "评分不能小于1")
    @Max(value = 5, message = "评分不能大于5")
    private Integer rating;

    @NotBlank(message = "评价内容不能为空")
    private String content;

    private String ipfsCid;
    private String images;
    private String userAddress; // 添加用户地址字段
    private String orderId; // 添加订单ID字段
}