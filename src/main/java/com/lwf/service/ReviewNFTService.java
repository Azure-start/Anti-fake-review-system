package com.lwf.service;

import com.lwf.model.bo.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.lwf.model.bo.*;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.transaction.manager.AssembleTransactionProcessor;
import org.fisco.bcos.sdk.transaction.manager.TransactionProcessorFactory;
import org.fisco.bcos.sdk.transaction.model.dto.CallResponse;
import org.fisco.bcos.sdk.transaction.model.dto.TransactionResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Arrays;

@Service
@NoArgsConstructor
@Data
public class ReviewNFTService {
  public static final String ABI = com.lwf.utils.IOUtil.readResourceAsString("abi/ReviewNFT.abi");

  public static final String BINARY = com.lwf.utils.IOUtil.readResourceAsString("bin/ecc/ReviewNFT.bin");

  public static final String SM_BINARY = com.lwf.utils.IOUtil.readResourceAsString("");

  @Value("${contract.review-nft}")
  private String address;

  @Autowired
  private Client client;

  AssembleTransactionProcessor txProcessor;

  @PostConstruct
  public void init() throws Exception {
    this.txProcessor = TransactionProcessorFactory.createAssembleTransactionProcessor(this.client,
        this.client.getCryptoSuite().getCryptoKeyPair());
  }

  public TransactionResponse mint(ReviewNFTMintInputBO input) throws Exception {
    return this.txProcessor.sendTransactionAndGetResponse(this.address, ABI, "mint", input.toArgs());
  }

  public CallResponse symbol() throws Exception {
    return this.txProcessor.sendCall(this.client.getCryptoSuite().getCryptoKeyPair().getAddress(), this.address, ABI,
        "symbol", Arrays.asList());
  }

  public CallResponse getNFT(ReviewNFTGetNFTInputBO input) throws Exception {
    return this.txProcessor.sendCall(this.client.getCryptoSuite().getCryptoKeyPair().getAddress(), this.address, ABI,
        "getNFT", input.toArgs());
  }

  public CallResponse nfts(ReviewNFTNftsInputBO input) throws Exception {
    return this.txProcessor.sendCall(this.client.getCryptoSuite().getCryptoKeyPair().getAddress(), this.address, ABI,
        "nfts", input.toArgs());
  }

  public CallResponse totalSupply() throws Exception {
    return this.txProcessor.sendCall(this.client.getCryptoSuite().getCryptoKeyPair().getAddress(), this.address, ABI,
        "totalSupply", Arrays.asList());
  }

  public CallResponse getUserTokens(ReviewNFTGetUserTokensInputBO input) throws Exception {
    return this.txProcessor.sendCall(this.client.getCryptoSuite().getCryptoKeyPair().getAddress(), this.address, ABI,
        "getUserTokens", input.toArgs());
  }

  public CallResponse reviewCore() throws Exception {
    return this.txProcessor.sendCall(this.client.getCryptoSuite().getCryptoKeyPair().getAddress(), this.address, ABI,
        "reviewCore", Arrays.asList());
  }

  public CallResponse name() throws Exception {
    return this.txProcessor.sendCall(this.client.getCryptoSuite().getCryptoKeyPair().getAddress(), this.address, ABI,
        "name", Arrays.asList());
  }

  public TransactionResponse setAccessControl(ReviewNFTSetAccessControlInputBO input) throws Exception {
    return this.txProcessor.sendTransactionAndGetResponse(this.address, ABI, "setAccessControl", input.toArgs());
  }

  public CallResponse accessControl() throws Exception {
    return this.txProcessor.sendCall(this.client.getCryptoSuite().getCryptoKeyPair().getAddress(), this.address, ABI,
        "accessControl", Arrays.asList());
  }

  public TransactionResponse setReviewCore(ReviewNFTSetReviewCoreInputBO input) throws Exception {
    return this.txProcessor.sendTransactionAndGetResponse(this.address, ABI, "setReviewCore", input.toArgs());
  }

  public CallResponse userTokens(ReviewNFTUserTokensInputBO input) throws Exception {
    return this.txProcessor.sendCall(this.client.getCryptoSuite().getCryptoKeyPair().getAddress(), this.address, ABI,
        "userTokens", input.toArgs());
  }
}
