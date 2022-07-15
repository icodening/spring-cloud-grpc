package org.springframework.cloud.grpc.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.grpc.DefaultGrpcServiceRegistry;
import org.springframework.cloud.grpc.GrpcJacksonMessageSerializer;
import org.springframework.cloud.grpc.GrpcMessageSerializer;
import org.springframework.cloud.grpc.GrpcProperties;
import org.springframework.cloud.grpc.GrpcServiceRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * @author icodening
 * @date 2022.07.14
 */
@Configuration
@EnableConfigurationProperties(GrpcProperties.class)
public class GrpcServiceAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(GrpcMessageSerializer.class)
    @ConditionalOnBean(ObjectMapper.class)
    public GrpcMessageSerializer grpcJacksonMessageSerializer(ObjectMapper objectMapper) {
        return new GrpcJacksonMessageSerializer(objectMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public GrpcServiceRegistry grpcServiceRegistry() {
        return new DefaultGrpcServiceRegistry();
    }

    @Bean
    @ConditionalOnMissingBean
    public GrpcServiceRegistrar grpcServiceRegistrar(ApplicationContext applicationContext) {
        return new GrpcServiceRegistrar(applicationContext);
    }

    @Bean
    public GrpcServerHandler grpcServerHandler(GrpcServiceRegistry grpcServiceRegistry, GrpcMessageSerializer grpcMessageSerializer) {
        return new GrpcServerHandler(grpcServiceRegistry, grpcMessageSerializer);
    }

    @Bean
    @ConditionalOnMissingBean
    public DefaultGrpcWebServerFactory defaultGrpcWebServerFactory() {
        return new DefaultGrpcWebServerFactory();
    }

    @Bean
    @ConditionalOnMissingBean
    public GrpcWebServerLifecycle grpcServerLauncher(GrpcWebServerFactory grpcWebServerFactory) {
        return new GrpcWebServerLifecycle(grpcWebServerFactory.getWebServer());
    }

    @Bean
    @ConditionalOnMissingBean
    public DefaultGrpcServerFactoryCustomizer defaultGrpcServerFactoryCustomizer(GrpcProperties grpcProperties, GrpcServerHandler serverHandler) {
        return new DefaultGrpcServerFactoryCustomizer(grpcProperties.getServer(), serverHandler);
    }

    @Bean
    public SmartInitializingSingleton customizeConfigurableGrpcServer(ConfigurableGrpcServerFactory configurableGrpcServerFactory,
                                                                      List<ConfigurableGrpcServerFactoryCustomizer> customizers) {
        return () -> {
            for (ConfigurableGrpcServerFactoryCustomizer customizer : customizers) {
                customizer.customize(configurableGrpcServerFactory);
            }
        };
    }
}
