package org.springframework.cloud.grpc.server;

import io.grpc.BindableService;
import io.grpc.ServerBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * @author icodening
 * @date 2022.07.14
 */
public class DefaultGrpcWebServerFactory implements ConfigurableGrpcServerFactory, GrpcWebServerFactory {

    private int port;

    private int maxInboundMessageSize;

    private final List<BindableService> bindableServices = new ArrayList<>();

    @Override
    public ConfigurableGrpcServerFactory port(int port) {
        this.port = port;
        return this;
    }

    @Override
    public ConfigurableGrpcServerFactory maxInboundMessageSize(int maxInboundMessageSize) {
        this.maxInboundMessageSize = maxInboundMessageSize;
        return this;
    }

    @Override
    public ConfigurableGrpcServerFactory addService(BindableService bindableService) {
        bindableServices.add(bindableService);
        return this;
    }

    @Override
    public GrpcWebServer getWebServer() {
        ServerBuilder<?> serverBuilder = ServerBuilder.forPort(port)
                .maxInboundMessageSize(maxInboundMessageSize);
        for (BindableService bindableService : bindableServices) {
            serverBuilder = serverBuilder.addService(bindableService);
        }
        return new DefaultGrpcWebServer(serverBuilder.build());
    }
}
