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
        order.setTxHash(null); // 初始时无交易哈希，支付后更新

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
    @Transactional
    public Map<String, Object> updateOrderStatus(String orderId, String status) {
        Map<String, Object> result = new HashMap<>();

        QueryWrapper<Orders> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_id", orderId);
        Orders order = this.getOne(queryWrapper);

        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        String oldStatus = order.getStatus();
        order.setStatus(status);
        this.updateById(order);

        // 当订单状态从pending变为completed时，增加商品销量
        if ("pending".equals(oldStatus) && "completed".equals(status)) {
            Products product = productsService.getById(order.getProductId());
            if (product != null) {
                // 由于订单表中没有数量字段，默认每个订单增加1个销量
                product.setSales(product.getSales() + 1);
                productsService.updateById(product);
            }
        }

        result.put("code", 0);
        result.put("orderId", orderId);
        result.put("status", status);
        result.put("message", "订单状态更新成功");

        return result;
    }

    @Override
    @Transactional
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

        // 只有在未确认收货的情况下才更新销量
        if (!"confirmed".equals(order.getReceiveStatus())) {
            order.setReceiveStatus("confirmed");
            this.updateById(order);

            // 确认收货时增加商品销量
            Products product = productsService.getById(order.getProductId());
            if (product != null) {
                // 由于订单表中没有数量字段，默认每个订单增加1个销量
                product.setSales(product.getSales() + 1);
                productsService.updateById(product);
            }
        }

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

    @Override
    @Transactional
    public Map<String, Object> updateOrderTxHash(String orderId, String txHash) {
        Map<String, Object> result = new HashMap<>();

        QueryWrapper<Orders> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_id", orderId);
        Orders order = this.getOne(queryWrapper);

        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        // 验证交易哈希格式（以太坊交易哈希为66位十六进制字符串）
        if (txHash != null && !txHash.matches("^0x[a-fA-F0-9]{64}$")) {
            throw new BusinessException("交易哈希格式不正确");
        }

        order.setTxHash(txHash);
        this.updateById(order);

        result.put("code", 0);
        result.put("orderId", orderId);
        result.put("txHash", txHash);
        result.put("message", "交易哈希更新成功");

        return result;
    }

    @Override
    public Map<String, Object> getOrderDetail(String orderId) {
        Map<String, Object> result = new HashMap<>();

        QueryWrapper<Orders> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_id", orderId);
        Orders order = this.getOne(queryWrapper);

        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        // 获取商品信息
        Products product = productsService.getById(order.getProductId());
        if (product == null) {
            throw new BusinessException("订单对应的商品不存在");
        }

        // 获取商家信息
        Users merchant = usersService.getById(product.getMerchantId());
        if (merchant == null) {
            throw new BusinessException("订单对应的商家不存在");
        }

        // 构建返回结果
        result.put("orderId", order.getOrderId());
        result.put("productId", order.getProductId());
        result.put("productName", product.getName());
        result.put("amount", order.getAmount());
        result.put("status", order.getStatus());
        result.put("receiveStatus", order.getReceiveStatus());
        result.put("txHash", order.getTxHash());
        result.put("spec", null); // 数据库中没有spec字段
        result.put("specText", null); // 数据库中没有spec_text字段
        result.put("address", null); // 数据库中没有address字段
        result.put("merchantName", merchant.getShopName());
        result.put("merchantAddress", product.getMerchantAddress());
        result.put("createdAt", order.getCreatedAt());
        result.put("updatedAt", order.getUpdatedAt());

        return result;
    }

    @Override
    @Transactional
    public Map<String, Object> updateReviewStatus(String orderId, Integer reviewStatus) {
        Map<String, Object> result = new HashMap<>();

        QueryWrapper<Orders> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_id", orderId);
        Orders order = this.getOne(queryWrapper);

        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        order.setReviewStatus(reviewStatus);
        this.updateById(order);

        result.put("code", 0);
        result.put("orderId", orderId);
        result.put("reviewStatus", reviewStatus);
        result.put("message", "评价状态更新成功");

        return result;
    }
    
    @Override
    public Map<String, Object> getMerchantOrdersByAddress(String merchantAddress, Integer page, Integer pageSize,
                                                          String orderId, String productName, String customerAddress,
                                                          String status, String startTime, String endTime) {
        Map<String, Object> result = new HashMap<>();
        
        // 根据商家地址查询商家ID
        QueryWrapper<Users> userQuery = new QueryWrapper<>();
        userQuery.eq("address", merchantAddress);
        userQuery.eq("role", "merchant");
        Users merchant = usersService.getOne(userQuery);
        
        if (merchant == null) {
            throw new BusinessException("商家不存在");
        }
        
        // 根据商家ID查询订单列表
        Page<Orders> pageInfo = new Page<>(page, pageSize);
        QueryWrapper<Orders> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("merchant_id", merchant.getId());
        
        // 添加查询条件
        if (orderId != null && !orderId.isEmpty()) {
            queryWrapper.like("order_id", orderId);
        }
        
        if (productName != null && !productName.isEmpty()) {
            queryWrapper.like("product_name", productName);
        }
        
        if (customerAddress != null && !customerAddress.isEmpty()) {
            queryWrapper.like("user_address", customerAddress);
        }
        
        if (status != null && !status.isEmpty()) {
            queryWrapper.eq("status", status);
        }
        
        if (startTime != null && !startTime.isEmpty()) {
            queryWrapper.ge("created_at", startTime);
        }
        
        if (endTime != null && !endTime.isEmpty()) {
            queryWrapper.le("created_at", endTime);
        }
        
        queryWrapper.orderByDesc("created_at");
        
        Page<Orders> orderPage = this.page(pageInfo, queryWrapper);
        
        result.put("list", orderPage.getRecords());
        result.put("total", orderPage.getTotal());
        result.put("page", page);
        result.put("pageSize", pageSize);
        result.put("code", 0);
        
        return result;
    }
}
