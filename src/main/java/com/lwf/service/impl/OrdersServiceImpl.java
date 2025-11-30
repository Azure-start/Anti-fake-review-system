package com.lwf.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lwf.entity.Orders;
import com.lwf.entity.Products;
import com.lwf.entity.Users;
import com.lwf.entity.dto.OrderDTO;
import com.lwf.mapper.OrdersMapper;
import com.lwf.service.IOrdersService;
import com.lwf.service.IProductsService;
import com.lwf.service.IUsersService;
import com.lwf.utils.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements IOrdersService {

    @Autowired
    private IProductsService productsService;

    @Autowired
    private IUsersService usersService;

    @Override
    @Transactional
    public Map<String, Object> createOrder(OrderDTO orderDTO) {
        Map<String, Object> result = new HashMap<>();

        // 验证商品
        Products product = productsService.getById(orderDTO.getProductId());
        if (product == null) {
            throw new BusinessException("商品不存在");
        }

        if (!"approved".equals(product.getStatus())) {
            throw new BusinessException("商品未上架");
        }

        if (product.getStock() < orderDTO.getQuantity()) {
            throw new BusinessException("商品库存不足");
        }

        // 验证用户
        QueryWrapper<Users> userQuery = new QueryWrapper<>();
        userQuery.eq("address", orderDTO.getUserAddress());
        Users user = usersService.getOne(userQuery);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 创建订单
        Orders order = new Orders();
        order.setOrderId("ORD" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8));
        order.setProductId(orderDTO.getProductId());
        order.setUserId(user.getId());
        order.setUserAddress(orderDTO.getUserAddress());
        order.setMerchantId(product.getMerchantId());
        order.setProductName(orderDTO.getProductName() != null ? orderDTO.getProductName() : product.getName());
        order.setAmount(orderDTO.getAmount() != null ? orderDTO.getAmount() : product.getPrice());
        order.setType("purchase");
        order.setStatus("pending");
        order.setReceiveStatus("pending");
        order.setTxHash("tx_" + System.currentTimeMillis()); // 模拟交易哈希

        boolean saved = this.save(order);

        if (saved) {
            // 更新商品库存
            product.setStock(product.getStock() - orderDTO.getQuantity());
            productsService.updateById(product);

            result.put("code", 0);
            result.put("orderId", order.getOrderId());
            result.put("txHash", order.getTxHash());
            result.put("message", "订单创建成功");
        } else {
            throw new BusinessException("订单创建失败");
        }

        return result;
    }

    @Override
    public Map<String, Object> getUserOrders(String userAddress, Integer page, Integer pageSize) {
        Map<String, Object> result = new HashMap<>();

        Page<Orders> pageInfo = new Page<>(page, pageSize);
        QueryWrapper<Orders> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_address", userAddress)
                .orderByDesc("created_at");

        Page<Orders> orderPage = this.page(pageInfo, queryWrapper);

        result.put("list", orderPage.getRecords());
        result.put("total", orderPage.getTotal());
        result.put("page", page);
        result.put("pageSize", pageSize);
        result.put("code", 0);

        return result;
    }

    @Override
    public Map<String, Object> getMerchantOrders(Long merchantId, Integer page, Integer pageSize) {
        Map<String, Object> result = new HashMap<>();

        Page<Orders> pageInfo = new Page<>(page, pageSize);
        QueryWrapper<Orders> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("merchant_id", merchantId)
                .orderByDesc("created_at");

        Page<Orders> orderPage = this.page(pageInfo, queryWrapper);

        result.put("list", orderPage.getRecords());
        result.put("total", orderPage.getTotal());
        result.put("page", page);
        result.put("pageSize", pageSize);
        result.put("code", 0);

        return result;
    }

    @Override
    public Map<String, Object> updateOrderStatus(String orderId, String status) {
        Map<String, Object> result = new HashMap<>();

        QueryWrapper<Orders> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_id", orderId);
        Orders order = this.getOne(queryWrapper);

        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        order.setStatus(status);
        this.updateById(order);

        result.put("code", 0);
        result.put("orderId", orderId);
        result.put("status", status);
        result.put("message", "订单状态更新成功");

        return result;
    }

    @Override
    public Map<String, Object> confirmReceipt(String orderId, String userAddress) {
        Map<String, Object> result = new HashMap<>();

        QueryWrapper<Orders> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_id", orderId)
                .eq("user_address", userAddress);
        Orders order = this.getOne(queryWrapper);

        if (order == null) {
            throw new BusinessException("订单不存在或不属于当前用户");
        }

        if (!"completed".equals(order.getStatus())) {
            throw new BusinessException("订单状态异常，无法确认收货");
        }

        order.setReceiveStatus("confirmed");
        this.updateById(order);

        result.put("code", 0);
        result.put("orderId", orderId);
        result.put("message", "收货确认成功");

        return result;
    }

    @Override
    public Orders getOrderByTxHash(String txHash) {
        QueryWrapper<Orders> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("tx_hash", txHash);
        return this.getOne(queryWrapper);
    }
}
