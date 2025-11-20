package com.lwf.entity.dto;


import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class LoginDTO {
    @NotBlank(message = "钱包地址不能为空")
    private String address;

    @NotBlank(message = "签名不能为空")
    private String signature;

    @NotBlank(message = "Nonce不能为空")
    private String nonce;
}