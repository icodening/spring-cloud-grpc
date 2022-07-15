package org.springframework.cloud.grpc.client;

import org.springframework.cloud.grpc.GrpcProperties;

/**
 * @author icodening
 * @date 2022.07.15
 */
public class DefaultChannelCustomizer implements ConfigurableGrpcChannelFactoryCustomizer {

    private final GrpcProperties.Client clientProperties;

    public DefaultChannelCustomizer(GrpcProperties.Client clientProperties) {
        this.clientProperties = clientProperties;
    }

    @Override
    public void customize(ConfigurableGrpcChannelFactory configurableGrpcChannelFactory) {
        configurableGrpcChannelFactory
                .maxInboundMessageSize((int) clientProperties.getMaxInboundMessageSize().toBytes());
        if (clientProperties.isUsePlainText()) {
            configurableGrpcChannelFactory.usePlainText();
        }
    }
}
