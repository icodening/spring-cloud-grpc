package org.springframework.cloud.grpc.client;

import org.springframework.cloud.grpc.GrpcProperties;

import java.util.Map;

/**
 * @author icodening
 * @date 2022.07.20
 */
public class ConfigurableApplicationFinder implements ApplicationFinder {

    private final GrpcProperties grpcProperties;

    public ConfigurableApplicationFinder(GrpcProperties grpcProperties) {
        this.grpcProperties = grpcProperties;
    }

    @Override
    public String findApplication(String interfaceServiceName) {
        Map<String, String> applications = grpcProperties.getInterfaceApplicationMapping();
        return applications.get(interfaceServiceName);
    }
}
