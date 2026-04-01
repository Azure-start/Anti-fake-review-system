package com.lwf.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Date;

/**
 * MyBatis-Plus配置类
 * 用于配置MyBatis-Plus的分页插件和自动填充功能
 */
@Configuration
public class MyBatisPlusConfig {

    /**
     * 配置MyBatis-Plus的拦截器
     * 添加分页插件，支持MySQL数据库的分页查询
     *
     * @return MybatisPlusInterceptor MyBatis-Plus拦截器实例
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 添加MySQL数据库的分页插件
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }

    /**
     * 配置MyBatis-Plus的自动填充处理器
     * 用于在插入和更新操作时自动填充指定字段
     *
     * @return MetaObjectHandler 自动填充处理器实例
     */
    @Bean
    public MetaObjectHandler metaObjectHandler() {
        return new MetaObjectHandler() {
            /**
             * 插入数据时的填充策略
             * 自动填充createdAt和updatedAt字段为当前时间
             *
             * @param metaObject 元数据对象，包含当前被操作的对象信息
             */
            @Override
            public void insertFill(MetaObject metaObject) {
                // 填充创建时间为当前时间
                this.strictInsertFill(metaObject, "createdAt", Date.class, new Date());
                // 填充更新时间为当前时间
                this.strictInsertFill(metaObject, "updatedAt", Date.class, new Date());
            }

            /**
             * 更新数据时的填充策略
             * 自动填充updatedAt字段为当前时间
             *
             * @param metaObject 元数据对象，包含当前被操作的对象信息
             */
            @Override
            public void updateFill(MetaObject metaObject) {
                // 填充更新时间为当前时间
                this.strictUpdateFill(metaObject, "updatedAt", Date.class, new Date());
            }
        };
    }
}