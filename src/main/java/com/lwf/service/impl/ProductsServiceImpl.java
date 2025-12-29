package com.lwf.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lwf.entity.Products;
import com.lwf.entity.Users;
import com.lwf.entity.dto.ProductQueryDTO;
import com.lwf.mapper.ProductsMapper;
import com.lwf.service.IProductsService;
import com.lwf.service.IUsersService;
import com.lwf.utils.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class ProductsServiceImpl extends ServiceImpl<ProductsMapper, Products> implements IProductsService {

    @Autowired
    private IUsersService usersService;

    @Autowired
    private ProductsMapper productsMapper;

    @Override
    /**
     * 获取商品列表
     * @param query 商品查询条件对象，包含页码、每页大小、关键词、排序字段和排序方式等信息
     * @return 返回一个Map，包含商品列表、总数和状态码
     */
    public Map<String, Object> getProductList(ProductQueryDTO query) {
        // 创建结果Map，用于返回商品列表相关信息
        Map<String, Object> result = new HashMap<>();

        // 创建分页对象，设置当前页码和每页大小
        Page<Products> pageInfo = new Page<>(query.getPage(), query.getPageSize());
        // 创建查询条件构造器
        QueryWrapper<Products> queryWrapper = new QueryWrapper<>();

        // 只查询已审核的商品，设置查询条件
        queryWrapper.eq("status", "onSale");

//        // 如果查询关键词不为空，则添加商品名称和描述的模糊查询条件
        //使用这个会，在进行条件查询时会把不在售的商品查出来
//        if (StringUtils.hasText(query.getKeyword())) {
//            queryWrapper.like("name", query.getKeyword())
//                    .or()
//                    .like("description", query.getKeyword());
//        }

        // 关键词搜索条件
        if (StringUtils.hasText(query.getKeyword())) {
            queryWrapper.and(wrapper -> wrapper
                    .like("name", query.getKeyword())
                    .or()
                    .like("description", query.getKeyword())
            );
        }

        // 排序逻辑处理
        if ("price".equals(query.getSortBy())) {
            // 如果按价格升序排序
            if ("asc".equals(query.getSortOrder())) {
                queryWrapper.orderByAsc("price");
            } else {
                // 否则按价格降序排序
                queryWrapper.orderByDesc("price");
            }
        } else {
            // 默认按创建时间降序排序
            queryWrapper.orderByDesc("created_at");
        }

        // 执行分页查询
        Page<Products> productPage = this.page(pageInfo, queryWrapper);



        // 将查询结果存入结果Map中
        result.put("list", productPage.getRecords());  // 商品列表
        result.put("total", productPage.getTotal());   // 商品总数
        result.put("code", 0);                        // 状态码，0表示成功

        // 返回结果
        return result;
    }

/**
 * 根据商品ID获取商品详情信息
 * @param productId 商品ID
 * @return 商品详细信息对象
 * @throws BusinessException 当商品不存在时抛出业务异常
 */
    @Override
    public Products getProductDetail(Long productId) {
    // 根据商品ID查询商品信息
        Products product = productsMapper.getProductById(productId);
    // 判断商品是否存在，不存在则抛出业务异常
        if (product == null) {
            throw new BusinessException("商品不存在");
        }
    // 返回商品信息
        return product;
    }

    @Override
    public Map<String, Object> createProduct(Products product) {
        Map<String, Object> result = new HashMap<>();

        // 验证商家身份
        Users merchant = usersService.getById(product.getMerchantId());
        if (merchant == null || !"merchant".equals(merchant.getRole()) ||
                !"approved".equals(merchant.getShopStatus())) {
            throw new BusinessException("商家身份未验证或未通过审核");
        }

        product.setMerchantAddress(merchant.getAddress());
        product.setStatus("pending"); // 新商品需要审核
        product.setSales(0);
        product.setRating(BigDecimal.ZERO);

        boolean saved = this.save(product);

        if (saved) {
            result.put("code", 0);
            result.put("id", product.getId());
            result.put("status", "pending");
            result.put("message", "商品已提交，等待审核");
        } else {
            throw new BusinessException("商品创建失败");
        }

        return result;
    }

/**
 * 根据商家地址获取商品列表，支持多条件查询
 * @param merchantAddress 商家地址
 * @param page 当前页码
 * @param pageSize 每页显示数量
 * @param productName 商品名称（可选，支持模糊查询）
 * @param status 商品状态（可选）
 * @param startTime 上架开始时间（可选）
 * @param endTime 上架结束时间（可选）
 * @return 包含商品列表和分页信息的Map对象
 */
    @Override
    public Map<String, Object> getMerchantProducts(String merchantAddress, Integer page, Integer pageSize,
                                                   String productName, String status, String startTime, String endTime) {
    // 创建结果Map对象，用于存储返回的数据
        Map<String, Object> result = new HashMap<>();

    // 创建分页对象，设置当前页码和每页显示数量，用于控制分页查询
        Page<Products> pageInfo = new Page<>(page, pageSize);
    // 创建查询条件构造器，用于构建SQL查询条件
        QueryWrapper<Products> queryWrapper = new QueryWrapper<>();
    // 设置查询条件：商家地址相等，并按创建时间降序排列，确保按最新时间排序
        queryWrapper.eq("merchant_address", merchantAddress);
    
    // 商品名称模糊查询
        if (StringUtils.hasText(productName)) {
            queryWrapper.like("name", productName);
        }
    
    // 商品状态查询
        if (StringUtils.hasText(status)) {
            queryWrapper.eq("status", status);
        }
    
    // 上架时间范围查询
        if (StringUtils.hasText(startTime)) {
            queryWrapper.ge("created_at", startTime);
        }
        if (StringUtils.hasText(endTime)) {
            queryWrapper.le("created_at", endTime);
        }
    
    // 按创建时间降序排序
        queryWrapper.orderByDesc("created_at");

    // 执行分页查询，将分页对象和查询条件传入，获取分页结果
        Page<Products> productPage = this.page(pageInfo, queryWrapper);



    // 将查询结果存入Map，构建返回给前端的数据结构
        result.put("list", productPage.getRecords());  // 商品列表数据
        result.put("total", productPage.getTotal());   // 总记录数，用于前端分页计算
        result.put("page", page);                    // 当前页码，方便前端展示
        result.put("pageSize", pageSize);             // 每页显示数量，用于前端分页控制
        result.put("code", 0);                       // 状态码，0表示成功

    // 返回结果Map
        return result;
    }

    @Override
    public Map<String, Object> getPendingProducts(Integer page, Integer pageSize) {
        Map<String, Object> result = new HashMap<>();

        Page<Products> pageInfo = new Page<>(page, pageSize);
        QueryWrapper<Products> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", "pending")
                .orderByDesc("created_at");

        Page<Products> productPage = this.page(pageInfo, queryWrapper);

        result.put("list", productPage.getRecords());
        result.put("total", productPage.getTotal());
        result.put("page", page);
        result.put("pageSize", pageSize);
        result.put("code", 0);

        return result;
    }

    @Override
    public Map<String, Object> auditProduct(Long productId, String action) {
        Map<String, Object> result = new HashMap<>();

        Products product = this.getById(productId);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }

        if ("approve".equals(action)) {
            product.setStatus("approved");
            result.put("message", "商品已上架");
        } else if ("reject".equals(action)) {
            product.setStatus("rejected");
            result.put("message", "商品已拒绝");
        } else {
            throw new BusinessException("无效的操作类型");
        }

        this.updateById(product);

        result.put("code", 0);
        result.put("id", productId);
        result.put("status", product.getStatus());
        return result;
    }

    @Override
    public Map<String, Object> updateProductStatus(Long productId, String status) {
        Map<String, Object> result = new HashMap<>();

        Products product = this.getById(productId);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }

        product.setStatus(status);
        this.updateById(product);

        result.put("code", 0);
        result.put("id", productId);
        result.put("status", status);
        return result;
    }

    @Override
    public Map<String, Object> updateProduct(Long productId, Products product) {
        Map<String, Object> result = new HashMap<>();

        // 检查商品是否存在
        Products existingProduct = this.getById(productId);
        if (existingProduct == null) {
            throw new BusinessException("商品不存在");
        }

        // 验证商家身份（确保只有商品的所有者可以更新商品）
        Users merchant = usersService.getById(existingProduct.getMerchantId());
        if (merchant == null || !"merchant".equals(merchant.getRole())) {
            throw new BusinessException("商家身份验证失败");
        }

        // 更新商品信息
        existingProduct.setName(product.getName());
        existingProduct.setDescription(product.getDescription());
        existingProduct.setPrice(product.getPrice());
        existingProduct.setImage(product.getImage());
        existingProduct.setImages(product.getImages());
        //规格
        existingProduct.setSpecs(product.getSpecs());
        //库存
        existingProduct.setStock(product.getStock());
        existingProduct.setUpdatedAt(new java.util.Date());
        // 如果商品状态是pending、approved、rejected、onSale、offShelf，更新后需要重新审核
        String currentStatus = existingProduct.getStatus();
        if ("pending".equals(currentStatus) ||
                "approved".equals(currentStatus) ||
                "rejected".equals(currentStatus) ||
                "onSale".equals(currentStatus) ||
                "offShelf".equals(currentStatus)) {
            existingProduct.setStatus("pending");
        }

        boolean updated = this.updateById(existingProduct);

        if (updated) {
            result.put("code", 0);
            result.put("id", productId);
            result.put("status", existingProduct.getStatus());
            result.put("message", "商品更新成功，等待审核");
        } else {
            throw new BusinessException("商品更新失败");
        }

        return result;
    }

    @Override
    public Map<String, Object> deleteProduct(Long productId) {
        Map<String, Object> result = new HashMap<>();

        // 检查商品是否存在
        Products existingProduct = this.getById(productId);
        if (existingProduct == null) {
            throw new BusinessException("商品不存在");
        }

        // 验证商家身份（确保只有商品的所有者可以删除商品）
        Users merchant = usersService.getById(existingProduct.getMerchantId());
        if (merchant == null || !"merchant".equals(merchant.getRole())) {
            throw new BusinessException("商家身份验证失败");
        }

        // 执行删除操作
        boolean deleted = this.removeById(productId);

        if (deleted) {
            result.put("code", 0);
            result.put("id", productId);
            result.put("message", "商品删除成功");
        } else {
            throw new BusinessException("商品删除失败");
        }

        return result;
    }

    @Override
    public Map<String, Object> offShelfProduct(Long productId) {
        Map<String, Object> result = new HashMap<>();

        // 检查商品是否存在
        Products existingProduct = this.getById(productId);
        if (existingProduct == null) {
            throw new BusinessException("商品不存在");
        }

        // 验证商家身份（确保只有商品的所有者可以下架商品）
        Users merchant = usersService.getById(existingProduct.getMerchantId());
        if (merchant == null || !"merchant".equals(merchant.getRole())) {
            throw new BusinessException("商家身份验证失败");
        }

        // 更新商品状态为下架
        existingProduct.setStatus("offShelf");
        existingProduct.setUpdatedAt(new java.util.Date());

        // 执行更新操作
        boolean updated = this.updateById(existingProduct);

        if (updated) {
            result.put("code", 0);
            result.put("id", productId);
            result.put("status", "offShelf");
            result.put("message", "商品已下架");
        } else {
            throw new BusinessException("商品下架失败");
        }

        return result;
    }


    @Override
    public Map<String, Object> onSaleProduct(Long productId) {
        Map<String, Object> result = new HashMap<>();

        // 检查商品是否存在
        Products existingProduct = this.getById(productId);
        if (existingProduct == null) {
            throw new BusinessException("商品不存在");
        }

        // 验证商家身份（确保只有商品的所有者可以下架商品）
        Users merchant = usersService.getById(existingProduct.getMerchantId());
        if (merchant == null || !"merchant".equals(merchant.getRole())) {
            throw new BusinessException("商家身份验证失败");
        }

        // 更新商品状态为上架
        existingProduct.setStatus("onSale");
        existingProduct.setUpdatedAt(new java.util.Date());

        // 执行更新操作
        boolean updated = this.updateById(existingProduct);

        if (updated) {
            result.put("code", 0);
            result.put("id", productId);
            result.put("status", "onSale");
            result.put("message", "商品已上架");
        } else {
            throw new BusinessException("商品上架失败");
        }

        return result;
    }
}