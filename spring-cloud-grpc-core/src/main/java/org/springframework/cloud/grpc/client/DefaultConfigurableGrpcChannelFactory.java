package org.springframework.cloud.grpc.client;

import io.grpc.Channel;
import io.grpc.ClientInterceptor;
import io.grpc.ManagedChannelBuilder;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author icodening
 * @date 2022.07.15
 */
public class DefaultConfigurableGrpcChannelFactory implements ConfigurableGrpcChannelFactory {

    private String userAgent;

    private int maxInboundMetadataSize = -1;

    private int maxInboundMessageSize = -1;

    private boolean usePlainText = false;

    private final List<ClientInterceptor> clientInterceptors = new ArrayList<>();

    @Override
    public ConfigurableGrpcChannelFactory usePlainText() {
        this.usePlainText = true;
        return this;
    }

    @Override
    public ConfigurableGrpcChannelFactory userAgent(String userAgent) {
        this.userAgent = userAgent;
        return this;
    }

    @Override
    public ConfigurableGrpcChannelFactory maxInboundMessageSize(int maxInboundMessageSize) {
        this.maxInboundMessageSize = maxInboundMessageSize;
        return this;
    }

    @Override
    public ConfigurableGrpcChannelFactory maxInboundMetadataSize(int maxInboundMetadataSize) {
        this.maxInboundMetadataSize = maxInboundMetadataSize;
        return this;
    }

    @Override
    public ConfigurableGrpcChannelFactory intercept(ClientInterceptor clientInterceptor) {
        clientInterceptors.add(clientInterceptor);
        return this;
    }

    @Override
    public Channel getChannel(String address, int port) {
        Assert.notNull(address, "address can not be null");
        Assert.isTrue(port > 0, "port is illegal");
        ManagedChannelBuilder<?> builder = ManagedChannelBuilder.forAddress(address, port);
        if (maxInboundMessageSize > 0) {
            builder.maxInboundMessageSize(maxInboundMessageSize);
        }
        if (maxInboundMetadataSize > 0) {
            builder.maxInboundMetadataSize(maxInboundMetadataSize);
        }
        if (StringUtils.hasText(userAgent)) {
            builder.userAgent(userAgent);
        }
        if (usePlainText) {
            builder.usePlaintext();
        } else {
            builder.useTransportSecurity();
        }
        if (!clientInterceptors.isEmpty()) {
            builder.intercept(clientInterceptors);
        }
        return builder.build();
    }

}
