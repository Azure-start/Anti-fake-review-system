package com.lwf.controller;

import com.lwf.entity.dto.OrderDTO;
import com.lwf.service.IOrdersService;
import com.lwf.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 订单控制器类
 * 提供订单相关的RESTful API接口
 */
@RestController
@RequestMapping("/api")
public class OrdersController {

    /**
     * 自动注入订单服务接口
     */
    @Autowired
    private IOrdersService ordersService;

    /**
     * 创建订单接口
     * 
     * @param orderDTO 订单数据传输对象，包含创建订单所需的信息
     * @return 返回操作结果，包含订单创建后的相关信息
     */
    @PostMapping("/orders")
    public Result<Map<String, Object>> createOrder(@Validated @RequestBody OrderDTO orderDTO) {
        try {
            Map<String, Object> result = ordersService.createOrder(orderDTO);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取用户订单列表接口
     * 
     * @param userAddress 用户地址
     * @param page        页码，默认为1
     * @param pageSize    每页大小，默认为10
     * @return 返回操作结果，包含用户订单列表信息
     */
    @GetMapping("/orders/user")
    public Result<Map<String, Object>> getUserOrders(
            @RequestParam String userAddress,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            Map<String, Object> result = ordersService.getUserOrders(userAddress, page, pageSize);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取商家订单列表接口
     * 
     * @param merchantId 商家ID
     * @param page       页码，默认为1
     * @param pageSize   每页大小，默认为10
     * @return 返回操作结果，包含商家订单列表信息
     */
    @GetMapping("/orders/merchant")
    public Result<Map<String, Object>> getMerchantOrders(
            @RequestParam Long merchantId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            Map<String, Object> result = ordersService.getMerchantOrders(merchantId, page, pageSize);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新订单状态接口
     * 
     * @param orderId 订单ID
     * @param request 包含状态信息的请求体
     * @return 返回操作结果，包含更新后的订单信息
     */
    @PutMapping("/orders/{orderId}/status")
    public Result<Map<String, Object>> updateOrderStatus(
            @PathVariable String orderId,
            @RequestBody Map<String, String> request) {
        try {
            String status = request.get("status");
            if (status == null) {
                return Result.error("状态不能为空");
            }
            Map<String, Object> result = ordersService.updateOrderStatus(orderId, status);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 确认收货接口
     * 
     * @param orderId     订单ID
     * @param userAddress 用户地址
     * @return 返回操作结果，包含确认收货后的订单信息
     */
    @PutMapping("/orders/{orderId}/confirm")
    public Result<Map<String, Object>> confirmReceipt(
            @PathVariable String orderId,
            @RequestParam String userAddress) {
        try {
            Map<String, Object> result = ordersService.confirmReceipt(orderId, userAddress);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取订单详情接口
     * 
     * @param orderId 订单ID
     * @return 返回操作结果，包含订单详细信息
     */
    @GetMapping("/orders/{orderId}")
    public Result<Map<String, Object>> getOrderDetail(@PathVariable String orderId) {
        try {
            Map<String, Object> result = ordersService.getOrderDetail(orderId);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新订单交易哈希接口
     * 
     * @param orderId 订单ID
     * @param request 包含交易哈希的请求体
     * @return 返回操作结果，包含更新后的订单信息
     */
    @PutMapping("/orders/{orderId}/tx-hash")
    public Result<Map<String, Object>> updateOrderTxHash(
            @PathVariable String orderId,
            @RequestBody Map<String, String> request) {
        try {
            String txHash = request.get("txHash");
            if (txHash == null) {
                return Result.error("交易哈希不能为空");
            }
            Map<String, Object> result = ordersService.updateOrderTxHash(orderId, txHash);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}