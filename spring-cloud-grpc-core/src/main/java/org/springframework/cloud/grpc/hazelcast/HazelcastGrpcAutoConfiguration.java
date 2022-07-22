package org.springframework.cloud.grpc.hazelcast;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.spring.cache.HazelcastCacheManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.cloud.grpc.server.GrpcWebServer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author icodening
 * @date 2022.07.21
 */
@Configuration
@EnableConfigurationProperties(HazelcastProperties.class)
@ConditionalOnProperty(prefix = HazelcastProperties.PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnClass({HazelcastInstance.class, HazelcastCacheManager.class})
public class HazelcastGrpcAutoConfiguration {

    @Bean
    public HazelcastCacheManager hazelcastCacheManager() {
        return new HazelcastCacheManager();
    }

    @Bean
    public HazelcastLifecycle postHazelcast(HazelcastCacheManager cacheManager) {
        return new HazelcastLifecycle(cacheManager);
    }

    @Bean
    @ConditionalOnBean({Registration.class})
    public HazelcastGrpcServiceExporter hazelCastGrpcServiceExporter(Registration registration,
                                                                     CacheManager cacheManager,
                                                                     GrpcWebServer grpcWebServer) {
        return new HazelcastGrpcServiceExporter(registration, cacheManager, grpcWebServer);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(HazelcastCacheManager.class)
    public HazelcastApplicationFinder hazelcastApplicationFinder(HazelcastCacheManager hazelcastCacheManager) {
        return new HazelcastApplicationFinder(hazelcastCacheManager);
    }

}
