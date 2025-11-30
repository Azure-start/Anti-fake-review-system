package com.lwf.controller;

import com.lwf.utils.BusinessException;
import com.lwf.utils.Result;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

/**
 * 全局异常处理器
 * 使用@RestControllerAdvice注解标记该类为全局异常处理类
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     * @param e 业务异常对象
     * @return 返回错误结果，包含错误码和错误信息
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理方法参数校验异常
     * @param e 方法参数校验异常对象
     * @return 返回错误结果，包含详细的校验错误信息
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidationException(MethodArgumentNotValidException e) {
        // 获取校验结果
        BindingResult bindingResult = e.getBindingResult();
        // 获取字段错误列表
        List<FieldError> fieldErrors = bindingResult.getFieldErrors();
        // 构建错误信息字符串
        StringBuilder errorMessage = new StringBuilder();
        for (FieldError error : fieldErrors) {
            errorMessage.append(error.getField()).append(": ").append(error.getDefaultMessage()).append("; ");
        }
        return Result.error(errorMessage.toString());
    }

    /**
     * 处理其他未捕获的异常
     * @param e 异常对象
     * @return 返回通用的系统错误信息
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        // 打印异常堆栈信息，方便排查问题
        e.printStackTrace();
        return Result.error("系统异常，请稍后重试");
    }
}