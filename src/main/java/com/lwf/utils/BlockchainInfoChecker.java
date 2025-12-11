package com.lwf.utils;

import com.lwf.BackendApplication;
import com.lwf.service.ReviewCoreService;
import org.fisco.bcos.sdk.transaction.model.dto.CallResponse;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.math.BigInteger;

public class BlockchainInfoChecker {
    
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(BackendApplication.class, args);
        ReviewCoreService reviewCoreService = context.getBean(ReviewCoreService.class);
        
        try {
            // 获取区块链上的总评论数
            CallResponse response = reviewCoreService.totalReviews();
            BigInteger totalReviews = (BigInteger) response.getReturnObject().get(0);
            System.out.println("区块链上的总评论数: " + totalReviews);
            
            // 检查ID 1-12是否存在
            for (int i = 1; i <= 12; i++) {
                try {
                    com.lwf.model.bo.ReviewCoreGetReviewInputBO dto = new com.lwf.model.bo.ReviewCoreGetReviewInputBO();
                    dto.setReviewId(BigInteger.valueOf(i));
                    CallResponse reviewResponse = reviewCoreService.getReview(dto);
                    
                    if (reviewResponse.getReturnObject() != null && !reviewResponse.getReturnObject().isEmpty()) {
                        System.out.println("评论ID " + i + " 存在");
                    } else {
                        System.out.println("评论ID " + i + " 不存在");
                    }
                } catch (Exception e) {
                    System.out.println("评论ID " + i + " 查询失败: " + e.getMessage());
                }
            }
            
        } catch (Exception e) {
            System.err.println("查询区块链信息失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            context.close();
        }
    }
}