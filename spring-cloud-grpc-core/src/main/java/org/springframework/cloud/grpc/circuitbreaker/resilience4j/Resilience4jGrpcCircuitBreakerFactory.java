package org.springframework.cloud.grpc.circuitbreaker.resilience4j;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.cloud.grpc.circuitbreaker.AbstractGrpcCircuitBreakerFactory;
import org.springframework.cloud.grpc.circuitbreaker.GrpcCircuitBreaker;

/**
 * @author icodening
 * @date 2022.07.24
 */
public class Resilience4jGrpcCircuitBreakerFactory
        extends AbstractGrpcCircuitBreakerFactory<Resilience4JCircuitBreakerFactory> {

    public Resilience4jGrpcCircuitBreakerFactory(Resilience4JCircuitBreakerFactory circuitBreakerFactory) {
        super(circuitBreakerFactory);
    }

    @Override
    protected GrpcCircuitBreaker doCreateGrpcCircuitBreaker(String application, String fullMethodName) {
        CircuitBreakerRegistry circuitBreakerRegistry = getCircuitBreakerFactory().getCircuitBreakerRegistry();
        String key = application + "/" + fullMethodName;
        CircuitBreakerConfig config = circuitBreakerRegistry.getConfiguration(key).orElse(CircuitBreakerConfig.ofDefaults());
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(key, config);
        return new Resilience4jGrpcCircuitBreaker(circuitBreaker);
    }
}
