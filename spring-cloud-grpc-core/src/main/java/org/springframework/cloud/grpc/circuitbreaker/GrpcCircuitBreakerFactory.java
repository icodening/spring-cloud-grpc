package org.springframework.cloud.grpc.circuitbreaker;

/**
 * @author icodening
 * @date 2022.07.24
 */
public interface GrpcCircuitBreakerFactory {

    GrpcCircuitBreaker createGrpcCircuitBreaker(String application, String fullMethodName);

}
