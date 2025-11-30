package com.lwf.controller;

import com.lwf.entity.dto.ReviewDTO;
import com.lwf.service.IReviewsService;
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
@RequestMapping("/api/reviews")  // 修改为专门的评价路径
public class ReviewsController {

    /**
     * 自动注入评价服务接口
     * 用于处理评价相关的业务逻辑
     */
    @Autowired
    private IReviewsService reviewsService;

    /**
     * 提交评价接口
     * @param reviewDTO 评价数据传输对象，包含评价信息
     * @return 返回操作结果，包含评价提交后的相关信息
     */
    @PostMapping
    public Result<Map<String, Object>> submitReview(@Validated @RequestBody ReviewDTO reviewDTO) {
        try {
            Map<String, Object> result = reviewsService.submitReview(reviewDTO);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取商品评价列表接口
     * @param productId 商品ID
     * @param 页码，默认为1
     * @param pageSize 每页大小，默认为10
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
     * @param userAddress 用户地址
     * @param page 页码，默认为1
     * @param pageSize 每页大小，默认为10
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
     * @param reviewId 评价ID
     * @param userAddress 用户地址
     * @param isHelpful 是否有帮助，true表示有帮助，false表示无帮助
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
}