package com.lwf.mapper;

import com.lwf.entity.Orders;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrdersMapper extends BaseMapper<Orders> {

    // 获取用户订单列表
    List<Orders> selectByUserAddress(@Param("userAddress") String userAddress);

    // 获取商家订单列表
    List<Orders> selectByMerchantId(@Param("merchantId") Long merchantId);

    // 根据订单号查询
    Orders selectByOrderId(@Param("orderId") String orderId);

    // 根据交易哈希查询
    Orders selectByTxHash(@Param("txHash") String txHash);

    // 更新订单状态
    int updateOrderStatus(@Param("orderId") String orderId, @Param("status") String status);

    // 确认收货
    int confirmReceipt(@Param("orderId") String orderId, @Param("userAddress") String userAddress);

    // 获取订单统计信息
    Map<String, Object> selectOrderStats(
            @Param("merchantId") Long merchantId,
            @Param("userAddress") String userAddress
    );

    // 获取最近订单
    List<Orders> selectRecentOrders(@Param("limit") Integer limit);
}