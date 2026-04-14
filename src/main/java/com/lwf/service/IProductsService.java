package com.lwf.service;

import com.lwf.entity.Products;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lwf.entity.dto.ProductQueryDTO;

import java.util.Map;

public interface IProductsService extends IService<Products> {
    Map<String, Object> getProductList(ProductQueryDTO query);
    Map<String, Object> createProduct(Products product);
    /**
     * 根据商家地址分页查询商品列表，支持多条件筛选
     * @param merchantAddress 商家地址
     * @param page 页码
     * @param pageSize 每页大小
     * @param productName 商品名称（模糊查询）
     * @param status 商品状态
     * @param startTime 上架开始时间
     * @param endTime 上架结束时间
     * @return 包含商品列表和总数的Map
     */
    Map<String, Object> getMerchantProducts(String merchantAddress, Integer page, Integer pageSize, String productName, String status, String startTime, String endTime);
    Map<String, Object> getPendingProducts(Integer page, Integer pageSize);
    Map<String, Object> auditProduct(Long productId, String action);
    Products getProductDetail(Long productId);
    Map<String, Object> updateProductStatus(Long productId, String status);
    Map<String, Object> updateProduct(Long productId, Products product);
    Map<String, Object> deleteProduct(Long productId);
    Map<String, Object> offShelfProduct(Long productId);

    Map<String, Object> onSaleProduct(Long productId);
}