package com.lwf.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lwf.entity.Products;
import com.lwf.entity.Reviews;
import com.lwf.entity.Users;
import com.lwf.entity.dto.ReviewDTO;
import com.lwf.mapper.ReviewsMapper;
import com.lwf.service.IOrdersService;
import com.lwf.model.bo.*;
import com.lwf.service.ReviewCoreService;
import com.lwf.service.ReviewNFTService;
import org.fisco.bcos.sdk.transaction.model.dto.TransactionResponse;
import com.lwf.service.IProductsService;
import com.lwf.service.IReviewsService;
import com.lwf.service.IUsersService;
import com.lwf.service.SimpleCacheService;
import com.lwf.utils.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import java.math.BigInteger;
import org.fisco.bcos.sdk.transaction.model.dto.CallResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@EnableAsync
public class ReviewsServiceImpl extends ServiceImpl<ReviewsMapper, Reviews> implements IReviewsService {

    @Autowired
    private IUsersService usersService;

    @Autowired
    private IProductsService productsService;

    @Autowired
    private SimpleCacheService cacheService;

    @Autowired
    private ReviewsMapper reviewsMapper;

    @Autowired
    private IOrdersService ordersService;

    @Autowired
    private ReviewCoreService reviewCoreService;

    @Autowired
    private ReviewNFTService reviewNFTService;

    /**
     * 提交商品评价的方法
     * 该方法处理用户提交商品评价的业务逻辑，包括验证、创建评价和更新相关数据
     *
     * @param reviewDTO 包含评价信息的DTO对象
     * @return 返回包含评价结果的Map对象，包含状态码、NFT ID、评价ID和消息
     * @throws BusinessException 当商品不存在、用户不存在或用户已评价过商品时抛出
     */
    @Override
    @Transactional // 声明事务注解，确保方法内操作的事务性
    public Map<String, Object> submitReview(ReviewDTO reviewDTO) {
        // 创建结果Map，用于返回操作结果
        Map<String, Object> result = new HashMap<>();

        // 验证商品是否存在
        Products product = productsService.getById(reviewDTO.getProductId());
        if (product == null) {
            throw new BusinessException("商品不存在");
        }

        // 查找用户（根据用户地址获取）
        QueryWrapper<Users> userQuery = new QueryWrapper<>();
        userQuery.eq("address", reviewDTO.getUserAddress()); // 从DTO中获取用户地址
        Users user = usersService.getOne(userQuery);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 移除重复评价检查，允许用户对同一商品进行多次评价
        // if (hasUserReviewed(user.getId(), reviewDTO.getProductId())) {
        // throw new BusinessException("您已经评价过该商品");
        // }

        // 创建评价对象并设置属性
        Reviews review = new Reviews();
        review.setProductId(reviewDTO.getProductId());
        review.setUserId(user.getId());
        review.setUserAddress(user.getAddress());
        review.setRating(reviewDTO.getRating());
        review.setContent(reviewDTO.getContent());
        review.setIpfsCid(reviewDTO.getIpfsCid());
        review.setImages(reviewDTO.getImages());
        // 生成唯一的NFT ID，包含商品ID和时间戳
        review.setNftId("NFT_" + reviewDTO.getProductId() + "_"  + System.currentTimeMillis());
        review.setHelpfulVotes(0); // 初始化有用投票数为0
        review.setUnhelpfulVotes(0); // 初始化无用投票数为0
        review.setVerified(user.getReputationScore() >= 50); // 高信誉用户自动验证

        boolean saved = this.save(review);

        if (saved) {
            // 更新商品评分
            updateProductRating(reviewDTO.getProductId());
            // 更新用户评价数
            usersService.incrementReviewCount(user.getId());

            // 更新订单评价状态
            if (reviewDTO.getOrderId() != null) {
                ordersService.updateReviewStatus(reviewDTO.getOrderId(), 1);
            }

            // 🚀 异步上传到区块链（不阻塞用户操作）
            asyncUploadToBlockchain(review.getId());

            result.put("code", 0);
            result.put("nftId", review.getNftId());
            result.put("reviewId", review.getId());
            result.put("message", "评价提交成功");
        } else {
            throw new BusinessException("评价提交失败");
        }

        return result;
    }

