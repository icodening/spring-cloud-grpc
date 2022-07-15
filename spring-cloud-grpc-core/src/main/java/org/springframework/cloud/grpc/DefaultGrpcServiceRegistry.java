package org.springframework.cloud.grpc;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author icodening
 * @date 2022.07.14
 */
public class DefaultGrpcServiceRegistry implements GrpcServiceRegistry {

    private final ConcurrentMap<Class<?>, Object> GRPC_SERVICE_REGISTRY = new ConcurrentHashMap<>(256);

    @Override
    public <T> void addService(Class<T> interfaceType, T ref) {
        GRPC_SERVICE_REGISTRY.put(interfaceType, ref);
    }

    @Override
    public <T> void removeService(Class<T> interfaceType) {
        GRPC_SERVICE_REGISTRY.remove(interfaceType);
    }

    @Override
    public <T> T getService(Class<T> interfaceType) {
        return (T) GRPC_SERVICE_REGISTRY.get(interfaceType);
    }
}
