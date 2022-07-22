package org.springframework.cloud.grpc.hazelcast;

import com.hazelcast.config.properties.PropertyDefinition;
import com.hazelcast.logging.ILogger;
import com.hazelcast.spi.discovery.DiscoveryNode;
import com.hazelcast.spi.discovery.DiscoveryStrategy;
import com.hazelcast.spi.discovery.DiscoveryStrategyFactory;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * @author icodening
 * @date 2022.07.21
 */
public class HazelcastSpringCloudDiscoveryFactory implements DiscoveryStrategyFactory {

    private final DiscoveryClient discoveryClient;

    public HazelcastSpringCloudDiscoveryFactory(DiscoveryClient discoveryClient) {
        Assert.notNull(discoveryClient, "discoveryClient can not be null");
        this.discoveryClient = discoveryClient;
    }

    @Override
    public Class<? extends DiscoveryStrategy> getDiscoveryStrategyType() {
        return HazelcastSpringCloudDiscovery.class;
    }

    @Override
    public DiscoveryStrategy newDiscoveryStrategy(DiscoveryNode discoveryNode, ILogger logger, Map<String, Comparable> properties) {
        return new HazelcastSpringCloudDiscovery(discoveryClient, logger, properties);
    }

    @Override
    public Collection<PropertyDefinition> getConfigurationProperties() {
        return Collections.emptyList();
    }
}
