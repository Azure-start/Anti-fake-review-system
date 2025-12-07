package com.lwf.service;

import java.lang.Exception;
import java.lang.String;
import java.util.Arrays;
import javax.annotation.PostConstruct;

import com.lwf.model.bo.TestTokenBalanceOfInputBO;
import com.lwf.model.bo.TestTokenTransferInputBO;
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
public class TestTokenService {
  public static final String ABI = com.lwf.utils.IOUtil.readResourceAsString("abi/TestToken.abi");

  public static final String BINARY = com.lwf.utils.IOUtil.readResourceAsString("bin/ecc/TestToken.bin");

  public static final String SM_BINARY = com.lwf.utils.IOUtil.readResourceAsString("");

  @Value("${contract.test-token}")
  private String address;

  @Autowired
  private Client client;

  AssembleTransactionProcessor txProcessor;

  @PostConstruct
  public void init() throws Exception {
    this.txProcessor = TransactionProcessorFactory.createAssembleTransactionProcessor(this.client,
        this.client.getCryptoSuite().getCryptoKeyPair());
  }

  public CallResponse decimals() throws Exception {
    return this.txProcessor.sendCall(this.client.getCryptoSuite().getCryptoKeyPair().getAddress(), this.address, ABI,
        "decimals", Arrays.asList());
  }

  public CallResponse symbol() throws Exception {
    return this.txProcessor.sendCall(this.client.getCryptoSuite().getCryptoKeyPair().getAddress(), this.address, ABI,
        "symbol", Arrays.asList());
  }

  public CallResponse balanceOf(TestTokenBalanceOfInputBO input) throws Exception {
    return this.txProcessor.sendCall(this.client.getCryptoSuite().getCryptoKeyPair().getAddress(), this.address, ABI,
        "balanceOf", input.toArgs());
  }

  public TransactionResponse transfer(TestTokenTransferInputBO input) throws Exception {
    return this.txProcessor.sendTransactionAndGetResponse(this.address, ABI, "transfer", input.toArgs());
  }

  public CallResponse totalSupply() throws Exception {
    return this.txProcessor.sendCall(this.client.getCryptoSuite().getCryptoKeyPair().getAddress(), this.address, ABI,
        "totalSupply", Arrays.asList());
  }

  public TransactionResponse faucet() throws Exception {
    return this.txProcessor.sendTransactionAndGetResponse(this.address, ABI, "faucet", Arrays.asList());
  }

  public CallResponse name() throws Exception {
    return this.txProcessor.sendCall(this.client.getCryptoSuite().getCryptoKeyPair().getAddress(), this.address, ABI,
        "name", Arrays.asList());
  }
}
