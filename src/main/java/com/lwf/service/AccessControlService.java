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
public class AccessControlService {
  public static final String ABI = com.lwf.utils.IOUtil.readResourceAsString("abi/AccessControl.abi");

  public static final String BINARY = com.lwf.utils.IOUtil.readResourceAsString("bin/ecc/AccessControl.bin");

  public static final String SM_BINARY = com.lwf.utils.IOUtil.readResourceAsString("");

  @Value("${contract.access-control}")
  private String address;

  @Autowired
  private Client client;

  AssembleTransactionProcessor txProcessor;

  @PostConstruct
  public void init() throws Exception {
    this.txProcessor = TransactionProcessorFactory.createAssembleTransactionProcessor(this.client,
        this.client.getCryptoSuite().getCryptoKeyPair());
  }

  public CallResponse isAuthorized(AccessControlIsAuthorizedInputBO input) throws Exception {
    return this.txProcessor.sendCall(this.client.getCryptoSuite().getCryptoKeyPair().getAddress(), this.address, ABI,
        "isAuthorized", input.toArgs());
  }

  public CallResponse authorizedContracts(AccessControlAuthorizedContractsInputBO input) throws Exception {
    return this.txProcessor.sendCall(this.client.getCryptoSuite().getCryptoKeyPair().getAddress(), this.address, ABI,
        "authorizedContracts", input.toArgs());
  }

  public TransactionResponse authorizeContract(AccessControlAuthorizeContractInputBO input) throws Exception {
    return this.txProcessor.sendTransactionAndGetResponse(this.address, ABI, "authorizeContract", input.toArgs());
  }

  public CallResponse admin() throws Exception {
    return this.txProcessor.sendCall(this.client.getCryptoSuite().getCryptoKeyPair().getAddress(), this.address, ABI,
        "admin", Arrays.asList());
  }

  public TransactionResponse revokeContract(AccessControlRevokeContractInputBO input) throws Exception {
    return this.txProcessor.sendTransactionAndGetResponse(this.address, ABI, "revokeContract", input.toArgs());
  }

  public TransactionResponse setAdmin(AccessControlSetAdminInputBO input) throws Exception {
    return this.txProcessor.sendTransactionAndGetResponse(this.address, ABI, "setAdmin", input.toArgs());
  }
}
