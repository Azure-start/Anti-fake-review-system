package com.lwf.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;

@Service
public class  NonceService {

    // 使用ConcurrentHashMap作为简单的内存缓存，存储地址与Nonce信息的映射关系
    // 线程安全，适合高并发场景
    private final Map<String, NonceInfo> nonceCache = new ConcurrentHashMap<>();

    // Nonce信息类
    private static class NonceInfo {
        String nonce;
        long timestamp;

        NonceInfo(String nonce, long timestamp) {
            this.nonce = nonce;
            this.timestamp = timestamp;
        }
    }

    /**
     * 生成nonce
     */
    public String generateNonce(String address) {
        // 清理过期nonce
        cleanExpiredNonces();

        String nonce = UUID.randomUUID().toString().substring(0, 8);
        nonceCache.put(address, new NonceInfo(nonce, System.currentTimeMillis()));
        return nonce;
    }

    /**
     * 验证nonce
     */
    public boolean verifyNonce(String address, String nonce) {
        cleanExpiredNonces();

        NonceInfo nonceInfo = nonceCache.get(address);
        if (nonceInfo == null) {
            return false;
        }

        // 验证nonce是否匹配
        boolean isValid = nonceInfo.nonce.equals(nonce);

        // 无论验证是否成功，都移除已使用的nonce
        nonceCache.remove(address);

        return isValid;
    }

    /**
     * 清理过期的nonce（5分钟过期）
     */
    private void cleanExpiredNonces() {
        long currentTime = System.currentTimeMillis();
        long expireTime = 5 * 60 * 1000; // 5分钟

        nonceCache.entrySet().removeIf(entry ->
                currentTime - entry.getValue().timestamp > expireTime
        );
    }
}