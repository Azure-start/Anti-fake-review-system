package com.lwf.mapper;

import com.lwf.entity.Reviews;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

@Mapper
public interface ReviewsMapper extends BaseMapper<Reviews> {

    // 获取商品评价列表
    List<Reviews> selectByProductId(
            @Param("productId") Long productId,
            @Param("sortBy") String sortBy
    );

    // 获取用户评价列表
    List<Reviews> selectByUserAddress(@Param("userAddress") String userAddress);

    // 检查用户是否已评价商品
    Boolean existsByUserIdAndProductId(
            @Param("userId") Long userId,
            @Param("productId") Long productId
    );

    // 获取商品平均评分
    Double selectAverageRatingByProductId(@Param("productId") Long productId);

    // 获取评价统计信息
    Map<String, Object> selectReviewStats(@Param("productId") Long productId);

    // 更新评价投票数
    int updateVotes(
            @Param("reviewId") Long reviewId,
            @Param("helpfulVotes") Integer helpfulVotes,
            @Param("unhelpfulVotes") Integer unhelpfulVotes
    );

    // 获取热门评价
    List<Reviews> selectTopHelpfulReviews(
            @Param("productId") Long productId,
            @Param("limit") Integer limit
    );
}