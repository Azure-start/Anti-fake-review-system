package com.lwf.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lwf.entity.Users;
import com.lwf.entity.dto.LoginDTO;
import com.lwf.mapper.UsersMapper;
import com.lwf.service.IUsersService;
import com.lwf.mapper.ProductsMapper;
import com.lwf.mapper.OrdersMapper;
import com.lwf.service.NonceService;
import com.lwf.utils.BusinessException;
import com.lwf.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class UsersServiceImpl extends ServiceImpl<UsersMapper, Users> implements IUsersService {

    @Autowired
    private NonceService nonceService;

    @Autowired
    private UsersMapper usersMapper;

    @Autowired
    private ProductsMapper productsMapper;

    @Autowired
    private OrdersMapper ordersMapper;

    @Override
    public String generateNonce(String address) {
        return nonceService.generateNonce(address);
    }

    @Override
    public Map<String, Object> signIn(LoginDTO loginDTO) {
        Map<String, Object> result = new HashMap<>();

        // 验证nonce
        if (!nonceService.verifyNonce(loginDTO.getAddress(), loginDTO.getNonce())) {
            throw new BusinessException("Nonce验证失败");
        }

        // 查找或创建用户
        QueryWrapper<Users> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("address", loginDTO.getAddress());
        Users user = this.getOne(queryWrapper);

        if (user == null) {
            user = new Users();
            user.setAddress(loginDTO.getAddress());
            user.setRole("user");
            user.setDisplayName("用户" + loginDTO.getAddress().substring(0, 8));
            user.setReputationScore(10);
            user.setBalance(BigDecimal.ZERO);
            user.setTotalReviews(0);
            user.setShopStatus("none");
            this.save(user);
        }

        // 生成简单token（实际应该用JWT）
//        String token = "token_" + System.currentTimeMillis() + "_" + user.getId();
        String token = JwtUtil.generateToken(user.getId(), user.getAddress(), user.getRole());

        result.put("code", 0);
        result.put("token", token);
        result.put("user", user);
        return result;
    }

    // 其他方法保持不变...
    @Override
    public Map<String, Object> applyShop(String address, String shopName, String shopDescription, String shopLogo) {
        Map<String, Object> result = new HashMap<>();

        QueryWrapper<Users> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("address", address);
        Users user = this.getOne(queryWrapper);

        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        if ("merchant".equals(user.getRole()) && "pending".equals(user.getShopStatus())) {
            throw new BusinessException("您已有待审核的店铺申请");
        }

        user.setRole("merchant");
        user.setShopName(shopName);
        user.setShopDescription(shopDescription);
        user.setShopLogo(shopLogo);
        user.setShopStatus("pending");
        this.updateById(user);

        result.put("code", 0);
        result.put("applicationId", user.getId());
        result.put("status", "pending");
        result.put("message", "申请已提交，等待管理员审核");
        return result;
    }

    @Override
    public Users getShopInfo(String address) {
        QueryWrapper<Users> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("address", address);
        return this.getOne(queryWrapper);
    }

    @Override
    public boolean updateShopInfo(Users users) {
        // 根据地址查询用户是否存在
        QueryWrapper<Users> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("address", users.getAddress());
        Users existingUser = this.getOne(queryWrapper);
        
        if (existingUser == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 更新店铺信息
        existingUser.setShopName(users.getShopName());
        existingUser.setShopDescription(users.getShopDescription());
        existingUser.setShopLogo(users.getShopLogo());
        
        // 使用MyBatis-Plus的updateById方法更新
        return this.updateById(existingUser);
    }

    @Override
    public Map<String, Object> getUserList(Integer page, Integer pageSize) {
        Map<String, Object> result = new HashMap<>();

        Page<Users> pageInfo = new Page<>(page, pageSize);
        QueryWrapper<Users> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("created_at");

        Page<Users> userPage = this.page(pageInfo, queryWrapper);

        result.put("list", userPage.getRecords());
        result.put("total", userPage.getTotal());
        result.put("page", page);
        result.put("pageSize", pageSize);
        result.put("code", 0);

        return result;
    }

    @Override
    public Map<String, Object> getShopApplications(Integer page, Integer pageSize) {
        Map<String, Object> result = new HashMap<>();

        Page<Users> pageInfo = new Page<>(page, pageSize);
        QueryWrapper<Users> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("role", "merchant")
                .eq("shop_status", "pending")
                .orderByDesc("created_at");

        Page<Users> applicationsPage = this.page(pageInfo, queryWrapper);

        result.put("list", applicationsPage.getRecords());
        result.put("total", applicationsPage.getTotal());
        result.put("page", page);
        result.put("pageSize", pageSize);
        result.put("code", 0);

        return result;
    }

    @Override
    public Map<String, Object> auditShopApplication(Long applicationId, String action) {
        Map<String, Object> result = new HashMap<>();

        Users user = this.getById(applicationId);
        if (user == null) {
            throw new BusinessException("申请不存在");
        }

        if ("approve".equals(action)) {
            user.setShopStatus("approved");
            result.put("message", "已通过审核");
        } else if ("reject".equals(action)) {
            user.setShopStatus("rejected");
            result.put("message", "已拒绝申请");
        } else {
            throw new BusinessException("无效的操作类型");
        }

        this.updateById(user);

        result.put("code", 0);
        result.put("id", applicationId);
        result.put("status", user.getShopStatus());
        return result;
    }

    @Override
    public Map<String, Object> getSystemStats() {
        Map<String, Object> result = new HashMap<>();

        // 总用户数
        long totalUsers = this.count();

        // 商家数（已审核通过）
        QueryWrapper<Users> merchantQuery = new QueryWrapper<>();
        merchantQuery.eq("role", "merchant").eq("shop_status", "approved");
        long totalMerchants = this.count(merchantQuery);

        // 商品总数与订单总数（直接使用 mapper，避免服务循环依赖）
        long totalProducts = productsMapper.selectCount(null);
        long totalOrders = ordersMapper.selectCount(null);

        result.put("totalUsers", totalUsers);
        result.put("totalMerchants", totalMerchants);
        result.put("totalProducts", totalProducts);
        result.put("totalOrders", totalOrders);
        result.put("totalTransactions", 0);
        result.put("code", 0);

        return result;
    }

    @Override
    public Map<String, Object> selectMerchantStats() {
        return usersMapper.selectMerchantStats();
    }

    @Override
    public void incrementReviewCount(Long userId) {
        Users user = this.getById(userId);
        if (user != null) {
            user.setTotalReviews(user.getTotalReviews() + 1);
            this.updateById(user);
        }
    }
}