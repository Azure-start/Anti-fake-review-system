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
public class ReviewCoreService {
  public static final String ABI = com.lwf.utils.IOUtil.readResourceAsString("abi/ReviewCore.abi");

  public static final String BINARY = com.lwf.utils.IOUtil.readResourceAsString("bin/ecc/ReviewCore.bin");

  public static final String SM_BINARY = com.lwf.utils.IOUtil.readResourceAsString("");

  @Value("${contract.review-core}")
  private String address;

  @Autowired
  private Client client;

  AssembleTransactionProcessor txProcessor;

  @PostConstruct
  public void init() throws Exception {
    this.txProcessor = TransactionProcessorFactory.createAssembleTransactionProcessor(this.client,
        this.client.getCryptoSuite().getCryptoKeyPair());
  }

  public CallResponse userReviews(ReviewCoreUserReviewsInputBO input) throws Exception {
    return this.txProcessor.sendCall(this.client.getCryptoSuite().getCryptoKeyPair().getAddress(), this.address, ABI,
        "userReviews", input.toArgs());
  }

  public CallResponse validator() throws Exception {
    return this.txProcessor.sendCall(this.client.getCryptoSuite().getCryptoKeyPair().getAddress(), this.address, ABI,
        "validator", Arrays.asList());
  }

  public CallResponse productReviews(ReviewCoreProductReviewsInputBO input) throws Exception {
    return this.txProcessor.sendCall(this.client.getCryptoSuite().getCryptoKeyPair().getAddress(), this.address, ABI,
        "productReviews", input.toArgs());
  }

  public TransactionResponse setMinContentLength(ReviewCoreSetMinContentLengthInputBO input) throws Exception {
    return this.txProcessor.sendTransactionAndGetResponse(this.address, ABI, "setMinContentLength", input.toArgs());
  }

  public CallResponse minContentLength() throws Exception {
    return this.txProcessor.sendCall(this.client.getCryptoSuite().getCryptoKeyPair().getAddress(), this.address, ABI,
        "minContentLength", Arrays.asList());
  }

  public CallResponse reviewNFT() throws Exception {
    return this.txProcessor.sendCall(this.client.getCryptoSuite().getCryptoKeyPair().getAddress(), this.address, ABI,
        "reviewNFT", Arrays.asList());
  }

  public CallResponse accessControl() throws Exception {
    return this.txProcessor.sendCall(this.client.getCryptoSuite().getCryptoKeyPair().getAddress(), this.address, ABI,
        "accessControl", Arrays.asList());
  }

  public CallResponse totalReviews() throws Exception {
    return this.txProcessor.sendCall(this.client.getCryptoSuite().getCryptoKeyPair().getAddress(), this.address, ABI,
        "totalReviews", Arrays.asList());
  }

  public CallResponse reviews(ReviewCoreReviewsInputBO input) throws Exception {
    return this.txProcessor.sendCall(this.client.getCryptoSuite().getCryptoKeyPair().getAddress(), this.address, ABI,
        "reviews", input.toArgs());
  }

  public CallResponse rewardPool() throws Exception {
    return this.txProcessor.sendCall(this.client.getCryptoSuite().getCryptoKeyPair().getAddress(), this.address, ABI,
        "rewardPool", Arrays.asList());
  }

  public TransactionResponse submitReview(ReviewCoreSubmitReviewInputBO input) throws Exception {
    return this.txProcessor.sendTransactionAndGetResponse(this.address, ABI, "submitReview", input.toArgs());
  }

  public CallResponse getReview(ReviewCoreGetReviewInputBO input) throws Exception {
    return this.txProcessor.sendCall(this.client.getCryptoSuite().getCryptoKeyPair().getAddress(), this.address, ABI,
        "getReview", input.toArgs());
  }

  public CallResponse getProductReviews(ReviewCoreGetProductReviewsInputBO input) throws Exception {
    return this.txProcessor.sendCall(this.client.getCryptoSuite().getCryptoKeyPair().getAddress(), this.address, ABI,
        "getProductReviews", input.toArgs());
  }

  public CallResponse getUserReviews(ReviewCoreGetUserReviewsInputBO input) throws Exception {
    return this.txProcessor.sendCall(this.client.getCryptoSuite().getCryptoKeyPair().getAddress(), this.address, ABI,
        "getUserReviews", input.toArgs());
  }
}
