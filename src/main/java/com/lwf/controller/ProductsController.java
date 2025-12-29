package com.lwf.controller;

import com.lwf.entity.Products;
import com.lwf.entity.dto.ProductQueryDTO;
import com.lwf.entity.dto.ReviewDTO;
import com.lwf.service.IProductsService;
import com.lwf.service.IReviewsService;
import com.lwf.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 商品控制器
 * 处理商品相关的HTTP请求，包括商品列表查询、商品详情获取、商品评价获取等功能
 */
@RestController
@RequestMapping("/api")
public class ProductsController {

    /**
     * 商品服务接口
     * 用于处理商品相关的业务逻辑
     */
    @Autowired
    private IProductsService productsService;

    /**
     * 商品评价服务接口
     * 用于处理商品评价相关的业务逻辑
     */
    @Autowired
    private IReviewsService reviewsService;

    /**
     * 获取商品列表
     * @param query 商品查询条件对象，包含各种筛选参数
     * @return 返回一个Result对象，其中包含一个Map类型的响应数据，包含商品列表及分页信息
     */
    @GetMapping("/products")
    public Result<Map<String, Object>> getProductList(ProductQueryDTO query) {
        try {
            Map<String, Object> result = productsService.getProductList(query);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取商品详情
     * @param productId 商品ID，通过路径变量传递
     * @return 返回一个Result对象，其中包含商品详细信息
     */
    @GetMapping("/products/{productId}")
    public Result<Products> getProductDetail(@PathVariable Long productId) {
        try {
            Products product = productsService.getProductDetail(productId);
            return Result.success(product);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取商品评价列表
     * @param productId 商品ID，通过路径变量传递
     * @param page 页码，默认为1
     * @param pageSize 每页数量，默认为10
     * @return 返回一个Result对象，其中包含商品评价列表及分页信息
     */
    @GetMapping("/products/{productId}/reviews")
    public Result<Map<String, Object>> getProductReviews(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            Map<String, Object> result = reviewsService.getProductReviews(productId, page, pageSize);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }


/**
 * 创建商品接口
 * @param product 接收前端传递的商品信息，使用@RequestBody注解将请求体映射到Products对象
 * @return 返回一个Result对象，其中包含一个Map类型的响应数据
 */
    @PostMapping("/merchant/products")
    public Result<Map<String, Object>> createProduct(@RequestBody Products product) {
        try {
        // 调用productsService的createProduct方法创建商品，并将返回结果存储在result中
            Map<String, Object> result = productsService.createProduct(product);
        // 创建成功，返回成功响应，包含商品创建后的数据
            return Result.success(result);
        } catch (Exception e) {
        // 创建过程中发生异常，返回错误响应，包含异常信息
            return Result.error(e.getMessage());
        }
    }

/**
 * 获取商户商品列表接口
 * @param merchantAddress 商户地址
 * @param page 页码，默认为1
 * @param pageSize 每页数量，默认为10
 * @return 返回商品列表数据
 */
    @GetMapping("/merchant/products")
    public Result<Map<String, Object>> getMerchantProducts(
            @RequestParam String merchantAddress,  // 商户地址参数
            @RequestParam(defaultValue = "1") Integer page,  // 页码参数，默认值为1
            @RequestParam(defaultValue = "10") Integer pageSize) {  // 每页数量参数，默认值为10
        try {
        // 调用服务层获取商户商品列表
            Map<String, Object> result = productsService.getMerchantProducts(merchantAddress, page, pageSize);
        // 返回成功结果
            return Result.success(result);
        } catch (Exception e) {
        // 异常处理，返回错误信息
            return Result.error(e.getMessage());
        }
    }
}