package com.lwf.config;

import java.lang.String;
import lombok.Data;

/**
 * 合约配置类，用于存储各种合约的地址信息
 * 使用@Data注解自动生成getter、setter、toString等方法
 */
@Data
public class ContractConfig {
  // 币天验证器合约地址
  private String coinDayValidatorAddress;

  // 访问控制合约地址
  private String accessControlAddress;

  // 审核核心合约地址
  private String reviewCoreAddress;

  // 测试代币合约地址
  private String testTokenAddress;

  // 审核NFT合约地址
  private String reviewNFTAddress;

  // 奖励池合约地址
  private String rewardPoolAddress;
}
