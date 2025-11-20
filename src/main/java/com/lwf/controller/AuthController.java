package com.lwf.controller;

import com.lwf.entity.dto.LoginDTO;
import com.lwf.service.IUsersService;
import com.lwf.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private IUsersService usersService;

    @PostMapping("/nonce")
    public Result<String> getNonce(@RequestBody Map<String, String> request) {
        String address = request.get("address");
        if (address == null || address.isEmpty()) {
            return Result.error("地址不能为空");
        }
        String nonce = usersService.generateNonce(address);
        return Result.success(nonce);
    }

    @PostMapping("/signin")
    public Result<Map<String, Object>> signIn(@Validated @RequestBody LoginDTO loginDTO) {
        try {
            Map<String, Object> result = usersService.signIn(loginDTO);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}