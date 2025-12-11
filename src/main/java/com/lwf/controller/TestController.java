package com.lwf.controller;

import com.lwf.model.bo.*;
import com.lwf.service.ReviewCoreService;
import org.fisco.bcos.sdk.transaction.model.dto.TransactionResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;

@RestController
@RequestMapping("/api/review")
public class TestController {

    /* WeBASE 已经帮你生成了 ReviewCoreService，直接注入即可 */
    @Autowired
    private ReviewCoreService reviewCoreService;

    /**
     * 一键提交评论
     * POST 示例（form-data）：
     * productId = iphone15
     * content = 真的很丝滑，就是有点贵
     * rating = 5
     */
    @PostMapping("/submit")
    public String submit(@RequestParam String productId,
            @RequestParam String content,
            @RequestParam Integer rating) throws Exception {

        ReviewCoreSubmitReviewInputBO dto = new ReviewCoreSubmitReviewInputBO();
        dto.setProductId(productId);
        dto.setContent(content);
        dto.setRating(BigInteger.valueOf(rating));

        TransactionResponse txResp = reviewCoreService.submitReview(dto);

        // ✅ 直接就是 BigInteger，别再套 Type
        BigInteger total = (BigInteger) reviewCoreService.totalReviews()
                .getReturnObject()
                .get(0);

        return "success, txHash = " + txResp.getTransactionReceipt().getTransactionHash()
                + ", reviewId = " + total;
    }

    /**
     * 随便查一条评论（调试用）
     */
    @GetMapping("/get")
    public Object get(@RequestParam Long reviewId) throws Exception {
        ReviewCoreGetReviewInputBO dto = new ReviewCoreGetReviewInputBO();
        dto.setReviewId(BigInteger.valueOf(reviewId));
        return reviewCoreService.getReview(dto).getReturnObject(); // 返回 Tuple8
    }
}