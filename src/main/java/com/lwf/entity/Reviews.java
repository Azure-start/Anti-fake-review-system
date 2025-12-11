package com.lwf.entity;

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
 * 评价表
 * </p>
 *
 * @author author
 * @since 2025-11-17
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("reviews")
public class Reviews implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 商品ID
     */
    @TableField("product_id")
    private Long productId;

    /**
     * 评价用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 评价用户地址
     */
    @TableField("user_address")
    private String userAddress;

    /**
     * 评分1-5
     */
    @TableField("rating")
    private Integer rating;

    /**
     * 评价内容
     */
    @TableField("content")
    private String content;

    /**
     * IPFS内容标识
     */
    @TableField("ipfs_cid")
    private String ipfsCid;

    /**
     * 评价图片JSON数组
     */
    @TableField("images")
    private String images;

    /**
     * NFT标识
     */
    @TableField("nft_id")
    private String nftId;

    /**
     * 有用投票数
     */
    @TableField("helpful_votes")
    private Integer helpfulVotes;

    /**
     * 无用投票数
     */
    @TableField("unhelpful_votes")
    private Integer unhelpfulVotes;

    /**
     * 是否已验证
     */
    @TableField("verified")
    private Boolean verified;

    /**
     * 区块链交易哈希
     */
    @TableField("tx_hash")
    private String txHash;

    /**
     * 区块链评论ID
     */
    @TableField("blockchain_review_id")
    private Long blockchainReviewId;

    @TableField("created_at")
    private LocalDateTime createdAt;


}
