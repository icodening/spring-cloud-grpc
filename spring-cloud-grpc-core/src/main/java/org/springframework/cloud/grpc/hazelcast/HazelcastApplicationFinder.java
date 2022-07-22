package org.springframework.cloud.grpc.hazelcast;

import com.hazelcast.spring.cache.HazelcastCacheManager;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cloud.grpc.client.ApplicationFinder;

/**
 * @author icodening
 * @date 2022.07.21
 */
public class HazelcastApplicationFinder implements ApplicationFinder {

    private volatile Cache cache;

    private final CacheManager cacheManager;

    public HazelcastApplicationFinder(HazelcastCacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Override
    public String findApplication(String interfaceServiceName) {
        initIfNecessary();
        return this.cache.get(interfaceServiceName, String.class);
    }

    private void initIfNecessary() {
        if (this.cache == null) {
            synchronized (this) {
                this.cache = cacheManager.getCache(HazelcastConstants.SERVICE_EXPORT_CACHE_NAME);
            }
        }
    }

}
