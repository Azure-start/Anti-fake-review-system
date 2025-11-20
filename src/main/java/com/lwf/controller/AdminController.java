package com.lwf.controller;

import com.lwf.service.IProductsService;
import com.lwf.service.IUsersService;
import com.lwf.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private IUsersService usersService;

    @Autowired
    private IProductsService productsService;

    @GetMapping("/users")
    public Result<Map<String, Object>> getUserList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            Map<String, Object> result = usersService.getUserList(page, pageSize);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/shop-applications")
    public Result<Map<String, Object>> getShopApplications(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            Map<String, Object> result = usersService.getShopApplications(page, pageSize);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/shop-applications/{applicationId}/audit")
    public Result<Map<String, Object>> auditShopApplication(
            @PathVariable Long applicationId,
            @RequestBody Map<String, String> request) {
        try {
            String action = request.get("action");
            if (action == null || (!"approve".equals(action) && !"reject".equals(action))) {
                return Result.error("无效的操作类型");
            }
            Map<String, Object> result = usersService.auditShopApplication(applicationId, action);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/products/pending")
    public Result<Map<String, Object>> getPendingProducts(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            Map<String, Object> result = productsService.getPendingProducts(page, pageSize);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/products/{productId}/audit")
    public Result<Map<String, Object>> auditProduct(
            @PathVariable Long productId,
            @RequestBody Map<String, String> request) {
        try {
            String action = request.get("action");
            if (action == null || (!"approve".equals(action) && !"reject".equals(action))) {
                return Result.error("无效的操作类型");
            }
            Map<String, Object> result = productsService.auditProduct(productId, action);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/stats")
    public Result<Map<String, Object>> getSystemStats() {
        try {
            Map<String, Object> result = usersService.getSystemStats();
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}