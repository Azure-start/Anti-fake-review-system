package com.lwf.service;

import java.lang.Exception;
import java.lang.String;
import java.util.Arrays;
import javax.annotation.PostConstruct;

import com.lwf.model.bo.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.demo.model.bo.CoinDayValidatorCalculateCoinDaysInputBO;
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
public class CoinDayValidatorService {
  public static final String ABI = com.lwf.utils.IOUtil.readResourceAsString("abi/CoinDayValidator.abi");

  public static final String BINARY = com.lwf.utils.IOUtil.readResourceAsString("bin/ecc/CoinDayValidator.bin");

  public static final String SM_BINARY = com.lwf.utils.IOUtil.readResourceAsString("");

  @Value("${contract.coin-day-validator}")
  private String address;

  @Autowired
  private Client client;

  AssembleTransactionProcessor txProcessor;

  @PostConstruct
  public void init() throws Exception {
    this.txProcessor = TransactionProcessorFactory.createAssembleTransactionProcessor(this.client, this.client.getCryptoSuite().getCryptoKeyPair());
  }

  public TransactionResponse setMinCoinDays(CoinDayValidatorSetMinCoinDaysInputBO input) throws Exception {
    return this.txProcessor.sendTransactionAndGetResponse(this.address, ABI, "setMinCoinDays", input.toArgs());
  }

  public CallResponse calculateCoinDays(CoinDayValidatorCalculateCoinDaysInputBO input) throws Exception {
    return this.txProcessor.sendCall(this.client.getCryptoSuite().getCryptoKeyPair().getAddress(), this.address, ABI, "calculateCoinDays", input.toArgs());
  }

  public CallResponse token() throws Exception {
    return this.txProcessor.sendCall(this.client.getCryptoSuite().getCryptoKeyPair().getAddress(), this.address, ABI, "token", Arrays.asList());
  }

  public CallResponse userData(CoinDayValidatorUserDataInputBO input) throws Exception {
    return this.txProcessor.sendCall(this.client.getCryptoSuite().getCryptoKeyPair().getAddress(), this.address, ABI, "userData", input.toArgs());
  }

  public CallResponse validateCoinDays(CoinDayValidatorValidateCoinDaysInputBO input) throws Exception {
    return this.txProcessor.sendCall(this.client.getCryptoSuite().getCryptoKeyPair().getAddress(), this.address, ABI, "validateCoinDays", input.toArgs());
  }

  public CallResponse reviewCore() throws Exception {
    return this.txProcessor.sendCall(this.client.getCryptoSuite().getCryptoKeyPair().getAddress(), this.address, ABI, "reviewCore", Arrays.asList());
  }

  public TransactionResponse setAccessControl(CoinDayValidatorSetAccessControlInputBO input) throws Exception {
    return this.txProcessor.sendTransactionAndGetResponse(this.address, ABI, "setAccessControl", input.toArgs());
  }

  public CallResponse accessControl() throws Exception {
    return this.txProcessor.sendCall(this.client.getCryptoSuite().getCryptoKeyPair().getAddress(), this.address, ABI, "accessControl", Arrays.asList());
  }

  public CallResponse minCoinDays() throws Exception {
    return this.txProcessor.sendCall(this.client.getCryptoSuite().getCryptoKeyPair().getAddress(), this.address, ABI, "minCoinDays", Arrays.asList());
  }

  public TransactionResponse initUser(CoinDayValidatorInitUserInputBO input) throws Exception {
    return this.txProcessor.sendTransactionAndGetResponse(this.address, ABI, "initUser", input.toArgs());
  }

  public TransactionResponse setReviewCore(CoinDayValidatorSetReviewCoreInputBO input) throws Exception {
    return this.txProcessor.sendTransactionAndGetResponse(this.address, ABI, "setReviewCore", input.toArgs());
  }

  public TransactionResponse burnCoinDays(CoinDayValidatorBurnCoinDaysInputBO input) throws Exception {
    return this.txProcessor.sendTransactionAndGetResponse(this.address, ABI, "burnCoinDays", input.toArgs());
  }
}
