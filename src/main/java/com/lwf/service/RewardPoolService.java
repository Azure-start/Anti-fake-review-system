package com.lwf.service;

import java.lang.Exception;
import java.lang.String;
import java.util.Arrays;
import javax.annotation.PostConstruct;

import com.lwf.model.bo.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.transaction.manager.AssembleTransactionProcessor;
import org.fisco.bcos.sdk.transaction.manager.TransactionProcessorFactory;
import org.fisco.bcos.sdk.transaction.model.dto.CallResponse;
import org.fisco.bcos.sdk.transaction.model.dto.TransactionResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@NoArgsConstructor
@Data
public class RewardPoolService {
  public static final String ABI = com.lwf.utils.IOUtil.readResourceAsString("abi/RewardPool.abi");

  public static final String BINARY = com.lwf.utils.IOUtil.readResourceAsString("bin/ecc/RewardPool.bin");

  public static final String SM_BINARY = com.lwf.utils.IOUtil.readResourceAsString("");

  @Value("${contract.reward-pool}")
  private String address;

  @Autowired
  private Client client;

  AssembleTransactionProcessor txProcessor;

  @PostConstruct
  public void init() throws Exception {
    this.txProcessor = TransactionProcessorFactory.createAssembleTransactionProcessor(this.client,
        this.client.getCryptoSuite().getCryptoKeyPair());
  }

  public TransactionResponse distributeReward(RewardPoolDistributeRewardInputBO input) throws Exception {
    return this.txProcessor.sendTransactionAndGetResponse(this.address, ABI, "distributeReward", input.toArgs());
  }

  public CallResponse totalDistributed() throws Exception {
    return this.txProcessor.sendCall(this.client.getCryptoSuite().getCryptoKeyPair().getAddress(), this.address, ABI,
        "totalDistributed", Arrays.asList());
  }

  public TransactionResponse calculateReward(RewardPoolCalculateRewardInputBO input) throws Exception {
    return this.txProcessor.sendTransactionAndGetResponse(this.address, ABI, "calculateReward", input.toArgs());
  }

  public CallResponse token() throws Exception {
    return this.txProcessor.sendCall(this.client.getCryptoSuite().getCryptoKeyPair().getAddress(), this.address, ABI,
        "token", Arrays.asList());
  }

  public TransactionResponse withdrawTokens(RewardPoolWithdrawTokensInputBO input) throws Exception {
    return this.txProcessor.sendTransactionAndGetResponse(this.address, ABI, "withdrawTokens", input.toArgs());
  }

  public CallResponse reviewCore() throws Exception {
    return this.txProcessor.sendCall(this.client.getCryptoSuite().getCryptoKeyPair().getAddress(), this.address, ABI,
        "reviewCore", Arrays.asList());
  }

  public TransactionResponse setDailyReward(RewardPoolSetDailyRewardInputBO input) throws Exception {
    return this.txProcessor.sendTransactionAndGetResponse(this.address, ABI, "setDailyReward", input.toArgs());
  }

  public CallResponse getPoolBalance() throws Exception {
    return this.txProcessor.sendCall(this.client.getCryptoSuite().getCryptoKeyPair().getAddress(), this.address, ABI,
        "getPoolBalance", Arrays.asList());
  }

  public TransactionResponse setAccessControl(RewardPoolSetAccessControlInputBO input) throws Exception {
    return this.txProcessor.sendTransactionAndGetResponse(this.address, ABI, "setAccessControl", input.toArgs());
  }

  public CallResponse accessControl() throws Exception {
    return this.txProcessor.sendCall(this.client.getCryptoSuite().getCryptoKeyPair().getAddress(), this.address, ABI,
        "accessControl", Arrays.asList());
  }

  public CallResponse dailyRewardAmount() throws Exception {
    return this.txProcessor.sendCall(this.client.getCryptoSuite().getCryptoKeyPair().getAddress(), this.address, ABI,
        "dailyRewardAmount", Arrays.asList());
  }

  public TransactionResponse setReviewCore(RewardPoolSetReviewCoreInputBO input) throws Exception {
    return this.txProcessor.sendTransactionAndGetResponse(this.address, ABI, "setReviewCore", input.toArgs());
  }
}