    @Override
    public Map<String, Object> getProductReviews(Long productId, Integer page, Integer pageSize) {
        Map<String, Object> result = new HashMap<>();

        Page<Reviews> pageInfo = new Page<>(page, pageSize);
        QueryWrapper<Reviews> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("product_id", productId)
                .orderByDesc("created_at");

        Page<Reviews> reviewPage = this.page(pageInfo, queryWrapper);

        result.put("list", reviewPage.getRecords());
        result.put("total", reviewPage.getTotal());
        result.put("page", page);
        result.put("pageSize", pageSize);
        result.put("code", 0);

        return result;
    }

    @Override
    public Map<String, Object> getUserReviews(String userAddress, Integer page, Integer pageSize) {
        Map<String, Object> result = new HashMap<>();

        Page<Reviews> pageInfo = new Page<>(page, pageSize);
        QueryWrapper<Reviews> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_address", userAddress)
                .orderByDesc("created_at");

        Page<Reviews> reviewPage = this.page(pageInfo, queryWrapper);

        // 获取评价列表并补充商品信息
        List<Reviews> reviews = reviewPage.getRecords();
        List<Map<String, Object>> reviewListWithProduct = new ArrayList<>();

        for (Reviews review : reviews) {
            Map<String, Object> reviewData = new HashMap<>();
            reviewData.put("id", review.getId());
            reviewData.put("productId", review.getProductId());
            reviewData.put("userId", review.getUserId());
            reviewData.put("userAddress", review.getUserAddress());
            reviewData.put("rating", review.getRating());
            reviewData.put("content", review.getContent());
            reviewData.put("ipfsCid", review.getIpfsCid());
            reviewData.put("images", review.getImages());
            reviewData.put("nftId", review.getNftId());
            reviewData.put("helpfulVotes", review.getHelpfulVotes());
            reviewData.put("unhelpfulVotes", review.getUnhelpfulVotes());
            reviewData.put("verified", review.getVerified());
            reviewData.put("txHash", review.getTxHash());
            reviewData.put("blockchainReviewId", review.getBlockchainReviewId());
            reviewData.put("createdAt", review.getCreatedAt());

            // 获取商品信息
            try {
                Products product = productsService.getById(review.getProductId());
                if (product != null) {
                    reviewData.put("productName", product.getName());
                    reviewData.put("productAddress", product.getMerchantAddress());
                } else {
                    reviewData.put("productName", "未知商品");
                    reviewData.put("productAddress", "未知地址");
                }
            } catch (Exception e) {
                reviewData.put("productName", "未知商品");
                reviewData.put("productAddress", "未知地址");
            }

            reviewListWithProduct.add(reviewData);
        }

        result.put("list", reviewListWithProduct);
        result.put("total", reviewPage.getTotal());
        result.put("page", page);
        result.put("pageSize", pageSize);
        result.put("code", 0);

        return result;
    }

    @Override
    public boolean hasUserReviewed(Long userId, Long productId) {
        QueryWrapper<Reviews> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                .eq("product_id", productId);
        return this.count(queryWrapper) > 0;
    }

    @Override
    public Double getAverageRating(Long productId) {
        QueryWrapper<Reviews> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("product_id", productId)
                .select("AVG(rating) as avg_rating");

        Map<String, Object> map = this.getMap(queryWrapper);
        if (map != null && map.get("avg_rating") != null) {
            return ((Number) map.get("avg_rating")).doubleValue();
        }
        return 0.0;
    }

    @Override
    /**
     * 对评价进行投票的方法
     * 
     * @param reviewId    评价ID
     * @param userAddress 用户地址
     * @param isHelpful   是否为有用投票
     * @return 包含投票结果的Map对象，包含状态码、有用票数、无用票数和消息
     */
    public Map<String, Object> voteReview(Long reviewId, String userAddress, boolean isHelpful) {
        // 创建结果Map对象
        Map<String, Object> result = new HashMap<>();

        // 根据ID获取评价信息
        Reviews review = this.getById(reviewId);
        if (review == null) {
            throw new BusinessException("评价不存在");
        }

        // 检查是否已经投过票（使用本地缓存）
        // 构建投票键，格式为"vote:评价ID:用户地址"
        String voteKey = "vote:" + reviewId + ":" + userAddress;
        if (cacheService.exists(voteKey)) {
            throw new BusinessException("您已经投过票了");
        }

        // 更新投票数
        if (isHelpful) {
            review.setHelpfulVotes(review.getHelpfulVotes() + 1);
        } else {
            review.setUnhelpfulVotes(review.getUnhelpfulVotes() + 1);
        }

        this.updateById(review);

        // 记录投票（30天有效期）
        cacheService.set(voteKey, "1", 30 * 24 * 60 * 60);

        result.put("code", 0);
        result.put("helpfulVotes", review.getHelpfulVotes());
        result.put("unhelpfulVotes", review.getUnhelpfulVotes());
        result.put("message", "投票成功");

        return result;
    }

