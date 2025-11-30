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

@RestController
@RequestMapping("/api")
public class ProductsController {

    @Autowired
    private IProductsService productsService;

    @Autowired
    private IReviewsService reviewsService;

    @GetMapping("/products")
    public Result<Map<String, Object>> getProductList(ProductQueryDTO query) {
        try {
            Map<String, Object> result = productsService.getProductList(query);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/products/{productId}")
    public Result<Products> getProductDetail(@PathVariable Long productId) {
        try {
            Products product = productsService.getProductDetail(productId);
            return Result.success(product);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

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

//    @PostMapping("/reviews")
//    public Result<Map<String, Object>> submitReview(@Validated @RequestBody ReviewDTO reviewDTO) {
//        try {
//            Map<String, Object> result = reviewsService.submitReview(reviewDTO);
//            return Result.success(result);
//        } catch (Exception e) {
//            return Result.error(e.getMessage());
//        }
//    }

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