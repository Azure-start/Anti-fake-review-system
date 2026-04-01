package com.lwf.controller;

import com.lwf.entity.Users;
import com.lwf.service.IUsersService;
import com.lwf.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户控制器类
 * 处理与用户相关的HTTP请求，包括店铺申请和信息查询等功能
 */
@RestController
@RequestMapping("/api")
public class UsersController {

    /**
     * 自动注入用户服务接口
     * 用于处理用户相关的业务逻辑
     */
    @Autowired
    private IUsersService usersService;

    /**
     * 处理店铺申请的接口
     *
     * @param request 包含店铺申请信息的请求体，包含地址、店铺名称和店铺描述等字段
     * @return 返回处理结果，包含申请后的相关信息或错误信息
     */
    @PostMapping("/merchant/shop/apply")
    public Result<Map<String, Object>> applyShop(@RequestBody Map<String, String> request) {
        try {
            // 从请求中获取店铺地址、店铺名称和店铺描述
            String address = request.get("address");
            String shopName = request.get("shopName");
            String shopDescription = request.get("shopDescription");

            // 验证必要参数是否存在
            if (address == null || shopName == null) {
                return Result.error("参数不完整");
            }

            // 调用服务层处理店铺申请逻辑
            Map<String, Object> result = usersService.applyShop(address, shopName, shopDescription);
            return Result.success(result);
        } catch (Exception e) {
            // 捕获并返回异常信息
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取店铺信息的接口
     *
     * @param address 店铺地址，用于查询对应的店铺信息
     * @return 返回店铺信息对象或错误信息
     */
    @GetMapping("/merchant/shop/info")
    public Result<Users> getShopInfo(@RequestParam String address) {
        try {
            // 调用服务层获取店铺信息
            Users shopInfo = usersService.getShopInfo(address);
            return Result.success(shopInfo);
        } catch (Exception e) {
            // 捕获并返回异常信息
            return Result.error(e.getMessage());
        }
    }
}