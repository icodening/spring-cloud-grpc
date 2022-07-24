package org.springframework.cloud.grpc.circuitbreaker;

import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;

/**
 * @author icodening
 * @date 2022.07.24
 */
public abstract class AbstractGrpcCircuitBreakerFactory<FACTORY extends CircuitBreakerFactory<?, ?>> implements GrpcCircuitBreakerFactory {

    private final FACTORY circuitBreakerFactory;

    public AbstractGrpcCircuitBreakerFactory(FACTORY circuitBreakerFactory) {
        this.circuitBreakerFactory = circuitBreakerFactory;
    }

    public FACTORY getCircuitBreakerFactory() {
        return circuitBreakerFactory;
    }

    @Override
    public GrpcCircuitBreaker createGrpcCircuitBreaker(String application, String fullMethodName) {
        return doCreateGrpcCircuitBreaker(application, fullMethodName);
    }

    protected abstract GrpcCircuitBreaker doCreateGrpcCircuitBreaker(String application, String fullMethodName);
}
