package com.lwf.config;

import java.lang.String;
import lombok.Data;

@Data
public class ContractConfig {
  private String coinDayValidatorAddress;

  private String accessControlAddress;

  private String reviewCoreAddress;

  private String testTokenAddress;

  private String reviewNFTAddress;

  private String rewardPoolAddress;
}
