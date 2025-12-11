package com.lwf.config;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.sdk.BcosSDK;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.config.ConfigOption;
import org.fisco.bcos.sdk.config.exceptions.ConfigException;
import org.fisco.bcos.sdk.config.model.ConfigProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class SdkBeanConfig {

    @Autowired
    private SystemConfig config;

    @Bean
    public Client client() throws Exception {
        log.info("=== SdkBeanConfig.client() 开始执行 ===");
        log.info("SystemConfig injected: {}", config != null ? "是" : "否");
        log.info("config.peers: {}", config.getPeers());
        log.info("config.groupId: {}", config.getGroupId());
        log.info("config.certPath: {}", config.getCertPath());

        // 强制设置系统属性禁用国密
        System.setProperty("fisco.ssl.crypto.type", "0");
        System.setProperty("fisco.crypto.type", "0");
        log.info("强制设置系统属性: fisco.ssl.crypto.type=0, fisco.crypto.type=0");

        String certPaths = this.config.getCertPath();
        String[] possibilities = certPaths.split(",|;");
        log.info("Attempting to connect with cert paths: {}", certPaths);
        log.info("Peers configuration: {}", config.getPeers());
        log.info("Group ID: {}", config.getGroupId());

        for (String certPath : possibilities) {
            try {
                log.info("Trying cert path: {}", certPath);
                ConfigProperty property = new ConfigProperty();
                configNetwork(property);
                configCryptoMaterial(property, certPath);

                ConfigOption configOption = new ConfigOption(property);
                log.info("Creating BcosSDK with config: {}", property);
                log.info("ConfigOption cryptoType: {}", configOption.getCryptoMaterialConfig().getSslCryptoType());

                // 检查证书文件是否存在
                try {
                    log.info("检查证书文件是否存在:");
                    log.info("CA证书: {}", this.getClass().getClassLoader().getResource("conf/ca.crt"));
                    log.info("SDK证书: {}", this.getClass().getClassLoader().getResource("conf/sdk.crt"));
                    log.info("SDK私钥: {}", this.getClass().getClassLoader().getResource("conf/sdk.key"));
                } catch (Exception e) {
                    log.error("检查证书文件失败", e);
                }

                // 创建BcosSDK实例，添加详细的错误处理
                BcosSDK bcosSDK;
                try {
                    bcosSDK = new BcosSDK(configOption);
                    log.info("BcosSDK created successfully");
                } catch (Exception e) {
                    log.error("BcosSDK创建失败，错误类型: {}", e.getClass().getSimpleName());
                    log.error("错误消息: {}", e.getMessage());
                    if (e.getCause() != null) {
                        log.error("根本原因: {}", e.getCause().getMessage());
                    }

                    // 检查是否是SSL连接问题
                    if (e.getMessage() != null && e.getMessage().contains("sslContext")) {
                        log.error("SSL连接失败，请检查:");
                        log.error("1. 节点 {} 是否正在运行", config.getPeers());
                        log.error("2. 证书文件是否正确配置");
                        log.error("3. 节点是否配置为ECDSA模式而非国密模式");
                        log.error("4. 网络连接是否正常");
                        log.error("5. 如果节点使用国密模式，请切换到ECDSA模式或使用国密SDK");
                    }

                    throw new Exception("ECDSA SSL连接失败。节点可能配置为国密模式或网络不可达。" +
                            "错误详情: " + e.getMessage() +
                            "。请确保节点支持ECDSA连接或切换到国密SDK。", e);
                }

                // 获取客户端
                Client client = bcosSDK.getClient(config.getGroupId());
                log.info("Client created successfully for group: {}", config.getGroupId());

                // 测试连接
                BigInteger blockNumber = client.getBlockNumber().getBlockNumber();
                log.info("Chain connect successful. Current block number {}", blockNumber);

                configCryptoKeyPair(client);
                log.info("is Gm:{}, address:{}", client.getCryptoSuite().cryptoTypeConfig == 1,
                        client.getCryptoSuite().getCryptoKeyPair().getAddress());
                return client;
            } catch (Exception ex) {
                log.error("Failed to connect with cert path {}: {}", certPath, ex.getMessage(), ex);
                if (certPath.equals(possibilities[possibilities.length - 1])) {
                    // 最后一个路径也失败了，提供更详细的错误信息
                    log.error("所有证书路径尝试失败。请检查以下问题:");
                    log.error("1. 节点 {} 是否正在运行并监听正确端口", config.getPeers());
                    log.error("2. 节点配置是否为ECDSA模式（不是国密模式）");
                    log.error("3. 证书文件是否存在于正确路径: {}", certPath);
                    log.error("4. 网络连接是否正常，端口是否开放");
                    log.error("5. 如果节点使用国密模式，需要使用国密证书和国密SDK配置");
                    throw new ConfigException("Failed to connect to peers:" + config.getPeers() +
                            "。请检查节点是否配置为ECDSA模式，或切换到国密SDK。");
                }
                try {
                    Thread.sleep(5000);
                } catch (Exception e) {
                }
            }
        }
        throw new ConfigException("Failed to connect to peers:" + config.getPeers());
    }

    public void configNetwork(ConfigProperty configProperty) {
        String peerStr = config.getPeers();
        log.info("Configuring network with peers string: {}", peerStr);

        if (peerStr == null || peerStr.trim().isEmpty()) {
            log.error("Peers configuration is null or empty!");
            throw new RuntimeException("Peers configuration is required");
        }

        List<String> peers = Arrays.stream(peerStr.split(",")).collect(Collectors.toList());
        log.info("Parsed peers list: {}", peers);

        Map<String, Object> networkConfig = new HashMap<>();
        networkConfig.put("peers", peers);

        configProperty.setNetwork(networkConfig);
    }

    public void configCryptoMaterial(ConfigProperty configProperty, String certPath) {
        Map<String, Object> cryptoMaterials = new HashMap<>();
        cryptoMaterials.put("certPath", certPath);

        // 添加详细的证书配置 - 只配置ECDSA，不配置国密
        Map<String, Object> certConfig = new HashMap<>();
        certConfig.put("caCert", certPath + "/ca.crt");
        certConfig.put("sslCert", certPath + "/sdk.crt");
        certConfig.put("sslKey", certPath + "/sdk.key");
        cryptoMaterials.putAll(certConfig);

        // 强制设置SSL加密类型为ECDSA（0）
        cryptoMaterials.put("sslCryptoType", 0);

        // 明确设置国密证书为空，防止SDK尝试加载默认国密证书
        cryptoMaterials.put("enSslCert", "");
        cryptoMaterials.put("enSslKey", "");

        // 添加调试信息
        log.info("Crypto material config - 强制ECDSA模式配置: {}", cryptoMaterials);
        log.info("证书路径: CA={}, SDK证书={}, SDK私钥={}",
                cryptoMaterials.get("caCert"),
                cryptoMaterials.get("sslCert"),
                cryptoMaterials.get("sslKey"));

        configProperty.setCryptoMaterial(cryptoMaterials);
    }

    public void configCryptoKeyPair(Client client) {
        if (config.getHexPrivateKey() == null || config.getHexPrivateKey().isEmpty()) {
            client.getCryptoSuite().setCryptoKeyPair(client.getCryptoSuite().createKeyPair());
            return;
        }
        String privateKey;
        if (!config.getHexPrivateKey().contains(",")) {
            privateKey = config.getHexPrivateKey();
        } else {
            String[] list = config.getHexPrivateKey().split(",");
            privateKey = list[0];
        }
        if (privateKey.startsWith("0x") || privateKey.startsWith("0X")) {
            privateKey = privateKey.substring(2);
            config.setHexPrivateKey(privateKey);
        }
        client.getCryptoSuite().setCryptoKeyPair(client.getCryptoSuite().createKeyPair(privateKey));
    }
}
