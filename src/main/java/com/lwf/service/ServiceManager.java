package com.lwf.service;

import java.lang.Exception;
import java.lang.String;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.PostConstruct;

import com.lwf.service.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import com.lwf.config.SystemConfig;
import org.fisco.bcos.sdk.client.Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
@Slf4j
public class ServiceManager {
	@Autowired
	private SystemConfig config;

	@Autowired
	private Client client;

	List<String> hexPrivateKeyList;

	@PostConstruct
	public void init() {
		hexPrivateKeyList = Arrays.asList(this.config.getHexPrivateKey().split(","));
	}

	/**
	 * @notice: must use @Qualifier("TestTokenService") with @Autowired to get this Bean
	 */
	@Bean("TestTokenService")
	public Map<String, TestTokenService> initTestTokenServiceManager() throws Exception {
		Map<String, TestTokenService> serviceMap = new ConcurrentHashMap<>(this.hexPrivateKeyList.size());
		for (int i = 0; i < this.hexPrivateKeyList.size(); i++) {
			String privateKey = this.hexPrivateKeyList.get(i);
			if (privateKey.startsWith("0x") || privateKey.startsWith("0X")) {
				privateKey = privateKey.substring(2);
			}
			if (privateKey.isEmpty()) {
				continue;
			}
			org.fisco.bcos.sdk.crypto.CryptoSuite cryptoSuite = new org.fisco.bcos.sdk.crypto.CryptoSuite(this.client.getCryptoType());
			org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair cryptoKeyPair = cryptoSuite.createKeyPair(privateKey);
			String userAddress = cryptoKeyPair.getAddress();
			log.info("++++++++hexPrivateKeyList[{}]:{},userAddress:{}", i, privateKey, userAddress);
			TestTokenService testTokenService = new TestTokenService();
			testTokenService.setAddress(this.config.getContract().getTestTokenAddress());
			testTokenService.setClient(this.client);
			org.fisco.bcos.sdk.transaction.manager.AssembleTransactionProcessor txProcessor =
					org.fisco.bcos.sdk.transaction.manager.TransactionProcessorFactory.createAssembleTransactionProcessor(this.client, cryptoKeyPair);
			testTokenService.setTxProcessor(txProcessor);
			serviceMap.put(userAddress, testTokenService);
		}
		log.info("++++++++TestTokenService map:{}", serviceMap);
		return serviceMap;
	}

	/**
	 * @notice: must use @Qualifier("RewardPoolService") with @Autowired to get this Bean
	 */
	@Bean("RewardPoolService")
	public Map<String, RewardPoolService> initRewardPoolServiceManager() throws Exception {
		Map<String, RewardPoolService> serviceMap = new ConcurrentHashMap<>(this.hexPrivateKeyList.size());
		for (int i = 0; i < this.hexPrivateKeyList.size(); i++) {
			String privateKey = this.hexPrivateKeyList.get(i);
			if (privateKey.startsWith("0x") || privateKey.startsWith("0X")) {
				privateKey = privateKey.substring(2);
			}
			if (privateKey.isEmpty()) {
				continue;
			}
			org.fisco.bcos.sdk.crypto.CryptoSuite cryptoSuite = new org.fisco.bcos.sdk.crypto.CryptoSuite(this.client.getCryptoType());
			org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair cryptoKeyPair = cryptoSuite.createKeyPair(privateKey);
			String userAddress = cryptoKeyPair.getAddress();
			log.info("++++++++hexPrivateKeyList[{}]:{},userAddress:{}", i, privateKey, userAddress);
			RewardPoolService rewardPoolService = new RewardPoolService();
			rewardPoolService.setAddress(this.config.getContract().getRewardPoolAddress());
			rewardPoolService.setClient(this.client);
			org.fisco.bcos.sdk.transaction.manager.AssembleTransactionProcessor txProcessor =
					org.fisco.bcos.sdk.transaction.manager.TransactionProcessorFactory.createAssembleTransactionProcessor(this.client, cryptoKeyPair);
			rewardPoolService.setTxProcessor(txProcessor);
			serviceMap.put(userAddress, rewardPoolService);
		}
		log.info("++++++++RewardPoolService map:{}", serviceMap);
		return serviceMap;
	}

