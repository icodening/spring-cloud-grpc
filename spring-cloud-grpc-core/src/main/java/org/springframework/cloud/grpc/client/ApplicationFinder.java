package org.springframework.cloud.grpc.client;

/**
 * @author icodening
 * @date 2022.07.20
 */
public interface ApplicationFinder {

    /**
     * get target application name for grpc service name
     *
     * @param interfaceServiceName e.g.FooServiceGrpc.SERVICE_NAME
     * @return remote application name
     */
    String findApplication(String interfaceServiceName);
}
