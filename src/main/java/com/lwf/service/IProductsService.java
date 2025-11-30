package com.lwf.service;

import com.lwf.entity.Products;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lwf.entity.dto.ProductQueryDTO;

import java.util.Map;

public interface IProductsService extends IService<Products> {
    Map<String, Object> getProductList(ProductQueryDTO query);
    Map<String, Object> createProduct(Products product);
    Map<String, Object> getMerchantProducts(String merchantAddress, Integer page, Integer pageSize);
    Map<String, Object> getPendingProducts(Integer page, Integer pageSize);
    Map<String, Object> auditProduct(Long productId, String action);
    Products getProductDetail(Long productId);
    Map<String, Object> updateProductStatus(Long productId, String status);
}