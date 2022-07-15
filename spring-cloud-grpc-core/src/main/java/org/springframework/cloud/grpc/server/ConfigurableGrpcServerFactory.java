package org.springframework.cloud.grpc.server;

import io.grpc.BindableService;
import org.springframework.boot.web.server.WebServerFactory;

/**
 * @author icodening
 * @date 2022.07.14
 */
public interface ConfigurableGrpcServerFactory extends WebServerFactory {

    ConfigurableGrpcServerFactory port(int port);

    ConfigurableGrpcServerFactory maxInboundMessageSize(int maxInboundMessageSize);

    ConfigurableGrpcServerFactory addService(BindableService bindableService);
}
