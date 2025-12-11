package com.lwf.service;

import com.lwf.entity.Reviews;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lwf.entity.dto.ReviewDTO;
import java.util.Map;

public interface IReviewsService extends IService<Reviews> {
    Map<String, Object> submitReview(ReviewDTO reviewDTO);

    Map<String, Object> getProductReviews(Long productId, Integer page, Integer pageSize);

    Map<String, Object> getUserReviews(String userAddress, Integer page, Integer pageSize);

    boolean hasUserReviewed(Long userId, Long productId);

    Double getAverageRating(Long productId);

    Map<String, Object> voteReview(Long reviewId, String userAddress, boolean isHelpful);

    Map<String, Object> uploadReviewToBlockchain(Long reviewId);

    Map<String, Object> uploadAllUnchainedReviews();

    Map<String, Object> getBlockchainReviews(Integer page, Integer pageSize);

    Map<String, Object> fixBlockchainReviewIds();
}