package com.lwf.entity;

import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 订单表(包含交易、奖励、优惠券)
 * </p>
 *
 * @author author
 * @since 2025-11-17
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("orders")
public class Orders implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 订单号
     */
    @TableField("order_id")
    private String orderId;

    /**
     * 商品ID
     */
    @TableField("product_id")
    private Long productId;

    /**
     * 购买用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 购买用户地址
     */
    @TableField("user_address")
    private String userAddress;

    /**
     * 商家ID
     */
    @TableField("merchant_id")
    private Long merchantId;

    /**
     * 商品名称(快照)
     */
    @TableField("product_name")
    private String productName;

    /**
     * 订单金额
     */
    @TableField("amount")
    private BigDecimal amount;

    /**
     * 订单类型
     */
    @TableField("type")
    private String type;

    /**
     * 订单状态
     */
    @TableField("status")
    private String status;

    /**
     * 收货状态
     */
    @TableField("receive_status")
    private String receiveStatus;

    /**
     * 区块链交易哈希
     */
    @TableField("tx_hash")
    private String txHash;

    /**
     * 描述(奖励/优惠券用)
     */
    @TableField("description")
    private String description;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;


}