    /**
     * 更新商品评分
     */
    private void updateProductRating(Long productId) {
        Double avgRating = getAverageRating(productId);
        if (avgRating > 0) {
            Products product = productsService.getById(productId);
            if (product != null) {
                product.setRating(BigDecimal.valueOf(avgRating).setScale(2, RoundingMode.HALF_UP));
                productsService.updateById(product);
            }
        }
    }

    /**
     * 将评论上传到区块链
     * 
     * @param reviewId 评论ID
     * @return 包含上链结果的Map
     */
    @Override
    @Transactional
    public Map<String, Object> uploadReviewToBlockchain(Long reviewId) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 获取评论信息
            Reviews review = this.getById(reviewId);
            if (review == null) {
                throw new BusinessException("评论不存在");
            }

            // 检查是否已上链（需要同时检查txHash和blockchainReviewId）
            if (review.getTxHash() != null && !review.getTxHash().isEmpty() && review.getBlockchainReviewId() != null) {
                result.put("code", 1);
                result.put("message", "评论已上链，交易哈希：" + review.getTxHash());
                result.put("txHash", review.getTxHash());
                return result;
            }

            // 准备区块链提交数据
            ReviewCoreSubmitReviewInputBO dto = new ReviewCoreSubmitReviewInputBO();
            dto.setProductId(String.valueOf(review.getProductId()));
            dto.setContent(review.getContent());
            dto.setRating(BigInteger.valueOf(review.getRating()));

            // 提交到区块链
            TransactionResponse txResp = reviewCoreService.submitReview(dto);
            String txHash = txResp.getTransactionReceipt().getTransactionHash();

            // 获取区块链返回的评论ID（通过总评论数获取）
            CallResponse totalResponse = reviewCoreService.totalReviews();
            BigInteger blockchainReviewId = (BigInteger) totalResponse.getReturnObject().get(0);
            System.out.println("获取到的区块链评论ID: " + blockchainReviewId);

            // 获取NFT ID（tokenId与blockchainReviewId相同）
            String nftId = "NFT_" + blockchainReviewId.toString();
            System.out.println("获取到的NFT ID: " + nftId);
         
            // 更新数据库中的交易哈希、区块链评论ID和NFT ID
            review.setTxHash(txHash);
            review.setBlockchainReviewId(blockchainReviewId.longValue());
            review.setNftId(nftId);
            this.updateById(review);

