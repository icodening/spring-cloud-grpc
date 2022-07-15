package org.springframework.cloud.grpc.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.grpc.GrpcContext;
import org.springframework.cloud.grpc.GrpcJacksonMessageSerializer;
import org.springframework.cloud.grpc.GrpcMessageSerializer;
import org.springframework.cloud.grpc.GrpcProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * @author icodening
 * @date 2022.07.12
 */
@Configuration
@EnableConfigurationProperties(GrpcProperties.class)
public class GrpcClientAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(GrpcContext.class)
    public GrpcContext grpcContext(List<GrpcClientSpecification> configurations) {
        GrpcContext grpcContext = new GrpcContext();
        grpcContext.setConfigurations(configurations);
        return grpcContext;
    }

    @Bean
    @ConditionalOnMissingBean(GrpcMessageSerializer.class)
    @ConditionalOnBean(ObjectMapper.class)
    public GrpcMessageSerializer grpcJacksonMessageSerializer(ObjectMapper objectMapper) {
        return new GrpcJacksonMessageSerializer(objectMapper);
    }

}
