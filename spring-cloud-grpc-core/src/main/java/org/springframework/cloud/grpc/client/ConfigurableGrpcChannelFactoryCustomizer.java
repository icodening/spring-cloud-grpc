package org.springframework.cloud.grpc.client;

/**
 * @author icodening
 * @date 2022.07.15
 */
public interface ConfigurableGrpcChannelFactoryCustomizer {

    void customize(ConfigurableGrpcChannelFactory configurableGrpcChannelFactory);

}
