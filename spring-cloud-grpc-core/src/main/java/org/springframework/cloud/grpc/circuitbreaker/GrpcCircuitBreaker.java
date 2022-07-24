package org.springframework.cloud.grpc.circuitbreaker;

/**
 * @author icodening
 * @date 2022.07.24
 */
public interface GrpcCircuitBreaker {

    void onStart();

    void onSuccess();

    void onError(Throwable throwable);
}
