package com.lwf.service;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SimpleCacheService {

    private final Map<String, CacheItem> cache = new ConcurrentHashMap<>();

    private static class CacheItem {
        Object value;
        long expireTime;

        CacheItem(Object value, long expireTime) {
            this.value = value;
            this.expireTime = expireTime;
        }
    }

    /**
     * 设置缓存
     */
    public void set(String key, Object value, long expireSeconds) {
        long expireTime = System.currentTimeMillis() + (expireSeconds * 1000);
        cache.put(key, new CacheItem(value, expireTime));

        // 简单清理过期项目
        cleanExpired();
    }

    /**
     * 获取缓存
     */
    public Object get(String key) {
        cleanExpired();

        CacheItem item = cache.get(key);
        if (item != null && System.currentTimeMillis() < item.expireTime) {
            return item.value;
        }

        // 如果过期或不存在，移除并返回null
        cache.remove(key);
        return null;
    }

    /**
     * 删除缓存
     */
    public void delete(String key) {
        cache.remove(key);
    }

    /**
     * 检查是否存在
     */
    public boolean exists(String key) {
        cleanExpired();
        return cache.containsKey(key);
    }

    /**
     * 清理过期缓存
     */
    private void cleanExpired() {
        long currentTime = System.currentTimeMillis();
        cache.entrySet().removeIf(entry ->
                currentTime >= entry.getValue().expireTime
        );
    }
}