package org.springframework.cloud.grpc.hazelcast;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;


/**
 * @author icodening
 * @date 2022.07.22
 */
@ConfigurationProperties(prefix = HazelcastProperties.PREFIX)
public class HazelcastProperties {

    public static final String PREFIX = "grpc.hazelcast";

    private boolean enabled = true;

    @Value("${random.int(30000,65535)}")
    private int port = 30000;

    public int getPort() {
        return port;
    }

    public HazelcastProperties setPort(int port) {
        this.port = port;
        return this;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public HazelcastProperties setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }
}
