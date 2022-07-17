package org.springframework.cloud.grpc.server;

import io.grpc.BindableService;
import org.springframework.beans.BeansException;
import org.springframework.cloud.grpc.GrpcProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.NonNull;

import java.util.Map;

/**
 * @author icodening
 * @date 2022.07.14
 */
public class DefaultGrpcServerFactoryCustomizer implements ConfigurableGrpcServerFactoryCustomizer, ApplicationContextAware {

    private final GrpcProperties.Server serverProperties;

    private ApplicationContext applicationContext;

    public DefaultGrpcServerFactoryCustomizer(GrpcProperties.Server serverProperties) {
        this.serverProperties = serverProperties;
    }

    @Override
    public void customize(ConfigurableGrpcServerFactory factory) {
        factory.port(serverProperties.getPort())
                .maxInboundMessageSize((int) serverProperties.getMaxInboundMessageSize().toBytes())
                .corePoolSize(serverProperties.getCorePoolSize())
                .maximumPoolSize(serverProperties.getMaximumPoolSize())
                .threadsQueue(serverProperties.getThreadsQueue());
        if (applicationContext != null) {
            Map<String, BindableService> bindableServiceMap = applicationContext.getBeansOfType(BindableService.class);
            for (BindableService bindableService : bindableServiceMap.values()) {
                factory.addService(bindableService);
            }
        }
    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
