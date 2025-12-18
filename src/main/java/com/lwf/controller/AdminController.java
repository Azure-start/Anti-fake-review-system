package com.lwf.controller;

import com.lwf.service.IProductsService;
import com.lwf.service.IUsersService;
import com.lwf.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 管理员控制器
 * 提供管理员相关的API接口，包括用户管理、店铺申请审核、商品审核和系统统计等功能
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    /**
     * 用户服务接口
     * 用于处理用户相关的业务逻辑
     */
    @Autowired
    private IUsersService usersService;

    /**
     * 商品服务接口
     * 用于处理商品相关的业务逻辑
     */
    @Autowired
    private IProductsService productsService;

    /**
     * 获取用户列表
     * @param page 页码，默认为1
     * @param pageSize 每页大小，默认为10
     * @return 返回用户列表数据
     */
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

    /**
     * 获取店铺申请列表
     * @param page 页码，默认为1
     * @param pageSize 每页大小，默认为10
     * @return 返回店铺申请列表数据
     */
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

    /**
     * 审核店铺申请
     * @param applicationId 店铺申请ID
     * @param request 包含操作类型的请求体
     * @return 返回审核结果
     */
    @PutMapping("/shop-applications/{applicationId}/audit")  // 定义PUT请求路径，用于处理店铺申请审核
    public Result<Map<String, Object>> auditShopApplication(  // 方法定义，返回包含审核结果的Map
            @PathVariable Long applicationId,  // 从路径变量中获取店铺申请ID
            @RequestBody Map<String, String> request) {  // 从请求体中获取包含操作类型的Map
        try {
            String action = request.get("action");  // 从请求中获取操作类型
            // 检查操作类型是否有效
            if (action == null || (!"approve".equals(action) && !"reject".equals(action))) {  // 验证操作类型是否为"approve"或"reject"
                return Result.error("无效的操作类型");  // 如果操作类型无效，返回错误信息
            }
            Map<String, Object> result = usersService.auditShopApplication(applicationId, action);  // 调用服务层方法处理审核逻辑
            return Result.success(result);  // 返回成功结果，包含审核处理后的数据
        } catch (Exception e) {  // 捕获处理过程中可能出现的异常
            return Result.error(e.getMessage());  // 返回错误信息，包含异常的具体描述
        }
    }

    /**
     * 获取待审核商品列表
     * @param page 页码，默认为1
     * @param pageSize 每页大小，默认为10
     * @return 返回待审核商品列表数据
     */
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

    /**
     * 审核商品
     * @param productId 商品ID
     * @param request 包含操作类型的请求体
     * @return 返回审核结果
     */
    @PutMapping("/products/{productId}/audit")
    public Result<Map<String, Object>> auditProduct(
            @PathVariable Long productId,
            @RequestBody Map<String, String> request) {
        try {
            String action = request.get("action");
            // 检查操作类型是否有效
            if (action == null || (!"approve".equals(action) && !"reject".equals(action))) {
                return Result.error("无效的操作类型");
            }
            Map<String, Object> result = productsService.auditProduct(productId, action);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取系统统计数据
     * @return 返回系统统计数据
     */
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