	/**
	 * @notice: must use @Qualifier("ReviewNFTService") with @Autowired to get this Bean
	 */
	@Bean("ReviewNFTService")
	public Map<String, ReviewNFTService> initReviewNFTServiceManager() throws Exception {
		Map<String, ReviewNFTService> serviceMap = new ConcurrentHashMap<>(this.hexPrivateKeyList.size());
		for (int i = 0; i < this.hexPrivateKeyList.size(); i++) {
			String privateKey = this.hexPrivateKeyList.get(i);
			if (privateKey.startsWith("0x") || privateKey.startsWith("0X")) {
				privateKey = privateKey.substring(2);
			}
			if (privateKey.isEmpty()) {
				continue;
			}
			org.fisco.bcos.sdk.crypto.CryptoSuite cryptoSuite = new org.fisco.bcos.sdk.crypto.CryptoSuite(this.client.getCryptoType());
			org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair cryptoKeyPair = cryptoSuite.createKeyPair(privateKey);
			String userAddress = cryptoKeyPair.getAddress();
			log.info("++++++++hexPrivateKeyList[{}]:{},userAddress:{}", i, privateKey, userAddress);
			ReviewNFTService reviewNFTService = new ReviewNFTService();
			reviewNFTService.setAddress(this.config.getContract().getReviewNFTAddress());
			reviewNFTService.setClient(this.client);
			org.fisco.bcos.sdk.transaction.manager.AssembleTransactionProcessor txProcessor =
					org.fisco.bcos.sdk.transaction.manager.TransactionProcessorFactory.createAssembleTransactionProcessor(this.client, cryptoKeyPair);
			reviewNFTService.setTxProcessor(txProcessor);
			serviceMap.put(userAddress, reviewNFTService);
		}
		log.info("++++++++ReviewNFTService map:{}", serviceMap);
		return serviceMap;
	}

	/**
	 * @notice: must use @Qualifier("ReviewCoreService") with @Autowired to get this Bean
	 */
	@Bean("ReviewCoreService")
	public Map<String, ReviewCoreService> initReviewCoreServiceManager() throws Exception {
		Map<String, ReviewCoreService> serviceMap = new ConcurrentHashMap<>(this.hexPrivateKeyList.size());
		for (int i = 0; i < this.hexPrivateKeyList.size(); i++) {
			String privateKey = this.hexPrivateKeyList.get(i);
			if (privateKey.startsWith("0x") || privateKey.startsWith("0X")) {
				privateKey = privateKey.substring(2);
			}
			if (privateKey.isEmpty()) {
				continue;
			}
			org.fisco.bcos.sdk.crypto.CryptoSuite cryptoSuite = new org.fisco.bcos.sdk.crypto.CryptoSuite(this.client.getCryptoType());
			org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair cryptoKeyPair = cryptoSuite.createKeyPair(privateKey);
			String userAddress = cryptoKeyPair.getAddress();
			log.info("++++++++hexPrivateKeyList[{}]:{},userAddress:{}", i, privateKey, userAddress);
			ReviewCoreService reviewCoreService = new ReviewCoreService();
			reviewCoreService.setAddress(this.config.getContract().getReviewCoreAddress());
			reviewCoreService.setClient(this.client);
			org.fisco.bcos.sdk.transaction.manager.AssembleTransactionProcessor txProcessor =
					org.fisco.bcos.sdk.transaction.manager.TransactionProcessorFactory.createAssembleTransactionProcessor(this.client, cryptoKeyPair);
			reviewCoreService.setTxProcessor(txProcessor);
			serviceMap.put(userAddress, reviewCoreService);
		}
		log.info("++++++++ReviewCoreService map:{}", serviceMap);
		return serviceMap;
	}

