package org.springframework.cloud.grpc.client;

import io.grpc.Channel;
import io.grpc.ClientInterceptor;

/**
 * @author icodening
 * @date 2022.07.15
 */
public interface ConfigurableGrpcChannelFactory {

    ConfigurableGrpcChannelFactory usePlainText();

    ConfigurableGrpcChannelFactory userAgent(String userAgent);

    ConfigurableGrpcChannelFactory maxInboundMessageSize(int maxInboundMessageSize);

    ConfigurableGrpcChannelFactory maxInboundMetadataSize(int maxInboundMetadataSize);

    ConfigurableGrpcChannelFactory intercept(ClientInterceptor clientInterceptor);

    Channel getChannel(String address, int port);
}
