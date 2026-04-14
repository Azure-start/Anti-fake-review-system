package com.lwf.controller;

import com.lwf.entity.Reviews;
import com.lwf.entity.dto.ReviewDTO;
import com.lwf.model.bo.ReviewCoreGetReviewInputBO;
import com.lwf.service.IReviewsService;
import com.lwf.service.ReviewCoreService;
import com.lwf.utils.JwtUtil;
import com.lwf.utils.Result;
import org.fisco.bcos.sdk.transaction.model.dto.CallResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
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

    @Autowired
    private ReviewCoreService reviewCoreService;

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

    /**
     * 查询评论的区块链详情
     * 
     * @param reviewId 评论ID
     * @return 返回区块链上的评论数据
     */
    @GetMapping("/blockchain/{reviewId}")
    public Result<Map<String, Object>> getBlockchainReview(@PathVariable Long reviewId) {
        try {
            // 先检查数据库中的评论是否存在
            Reviews review = reviewsService.getById(reviewId);
            if (review == null) {
                return Result.error("评论不存在");
            }

            // 检查是否已上链
            if (review.getTxHash() == null || review.getTxHash().isEmpty()) {
                Map<String, Object> result = new HashMap<>();
                result.put("reviewId", reviewId);
                result.put("blockchainStatus", "未上链");
                result.put("message", "该评论尚未上传到区块链");
                return Result.success(result);
            }

            // 检查是否有区块链评论ID
            if (review.getBlockchainReviewId() == null) {
                Map<String, Object> result = new HashMap<>();
                result.put("reviewId", reviewId);
                result.put("blockchainStatus", "未上链");
                result.put("message", "该评论尚未获取区块链ID");
                return Result.success(result);
            }

            // 从区块链查询评论数据
            ReviewCoreGetReviewInputBO dto = new ReviewCoreGetReviewInputBO();
            dto.setReviewId(BigInteger.valueOf(review.getBlockchainReviewId()));

            System.out.println("查询区块链评论，reviewId: " + review.getBlockchainReviewId());
            CallResponse blockchainResponse;
            try {
                blockchainResponse = reviewCoreService.getReview(dto);
                System.out.println("区块链响应: " + blockchainResponse);

                // 检查区块链响应状态
                if (blockchainResponse.getReturnObject() == null) {
                    System.err.println("区块链返回数据为null");
                    return Result.error("区块链返回数据为空，可能评论ID不存在于区块链上");
                }

            } catch (Exception blockchainError) {
                System.err.println("区块链查询失败，详细错误: ");
                blockchainError.printStackTrace();

                // 构建详细的错误消息，避免undefined
                String errorMessage = blockchainError.getMessage();
                if (errorMessage == null || errorMessage.trim().isEmpty()) {
                    errorMessage = blockchainError.getClass().getSimpleName() + ": 无详细错误信息";
                }

                return Result.error("区块链查询失败: " + errorMessage +
                        " (区块链ID: " + review.getBlockchainReviewId() + ")");
            }

            List<Object> returnData = blockchainResponse.getReturnObject();
            System.out.println("区块链返回数据: " + returnData);

            // 检查返回数据的有效性
            if (returnData == null || returnData.isEmpty()) {
                System.err.println("区块链返回数据为空或无效");
                return Result.error("区块链返回数据为空，可能评论ID不存在于区块链上 (ID: " + review.getBlockchainReviewId() + ")");
            }

            // 打印返回数据的详细信息
            System.out.println("返回数据类型: " + returnData.getClass().getName());
            System.out.println("返回数据大小: " + returnData.size());
            for (int i = 0; i < returnData.size(); i++) {
                Object item = returnData.get(i);
                System.out.println("数据项[" + i + "]: " + item + " (类型: "
                        + (item != null ? item.getClass().getName() : "null") + ")");
            }

            Map<String, Object> result = new HashMap<>();
            result.put("reviewId", reviewId);
            result.put("blockchainStatus", "已上链");
            result.put("txHash", review.getTxHash());
            result.put("blockchainData", returnData);
            result.put("productId", review.getProductId());
            result.put("content", review.getContent());
            result.put("rating", review.getRating());
            result.put("userAddress", review.getUserAddress());
            result.put("createdAt", review.getCreatedAt());

            System.out.println("查询成功，返回结果给前端");
            return Result.success(result);

        } catch (Exception e) {
            System.err.println("查询区块链评论失败，详细错误: ");
            e.printStackTrace();

            // 构建详细的错误消息，避免undefined
            String errorMessage = e.getMessage();
            if (errorMessage == null || errorMessage.trim().isEmpty()) {
                errorMessage = e.getClass().getSimpleName() + ": 无详细错误信息";
            }

            return Result.error("查询区块链评论失败：" + errorMessage);
        }
    }

    /**
     * 调试接口：检查指定评论的区块链状态
     * 
     * @param reviewId 评论ID
     * @return 返回评论的详细状态信息
     */
    @GetMapping("/debug/blockchain-status/{reviewId}")
    public Result<Map<String, Object>> debugBlockchainStatus(@PathVariable Long reviewId) {
        try {
            System.out.println("=== 调试区块链状态，评论ID: " + reviewId + " ===");

            // 检查评论是否存在
            Reviews review = reviewsService.getById(reviewId);
            if (review == null) {
                System.err.println("评论不存在: " + reviewId);
                return Result.error("评论不存在");
            }

            System.out.println("评论基本信息:");
            System.out.println("- ID: " + review.getId());
            System.out.println("- 内容: " + review.getContent());
            System.out.println("- 交易哈希: " + review.getTxHash());
            System.out.println("- 区块链评论ID: " + review.getBlockchainReviewId());
            System.out.println("- 创建时间: " + review.getCreatedAt());

            Map<String, Object> debugInfo = new HashMap<>();
            debugInfo.put("reviewExists", true);
            debugInfo.put("txHash", review.getTxHash());
            debugInfo.put("blockchainReviewId", review.getBlockchainReviewId());
            debugInfo.put("hasBlockchainId", review.getBlockchainReviewId() != null);

            // 如果有区块链ID，尝试查询区块链
            if (review.getBlockchainReviewId() != null) {
                System.out.println("尝试查询区块链，ID: " + review.getBlockchainReviewId());
                try {
                    ReviewCoreGetReviewInputBO dto = new ReviewCoreGetReviewInputBO();
                    dto.setReviewId(BigInteger.valueOf(review.getBlockchainReviewId()));

                    CallResponse blockchainResponse = reviewCoreService.getReview(dto);
                    System.out.println("区块链查询成功，响应: " + blockchainResponse);

                    debugInfo.put("blockchainQuerySuccess", true);
                    debugInfo.put("blockchainResponse", blockchainResponse.getReturnObject());

                } catch (Exception blockchainError) {
                    System.err.println("区块链查询失败: " + blockchainError.getMessage());
                    blockchainError.printStackTrace();

                    debugInfo.put("blockchainQuerySuccess", false);
                    debugInfo.put("blockchainError", blockchainError.getMessage());
                    debugInfo.put("blockchainErrorType", blockchainError.getClass().getSimpleName());
                }
            } else {
                System.out.println("评论没有区块链ID，跳过区块链查询");
                debugInfo.put("blockchainQuerySkipped", "没有区块链ID");
            }

            System.out.println("=== 调试完成 ===");
            return Result.success(debugInfo);

        } catch (Exception e) {
            System.err.println("调试接口发生错误: " + e.getMessage());
            e.printStackTrace();
            return Result.error("调试接口错误: " + e.getMessage());
        }
    }

    /**
     * 获取所有已上链的评论列表
     * 
     * @param page     页码
     * @param pageSize 每页数量
     * @return 返回已上链的评论列表
     */
    @GetMapping("/blockchain-list")
    public Result<Map<String, Object>> getBlockchainReviews(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            Map<String, Object> result = reviewsService.getBlockchainReviews(page, pageSize);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("获取已上链评论失败：" + e.getMessage());
        }
    }

    /**
     * 修复已有交易哈希但没有区块链评论ID的数据
     * 
     * @return 返回修复结果
     */
    @PostMapping("/fix-blockchain-ids")
    public Result<Map<String, Object>> fixBlockchainReviewIds() {
        try {
            Map<String, Object> result = reviewsService.fixBlockchainReviewIds();
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("修复区块链ID失败：" + e.getMessage());
        }
    }
}