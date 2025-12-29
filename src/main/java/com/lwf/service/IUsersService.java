package com.lwf.service;

import com.lwf.entity.Users;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lwf.entity.dto.LoginDTO;
import java.util.Map;

public interface IUsersService extends IService<Users> {
    String generateNonce(String address);
    Map<String, Object> signIn(LoginDTO loginDTO);
    Map<String, Object> applyShop(String address, String shopName, String shopDescription);
    Users getShopInfo(String address);
    Map<String, Object> getUserList(Integer page, Integer pageSize);
    Map<String, Object> getShopApplications(Integer page, Integer pageSize);
    Map<String, Object> auditShopApplication(Long applicationId, String action);
    Map<String, Object> getSystemStats();
    void incrementReviewCount(Long userId);
}