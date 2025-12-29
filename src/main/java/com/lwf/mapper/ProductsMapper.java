package com.lwf.mapper;

import com.lwf.entity.Products;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lwf.entity.dto.ProductQueryDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

@Mapper
public interface ProductsMapper extends BaseMapper<Products> {


    // 商品搜索查询
    List<Products> selectProductList(ProductQueryDTO query);

    // 获取商家商品列表
    List<Products> selectByMerchantAddress(
            @Param("merchantAddress") String merchantAddress,
            @Param("sortBy") String sortBy,
            @Param("sortOrder") String sortOrder
    );

    // 获取待审核商品
    List<Products> selectPendingProducts();

    // 更新商品评分
    int updateProductRating(@Param("productId") Long productId, @Param("rating") Double rating);

    // 增加商品销量
    int incrementSales(@Param("productId") Long productId, @Param("quantity") Integer quantity);

    // 减少商品库存
    int decrementStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);

    // 获取商品统计信息
    Map<String, Object> selectProductStats();

    //根据商品ID获取商品详情（包含店铺名称）
    Products getProductById(Long productId);
}