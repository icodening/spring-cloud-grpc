package org.springframework.cloud.grpc.server;

import io.grpc.BindableService;
import io.grpc.ServerInterceptor;
import org.springframework.boot.web.server.WebServerFactory;

/**
 * @author icodening
 * @date 2022.07.14
 */
public interface ConfigurableGrpcServerFactory extends WebServerFactory {

    ConfigurableGrpcServerFactory port(int port);

    ConfigurableGrpcServerFactory maxInboundMessageSize(int maxInboundMessageSize);

    ConfigurableGrpcServerFactory addService(BindableService bindableService);

    default ConfigurableGrpcServerFactory addService(Iterable<BindableService> bindableServices) {
        bindableServices.forEach(this::addService);
        return this;
    }

    ConfigurableGrpcServerFactory maximumPoolSize(int max);

    ConfigurableGrpcServerFactory corePoolSize(int cores);

    ConfigurableGrpcServerFactory threadsQueue(int queueSize);

    ConfigurableGrpcServerFactory intercept(ServerInterceptor interceptor);

    default ConfigurableGrpcServerFactory intercept(Iterable<ServerInterceptor> interceptors) {
        interceptors.forEach(this::intercept);
        return this;
    }
}
