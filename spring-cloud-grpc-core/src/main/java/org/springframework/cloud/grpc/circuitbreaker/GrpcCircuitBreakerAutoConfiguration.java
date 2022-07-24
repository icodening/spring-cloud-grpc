package org.springframework.cloud.grpc.circuitbreaker;

import com.alibaba.cloud.sentinel.custom.SentinelAutoConfiguration;
import com.alibaba.csp.sentinel.adapter.grpc.SentinelGrpcClientInterceptor;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JAutoConfiguration;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.cloud.grpc.circuitbreaker.resilience4j.Resilience4jGrpcCircuitBreakerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author icodening
 * @date 2022.07.24
 */
@Configuration
public class GrpcCircuitBreakerAutoConfiguration {

    @Bean
    @ConditionalOnBean(GrpcCircuitBreakerFactory.class)
    public GrpcCircuitBreakerInterceptor grpcCircuitBreakerClientInterceptor(GrpcCircuitBreakerFactory grpcCircuitBreakerFactory) {
        return new GrpcCircuitBreakerInterceptor(grpcCircuitBreakerFactory);
    }


    @ConditionalOnClass(Resilience4JAutoConfiguration.class)
    @AutoConfigureAfter(Resilience4JAutoConfiguration.class)
    public static class GrpcResilience4jCircuitBreakerAutoConfiguration {

        @Bean
        @ConditionalOnClass(Resilience4JCircuitBreakerFactory.class)
        @ConditionalOnBean(Resilience4JCircuitBreakerFactory.class)
        public GrpcCircuitBreakerFactory resilience4jGrpcCircuitBreakerFactory(Resilience4JCircuitBreakerFactory resilience4JCircuitBreakerFactory) {
            return new Resilience4jGrpcCircuitBreakerFactory(resilience4JCircuitBreakerFactory);
        }
    }

    @ConditionalOnClass({SentinelGrpcClientInterceptor.class, SentinelAutoConfiguration.class})
    @ConditionalOnProperty(name = "spring.cloud.sentinel.enabled", matchIfMissing = true)
    public static class GrpcSentinelCircuitBreakerAutoConfiguration {

        @Bean
        @ConditionalOnMissingBean(GrpcCircuitBreakerInterceptor.class)
        public SentinelGrpcClientInterceptor sentinelGrpcClientInterceptor() {
            return new SentinelGrpcClientInterceptor();
        }
    }

}
