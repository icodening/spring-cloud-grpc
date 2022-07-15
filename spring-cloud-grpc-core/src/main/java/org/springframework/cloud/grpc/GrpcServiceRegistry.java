package org.springframework.cloud.grpc;

/**
 * @author icodening
 * @date 2022.07.14
 */
public interface GrpcServiceRegistry {

    <T> void addService(Class<T> interfaceType, T ref);

    <T> void removeService(Class<T> interfaceType);

    <T> T getService(Class<T> interfaceType);

}