	/**
	 * @notice: must use @Qualifier("CoinDayValidatorService") with @Autowired to get this Bean
	 */
	@Bean("CoinDayValidatorService")
	public Map<String, CoinDayValidatorService> initCoinDayValidatorServiceManager() throws Exception {
		Map<String, CoinDayValidatorService> serviceMap = new ConcurrentHashMap<>(this.hexPrivateKeyList.size());
		for (int i = 0; i < this.hexPrivateKeyList.size(); i++) {
			String privateKey = this.hexPrivateKeyList.get(i);
			if (privateKey.startsWith("0x") || privateKey.startsWith("0X")) {
				privateKey = privateKey.substring(2);
			}
			if (privateKey.isEmpty()) {
				continue;
			}
			org.fisco.bcos.sdk.crypto.CryptoSuite cryptoSuite = new org.fisco.bcos.sdk.crypto.CryptoSuite(this.client.getCryptoType());
			org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair cryptoKeyPair = cryptoSuite.createKeyPair(privateKey);
			String userAddress = cryptoKeyPair.getAddress();
			log.info("++++++++hexPrivateKeyList[{}]:{},userAddress:{}", i, privateKey, userAddress);
			CoinDayValidatorService coinDayValidatorService = new CoinDayValidatorService();
			coinDayValidatorService.setAddress(this.config.getContract().getCoinDayValidatorAddress());
			coinDayValidatorService.setClient(this.client);
			org.fisco.bcos.sdk.transaction.manager.AssembleTransactionProcessor txProcessor =
					org.fisco.bcos.sdk.transaction.manager.TransactionProcessorFactory.createAssembleTransactionProcessor(this.client, cryptoKeyPair);
			coinDayValidatorService.setTxProcessor(txProcessor);
			serviceMap.put(userAddress, coinDayValidatorService);
		}
		log.info("++++++++CoinDayValidatorService map:{}", serviceMap);
		return serviceMap;
	}

	/**
	 * @notice: must use @Qualifier("AccessControlService") with @Autowired to get this Bean
	 */
	@Bean("AccessControlService")
	public Map<String, AccessControlService> initAccessControlServiceManager() throws Exception {
		Map<String, AccessControlService> serviceMap = new ConcurrentHashMap<>(this.hexPrivateKeyList.size());
		for (int i = 0; i < this.hexPrivateKeyList.size(); i++) {
			String privateKey = this.hexPrivateKeyList.get(i);
			if (privateKey.startsWith("0x") || privateKey.startsWith("0X")) {
				privateKey = privateKey.substring(2);
			}
			if (privateKey.isEmpty()) {
				continue;
			}
			org.fisco.bcos.sdk.crypto.CryptoSuite cryptoSuite = new org.fisco.bcos.sdk.crypto.CryptoSuite(this.client.getCryptoType());
			org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair cryptoKeyPair = cryptoSuite.createKeyPair(privateKey);
			String userAddress = cryptoKeyPair.getAddress();
			log.info("++++++++hexPrivateKeyList[{}]:{},userAddress:{}", i, privateKey, userAddress);
			AccessControlService accessControlService = new AccessControlService();
			accessControlService.setAddress(this.config.getContract().getAccessControlAddress());
			accessControlService.setClient(this.client);
			org.fisco.bcos.sdk.transaction.manager.AssembleTransactionProcessor txProcessor =
					org.fisco.bcos.sdk.transaction.manager.TransactionProcessorFactory.createAssembleTransactionProcessor(this.client, cryptoKeyPair);
			accessControlService.setTxProcessor(txProcessor);
			serviceMap.put(userAddress, accessControlService);
		}
		log.info("++++++++AccessControlService map:{}", serviceMap);
		return serviceMap;
	}
}
