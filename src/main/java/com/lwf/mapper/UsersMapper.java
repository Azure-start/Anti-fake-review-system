package com.lwf.mapper;

import com.lwf.entity.Users;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.Map;
@Mapper

public interface UsersMapper extends BaseMapper<Users> {

    // 获取商家统计信息
    Map<String, Object> selectMerchantStats();

    // 根据地址查询用户
    Users selectByAddress(@Param("address") String address);

    // 获取待审核商家列表
    Page<Users> selectPendingMerchants(Page<Users> page);

    // 更新用户信誉分
    int updateReputationScore(@Param("userId") Long userId, @Param("reputationScore") Integer reputationScore);

    // 增加用户评价数
    int incrementTotalReviews(@Param("userId") Long userId);
}