            result.put("code", 0);
            result.put("message", "评论上链成功");
            result.put("txHash", txHash);
            result.put("reviewId", reviewId);

        } catch (Exception e) {
            result.put("code", -1);
            result.put("message", "评论上链失败：" + e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

    /**
     * 批量将未上链的评论上传到区块链
     * 
     * @return 包含批量上链结果的Map
     */
    @Override
    @Transactional
    public Map<String, Object> uploadAllUnchainedReviews() {
        Map<String, Object> result = new HashMap<>();

        try {
            // 查询所有未上链的评论
            QueryWrapper<Reviews> queryWrapper = new QueryWrapper<>();
            queryWrapper.isNull("tx_hash").or().eq("tx_hash", "");
            List<Reviews> unchainedReviews = this.list(queryWrapper);

            if (unchainedReviews.isEmpty()) {
                result.put("code", 1);
                result.put("message", "没有需要上链的评论");
                return result;
            }

            int successCount = 0;
            int failCount = 0;
            List<Map<String, Object>> details = new ArrayList<>();

            for (Reviews review : unchainedReviews) {
                Map<String, Object> singleResult = uploadReviewToBlockchain(review.getId());
                Map<String, Object> detail = new HashMap<>();
                detail.put("reviewId", review.getId());
                detail.put("result", singleResult);

                if ((Integer) singleResult.get("code") == 0) {
                    successCount++;
                    detail.put("status", "success");
                } else {
                    failCount++;
                    detail.put("status", "failed");
                }
                details.add(detail);
            }

            result.put("code", 0);
            result.put("message", String.format("批量上链完成，成功：%d，失败：%d", successCount, failCount));
            result.put("successCount", successCount);
            result.put("failCount", failCount);
            result.put("details", details);

        } catch (Exception e) {
            result.put("code", -1);
            result.put("message", "批量上链失败：" + e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

    /**
     * 异步上传评论到区块链（不阻塞主流程）
     * 
     * @param reviewId 评论ID
     */
    @Async
    public void asyncUploadToBlockchain(Long reviewId) {
        try {
            // 延迟2秒执行，确保数据库事务提交完成
            Thread.sleep(2000);
            Map<String, Object> result = uploadReviewToBlockchain(reviewId);
            System.out.println("异步上链完成 - 评论ID: " + reviewId + ", 结果: " + result.get("message"));
        } catch (Exception e) {
            System.err.println("异步上链失败 - 评论ID: " + reviewId + ", 错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 获取所有已上链的评论列表
     * 
     * @param page     页码
     * @param pageSize 每页数量
     * @return 包含已上链评论列表的Map
     */
    @Override
    public Map<String, Object> getBlockchainReviews(Integer page, Integer pageSize) {
        Map<String, Object> result = new HashMap<>();

        try {
            Page<Reviews> pageInfo = new Page<>(page, pageSize);
            QueryWrapper<Reviews> queryWrapper = new QueryWrapper<>();
            queryWrapper.isNotNull("tx_hash")
                    .ne("tx_hash", "")
                    .orderByDesc("created_at");

            Page<Reviews> reviewPage = this.page(pageInfo, queryWrapper);
            List<Reviews> blockchainReviews = reviewPage.getRecords();

            List<Map<String, Object>> reviewList = new ArrayList<>();
            for (Reviews review : blockchainReviews) {
                Map<String, Object> reviewData = new HashMap<>();
                reviewData.put("id", review.getId());
                reviewData.put("productId", review.getProductId());
                reviewData.put("userAddress", review.getUserAddress());
                reviewData.put("rating", review.getRating());
                reviewData.put("content", review.getContent());
                reviewData.put("txHash", review.getTxHash());
                reviewData.put("blockchainReviewId", review.getBlockchainReviewId());
                reviewData.put("createdAt", review.getCreatedAt());
                reviewData.put("blockchainStatus", "已上链");

                // 获取商品名称
                try {
                    Products product = productsService.getById(review.getProductId());
                    reviewData.put("productName", product != null ? product.getName() : "未知商品");
                } catch (Exception e) {
                    reviewData.put("productName", "未知商品");
                }

                reviewList.add(reviewData);
            }

            result.put("list", reviewList);
            result.put("total", reviewPage.getTotal());
            result.put("page", page);
            result.put("pageSize", pageSize);
            result.put("code", 0);
            result.put("message", "已上链评论列表获取成功");

        } catch (Exception e) {
            result.put("code", -1);
            result.put("message", "获取已上链评论失败：" + e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

    @Override
    public Map<String, Object> fixBlockchainReviewIds() {
        Map<String, Object> result = new HashMap<>();
        int fixedCount = 0;

        try {
            // 查询所有有tx_hash但没有blockchain_review_id的记录
            QueryWrapper<Reviews> queryWrapper = new QueryWrapper<>();
            queryWrapper.isNotNull("tx_hash")
                    .isNull("blockchain_review_id");

            List<Reviews> reviewsToFix = this.list(queryWrapper);
            System.out.println("需要修复的评论数量: " + reviewsToFix.size());

            for (Reviews review : reviewsToFix) {
                try {
                    System.out.println("修复评论ID: " + review.getId() + ", txHash: " + review.getTxHash());

                    // 获取区块链上的总评论数
                    CallResponse totalResponse = reviewCoreService.totalReviews();
                    BigInteger totalReviews = (BigInteger) totalResponse.getReturnObject().get(0);

                    // 使用更安全的估算方法：从0开始递增
                    // 避免负数ID
                    Long estimatedId = (long) fixedCount;

                    // 确保ID在合理范围内
                    if (estimatedId < 0) {
                        estimatedId = 0L;
                    }
                    if (estimatedId >= totalReviews.longValue()) {
                        estimatedId = totalReviews.longValue() - 1;
                    }

                    review.setBlockchainReviewId(estimatedId);
                    this.updateById(review);
                    fixedCount++;

                    System.out.println("修复成功，评论ID: " + review.getId() + ", blockchainReviewId: " + estimatedId);

                } catch (Exception e) {
                    System.err.println("修复评论失败，ID: " + review.getId() + ", 错误: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            result.put("code", 0);
            result.put("message", "修复完成，共修复 " + fixedCount + " 条评论");
            result.put("fixedCount", fixedCount);

        } catch (Exception e) {
            result.put("code", -1);
            result.put("message", "修复失败：" + e.getMessage());
            e.printStackTrace();
        }

        return result;
    }
}