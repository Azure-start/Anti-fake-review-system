package com.lwf.controller;

import com.lwf.entity.dto.ReviewDTO;
import com.lwf.service.IReviewsService;
import com.lwf.utils.JwtUtil;
import com.lwf.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 评价控制器类，处理与评价相关的HTTP请求
 * 提供提交评价、获取商品评价、获取用户评价、对评价进行投票等功能
 */
@RestController
@RequestMapping("/api/reviews") // 修改为专门的评价路径
public class ReviewsController {

    /**
     * 自动注入评价服务接口
     * 用于处理评价相关的业务逻辑
     */
    @Autowired
    private IReviewsService reviewsService;

    /**
     * 提交评价接口
     * 
     * @param reviewDTO     评价数据传输对象，包含评价信息
     * @param authorization JWT token from request header
     * @return 返回操作结果，包含评价提交后的相关信息
     */
    @PostMapping
    public Result<Map<String, Object>> submitReview(@Validated @RequestBody ReviewDTO reviewDTO,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        try {
            // 从JWT token中获取用户地址，如果token不存在则使用请求体中的地址（开发模式）
            String userAddress;
            if (authorization != null && !authorization.isEmpty()) {
                userAddress = JwtUtil.getAddressFromToken(authorization);
            } else if (reviewDTO.getUserAddress() != null && !reviewDTO.getUserAddress().isEmpty()) {
                // 开发模式下使用请求体中的地址
                userAddress = reviewDTO.getUserAddress();
            } else {
                return Result.error("用户地址不能为空");
            }
            reviewDTO.setUserAddress(userAddress);

            Map<String, Object> result = reviewsService.submitReview(reviewDTO);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取商品评价列表接口
     * 
     * @param productId 商品ID
     * @param 页码，默认为1
     * @param pageSize  每页大小，默认为10
     * @return 返回操作结果，包含商品评价列表及分页信息
     */
    @GetMapping("/product/{productId}")
    public Result<Map<String, Object>> getProductReviews(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            Map<String, Object> result = reviewsService.getProductReviews(productId, page, pageSize);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取用户评价列表接口
     * 
     * @param userAddress 用户地址
     * @param page        页码，默认为1
     * @param pageSize    每页大小，默认为10
     * @return 返回操作结果，包含用户评价列表及分页信息
     */
    @GetMapping("/user")
    public Result<Map<String, Object>> getUserReviews(
            @RequestParam String userAddress,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            Map<String, Object> result = reviewsService.getUserReviews(userAddress, page, pageSize);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 对评价进行投票接口
     * 
     * @param reviewId    评价ID
     * @param userAddress 用户地址
     * @param isHelpful   是否有帮助，true表示有帮助，false表示无帮助
     * @return 返回操作结果，包含投票后的相关信息
     */
    @PostMapping("/{reviewId}/vote")
    public Result<Map<String, Object>> voteReview(
            @PathVariable Long reviewId,
            @RequestParam String userAddress,
            @RequestParam Boolean isHelpful) {
        try {
            Map<String, Object> result = reviewsService.voteReview(reviewId, userAddress, isHelpful);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 将指定评价上传到区块链
     * 
     * @param reviewId 评价ID
     * @return 返回上链结果，包含交易哈希等信息
     */
    @PostMapping("/{reviewId}/upload-to-blockchain")
    public Result<Map<String, Object>> uploadReviewToBlockchain(@PathVariable Long reviewId) {
        try {
            Map<String, Object> result = reviewsService.uploadReviewToBlockchain(reviewId);
            if ((Integer) result.get("code") == 0) {
                return Result.success(result);
            } else {
                return Result.error(result.get("message").toString());
            }
        } catch (Exception e) {
            return Result.error("上链失败：" + e.getMessage());
        }
    }

    /**
     * 批量将所有未上链的评价上传到区块链
     * 
     * @return 返回批量上链结果，包含成功和失败数量
     */
    @PostMapping("/upload-all-to-blockchain")
    public Result<Map<String, Object>> uploadAllUnchainedReviews() {
        try {
            Map<String, Object> result = reviewsService.uploadAllUnchainedReviews();
            if ((Integer) result.get("code") == 0) {
                return Result.success(result);
            } else {
                return Result.error(result.get("message").toString());
            }
        } catch (Exception e) {
            return Result.error("批量上链失败：" + e.getMessage());
        }
    }
}