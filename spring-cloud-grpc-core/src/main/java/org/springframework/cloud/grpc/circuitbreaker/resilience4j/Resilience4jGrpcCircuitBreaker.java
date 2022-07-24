package org.springframework.cloud.grpc.circuitbreaker.resilience4j;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.grpc.circuitbreaker.GrpcCircuitBreaker;

import java.util.concurrent.TimeUnit;

/**
 * @author icodening
 * @date 2022.07.24
 */
public class Resilience4jGrpcCircuitBreaker implements GrpcCircuitBreaker {

    private final CircuitBreaker resilience4jCircuitBreaker;

    private volatile long startTime = -1;

    private volatile long elapsedTime = -1;

    public Resilience4jGrpcCircuitBreaker(CircuitBreaker resilience4jCircuitBreaker) {
        this.resilience4jCircuitBreaker = resilience4jCircuitBreaker;
    }

    @Override
    public void onStart() {
        resilience4jCircuitBreaker.acquirePermission();
        startTime = System.currentTimeMillis();
    }

    @Override
    public void onSuccess() {
        onComplete();
        resilience4jCircuitBreaker.onSuccess(elapsedTime, TimeUnit.MILLISECONDS);
    }

    @Override
    public void onError(Throwable throwable) {
        onComplete();
        resilience4jCircuitBreaker.onError(elapsedTime, TimeUnit.MILLISECONDS, throwable);

    }

    private void onComplete() {
        elapsedTime = System.currentTimeMillis() - startTime;
    }
}
