package com.lwf.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lwf.entity.Products;
import com.lwf.entity.Reviews;
import com.lwf.entity.Users;
import com.lwf.entity.dto.ReviewDTO;
import com.lwf.mapper.ReviewsMapper;
import com.lwf.service.IProductsService;
import com.lwf.service.IReviewsService;
import com.lwf.service.IUsersService;
import com.lwf.service.SimpleCacheService;
import com.lwf.utils.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

@Service
public class ReviewsServiceImpl extends ServiceImpl<ReviewsMapper, Reviews> implements IReviewsService {

    @Autowired
    private IUsersService usersService;

    @Autowired
    private IProductsService productsService;

    @Autowired
    private SimpleCacheService cacheService;

    @Autowired
    private ReviewsMapper reviewsMapper;

/**
 * 提交商品评价的方法
 * 该方法处理用户提交商品评价的业务逻辑，包括验证、创建评价和更新相关数据
 *
 * @param reviewDTO 包含评价信息的DTO对象
 * @return 返回包含评价结果的Map对象，包含状态码、NFT ID、评价ID和消息
 * @throws BusinessException 当商品不存在、用户不存在或用户已评价过商品时抛出
 */
    @Override
    @Transactional  // 声明事务注解，确保方法内操作的事务性
    public Map<String, Object> submitReview(ReviewDTO reviewDTO) {
    // 创建结果Map，用于返回操作结果
        Map<String, Object> result = new HashMap<>();

        // 验证商品是否存在
        Products product = productsService.getById(reviewDTO.getProductId());
        if (product == null) {
            throw new BusinessException("商品不存在");
        }

        // 查找用户（这里简化处理，实际应该从token中获取用户信息）
        QueryWrapper<Users> userQuery = new QueryWrapper<>();
        userQuery.eq("address", product.getMerchantAddress()); // 简化，实际应该从上下文获取
        Users user = usersService.getOne(userQuery);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 检查是否已经评价过
        if (hasUserReviewed(user.getId(), reviewDTO.getProductId())) {
            throw new BusinessException("您已经评价过该商品");
        }

        // 创建评价对象并设置属性
        Reviews review = new Reviews();
        review.setProductId(reviewDTO.getProductId());
        review.setUserId(user.getId());
        review.setUserAddress(user.getAddress());
        review.setRating(reviewDTO.getRating());
        review.setContent(reviewDTO.getContent());
        review.setIpfsCid(reviewDTO.getIpfsCid());
    // 生成唯一的NFT ID，包含商品ID和时间戳
        review.setNftId("NFT_" + reviewDTO.getProductId() + "_" + System.currentTimeMillis());
        review.setHelpfulVotes(0);  // 初始化有用投票数为0
        review.setUnhelpfulVotes(0);  // 初始化无用投票数为0
        review.setVerified(user.getReputationScore() >= 50); // 高信誉用户自动验证

        boolean saved = this.save(review);

        if (saved) {
            // 更新商品评分
            updateProductRating(reviewDTO.getProductId());
            // 更新用户评价数
            usersService.incrementReviewCount(user.getId());

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

        result.put("list", reviewPage.getRecords());
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
     * @param reviewId 评价ID
     * @param userAddress 用户地址
     * @param isHelpful 是否为有用投票
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
}