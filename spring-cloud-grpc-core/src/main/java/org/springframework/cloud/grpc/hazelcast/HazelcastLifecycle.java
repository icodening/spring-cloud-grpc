package org.springframework.cloud.grpc.hazelcast;

import com.hazelcast.config.CacheSimpleConfig;
import com.hazelcast.config.Config;
import com.hazelcast.config.DiscoveryStrategyConfig;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.instance.impl.HazelcastInstanceFactory;
import com.hazelcast.spring.cache.HazelcastCacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.SmartLifecycle;
import org.springframework.core.Ordered;
import org.springframework.lang.NonNull;

import java.util.Collections;

/**
 * @author icodening
 * @date 2022.07.22
 */
public class HazelcastLifecycle implements SmartLifecycle,
        ApplicationEventPublisherAware {

    private final HazelcastCacheManager hazelcastCacheManager;

    private ApplicationEventPublisher applicationEventPublisher;

    private DiscoveryClient discoveryClient;

    private Registration registration;

    private HazelcastProperties hazelcastProperties;

    private volatile boolean running = false;

    private volatile HazelcastInstance hazelcastInstance = null;

    public HazelcastLifecycle(HazelcastCacheManager hazelcastCacheManager) {
        this.hazelcastCacheManager = hazelcastCacheManager;
    }

    @Autowired
    public HazelcastLifecycle setDiscoveryClient(DiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
        return this;
    }

    @Autowired
    public HazelcastLifecycle setRegistration(Registration registration) {
        this.registration = registration;
        return this;
    }

    @Autowired
    public HazelcastLifecycle setHazelcastProperties(HazelcastProperties hazelcastProperties) {
        this.hazelcastProperties = hazelcastProperties;
        return this;
    }

    @Override
    public void start() {
        Config config = new Config();
        int hazelcastPort = hazelcastProperties.getPort();
        registration.getMetadata().put(HazelcastConstants.METADATA_SERVER_PORT, String.valueOf(hazelcastPort));
        config.setInstanceName("spring-cloud-grpc");
        config.getNetworkConfig().setPort(hazelcastPort)
                .setPublicAddress(registration.getHost() + ":" + hazelcastPort);
        configurerCache(config);
        configurerDiscovery(config);
        HazelcastInstance grpcHazelcastInstance = HazelcastInstanceFactory.newHazelcastInstance(config);
        hazelcastCacheManager.setHazelcastInstance(grpcHazelcastInstance);
        this.hazelcastInstance = grpcHazelcastInstance;
        this.applicationEventPublisher.publishEvent(new HazelcastInitializedEvent(grpcHazelcastInstance));
        this.running = true;
    }

    private void configurerCache(Config config) {
        CacheSimpleConfig cacheSimpleConfig = new CacheSimpleConfig();
        cacheSimpleConfig.setKeyType(String.class.getName())
                .setValueType(String.class.getName());
        config.getCacheConfigs().put(HazelcastConstants.SERVICE_EXPORT_CACHE_NAME, cacheSimpleConfig);
    }

    private void configurerDiscovery(Config config) {
        config.setProperty("hazelcast.discovery.enabled", "true");
        DiscoveryStrategyConfig scDiscoveryConfig = new DiscoveryStrategyConfig();
        HazelcastSpringCloudDiscoveryFactory hazelcastSpringCloudDiscoveryFactory = new HazelcastSpringCloudDiscoveryFactory(discoveryClient);
        scDiscoveryConfig.setDiscoveryStrategyFactory(hazelcastSpringCloudDiscoveryFactory);
        JoinConfig joinConfig = config.getNetworkConfig().getJoin();
        joinConfig.getDiscoveryConfig()
                .setDiscoveryStrategyConfigs(Collections.singletonList(scDiscoveryConfig));
        joinConfig.getMulticastConfig()
                .setEnabled(false);

    }

    @Override
    public void stop() {
        if (hazelcastInstance != null) {
            hazelcastInstance.shutdown();
        }
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public int getPhase() {
        return Ordered.HIGHEST_PRECEDENCE + 2000;
    }

    @Override
    public void setApplicationEventPublisher(@NonNull ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }
}
