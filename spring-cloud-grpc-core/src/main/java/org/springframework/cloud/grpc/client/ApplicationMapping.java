package org.springframework.cloud.grpc.client;

/**
 * @author icodening
 * @date 2022.07.20
 */
public interface ApplicationMapping {

    /**
     * get target application name for grpc class
     *
     * @param grpcClass e.g.FooServiceGrpc
     * @return remote application name
     */
    String getApplication(Class<?> grpcClass);
}
