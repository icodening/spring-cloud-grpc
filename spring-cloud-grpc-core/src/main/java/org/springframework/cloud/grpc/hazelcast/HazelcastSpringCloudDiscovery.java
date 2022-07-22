package org.springframework.cloud.grpc.hazelcast;

import com.hazelcast.cluster.Address;
import com.hazelcast.logging.ILogger;
import com.hazelcast.spi.discovery.AbstractDiscoveryStrategy;
import com.hazelcast.spi.discovery.DiscoveryNode;
import com.hazelcast.spi.discovery.SimpleDiscoveryNode;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author icodening
 * @date 2022.07.21
 */
public class HazelcastSpringCloudDiscovery extends AbstractDiscoveryStrategy {

    private final DiscoveryClient discoveryClient;

    public HazelcastSpringCloudDiscovery(DiscoveryClient discoveryClient,
                                         ILogger logger,
                                         Map<String, Comparable> properties) {
        super(logger, properties);
        this.discoveryClient = discoveryClient;
    }

    @Override
    public Iterable<DiscoveryNode> discoverNodes() {
        List<String> services = discoveryClient.getServices();
        List<DiscoveryNode> discoveryNodes = new ArrayList<>();
        for (String service : services) {
            List<DiscoveryNode> nodes = discoveryClient.getInstances(service)
                    .stream()
                    .filter(this::filter)
                    .map(this::buildDiscoveryNode)
                    .collect(Collectors.toList());
            discoveryNodes.addAll(nodes);
        }
        return discoveryNodes;
    }

    private boolean filter(ServiceInstance serviceInstance) {
        return serviceInstance.getMetadata().containsKey(HazelcastConstants.METADATA_SERVER_PORT);
    }

    private DiscoveryNode buildDiscoveryNode(ServiceInstance serviceInstance) {
        String portString = serviceInstance.getMetadata().get("grpc.hazelcast.port");
        int hazelcastPort = Integer.parseInt(portString);
        Address address = new Address(new InetSocketAddress(serviceInstance.getHost(), hazelcastPort));
        return new SimpleDiscoveryNode(address, serviceInstance.getMetadata());
    }
}
