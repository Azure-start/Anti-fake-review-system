package com.lwf.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lwf.entity.Orders;
import com.lwf.entity.dto.OrderDTO;

import java.util.Map;

/**
 * 订单服务接口，继承自IService<Orders>，提供订单相关的业务操作
 */
public interface IOrdersService extends IService<Orders> {
    /**
     * 创建新订单
     * 
     * @param orderDTO 订单数据传输对象，包含创建订单所需的信息
     * @return 返回包含操作结果和数据的Map对象
     */
    Map<String, Object> createOrder(OrderDTO orderDTO);

    /**
     * 获取指定用户的订单列表
     * 
     * @param userAddress 用户地址，用于标识用户
     * @param page        页码，用于分页查询
     * @param pageSize    每页大小，用于分页查询
     * @return 返回包含用户订单列表和分页信息的Map对象
     */
    Map<String, Object> getUserOrders(String userAddress, Integer page, Integer pageSize);

    /**
     * 获取指定商家的订单列表
     * 
     * @param merchantId 商家ID，用于标识商家
     * @param page       页码，用于分页查询
     * @param pageSize   每页大小，用于分页查询
     * @return 返回包含商家订单列表和分页信息的Map对象
     */
    Map<String, Object> getMerchantOrders(Long merchantId, Integer page, Integer pageSize);

    /**
     * 获取订单详情
     * 
     * @param orderId 订单ID
     * @return 返回包含订单详细信息的Map对象
     */
    Map<String, Object> getOrderDetail(String orderId);

    /**
     * 更新订单状态
     * 
     * @param orderId 订单ID，用于标识要更新的订单
     * @param status  新的订单状态
     * @return 返回包含操作结果和数据的Map对象
     */
    Map<String, Object> updateOrderStatus(String orderId, String status);

    /**
     * 确认收货
     * 
     * @param orderId     订单ID，用于标识要确认收货的订单
     * @param userAddress 用户地址，用于验证操作权限
     * @return 返回包含操作结果和数据的Map对象
     */
    Map<String, Object> confirmReceipt(String orderId, String userAddress);

    /**
     * 根据交易哈希获取订单信息
     * 
     * @param txHash 交易哈希，用于标识订单
     * @return 返回对应的订单对象
     */
    Orders getOrderByTxHash(String txHash);

    /**
     * 更新订单交易哈希
     * 
     * @param orderId 订单ID
     * @param txHash  交易哈希
     * @return 返回包含操作结果和数据的Map对象
     */
    Map<String, Object> updateOrderTxHash(String orderId, String txHash);

    /**
     * 更新订单评价状态
     * 
     * @param orderId      订单ID
     * @param reviewStatus 评价状态（0: 未评价，1: 已评价）
     * @return 返回包含操作结果和数据的Map对象
     */
    Map<String, Object> updateReviewStatus(String orderId, Integer reviewStatus);
}