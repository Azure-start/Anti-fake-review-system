package com.lwf.controller;

import com.lwf.entity.dto.LoginDTO;
import com.lwf.service.IUsersService;
import com.lwf.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 认证控制器类
 * 处理用户认证相关的请求，包括获取随机数和登录功能
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    /**
     * 自动注入用户服务接口
     * 用于处理用户相关的业务逻辑
     */
    @Autowired
    private IUsersService usersService;

    /**
     * 获取随机数接口
     * 用于生成指定地址的随机数，通常用于登录验证流程
     * @param request 包含用户地址的请求体
     * @return 返回生成的随机数或错误信息
     */
    @PostMapping("/nonce")
    public Result<String> getNonce(@RequestBody Map<String, String> request) {

        System.out.println("我进来获取nonce值了");

        String address = request.get("address");
        if (address == null || address.isEmpty()) {
            return Result.error("地址不能为空");
        }
        String nonce = usersService.generateNonce(address);
        return Result.success(nonce);
    }

    /**
     * 用户登录接口
     * 验证用户登录信息并返回登录结果
     * @param loginDTO 包含登录信息的DTO对象
     * @return 返回登录结果或错误信息
     */
    @PostMapping("/signin")
    public Result<Map<String, Object>> signIn(@Validated @RequestBody LoginDTO loginDTO) {

        System.out.println("我进来登录验证了");
        System.out.println(loginDTO);

        try {
            Map<String, Object> result = usersService.signIn(loginDTO);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}