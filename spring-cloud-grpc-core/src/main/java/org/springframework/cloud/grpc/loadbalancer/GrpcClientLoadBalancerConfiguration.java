package org.springframework.cloud.grpc.loadbalancer;

import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.grpc.GrpcMessageSerializer;
import org.springframework.cloud.grpc.GrpcProperties;
import org.springframework.cloud.grpc.SimpleGrpcChannelManager;
import org.springframework.cloud.grpc.client.ConfigurableGrpcChannelFactory;
import org.springframework.cloud.grpc.client.ConfigurableGrpcChannelFactoryCustomizer;
import org.springframework.cloud.grpc.client.DefaultChannelCustomizer;
import org.springframework.cloud.grpc.client.DefaultConfigurableGrpcChannelFactory;
import org.springframework.cloud.grpc.client.GrpcChannelManager;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

import java.util.List;

/**
 * @author icodening
 * @date 2022.07.13
 */
public class GrpcClientLoadBalancerConfiguration {

    @Bean
    @ConditionalOnBean(LoadBalancerClient.class)
    public GrpcLoadBalancerInvoker grpcLoadBalancerInterceptor(GrpcMessageSerializer grpcMessageSerializer,
                                                               LoadBalancerClient loadBalancerClient,
                                                               Environment environment, GrpcChannelManager grpcChannelManager) {
        String grpcClientName = environment.getProperty("grpc.client.name");
        return new GrpcLoadBalancerInvoker(grpcClientName, loadBalancerClient, grpcMessageSerializer, grpcChannelManager);
    }

    @Bean
    public ConfigurableGrpcChannelFactory grpcChannelFactory() {
        return new DefaultConfigurableGrpcChannelFactory();
    }

    @Bean
    @ConditionalOnMissingBean
    public GrpcChannelManager defaultGrpcChannelManager(ConfigurableGrpcChannelFactory channelFactory) {
        return new SimpleGrpcChannelManager(channelFactory);
    }

    @Bean
    public ConfigurableGrpcChannelFactoryCustomizer channelFactoryCustomizer(GrpcProperties grpcProperties) {
        return new DefaultChannelCustomizer(grpcProperties.getClient());
    }

    @Bean
    public SmartInitializingSingleton customizeChannel(ConfigurableGrpcChannelFactory configurableGrpcChannelFactory,
                                                       List<ConfigurableGrpcChannelFactoryCustomizer> customizers) {
        return () -> {
            for (ConfigurableGrpcChannelFactoryCustomizer customizer : customizers) {
                customizer.customize(configurableGrpcChannelFactory);
            }
        };
    }

}
