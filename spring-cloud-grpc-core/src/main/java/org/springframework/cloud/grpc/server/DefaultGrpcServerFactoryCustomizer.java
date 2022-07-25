package org.springframework.cloud.grpc.server;

import io.grpc.BindableService;
import io.grpc.ServerInterceptor;
import org.springframework.beans.BeansException;
import org.springframework.cloud.grpc.GrpcProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.NonNull;

import java.util.Map;
import java.util.function.Consumer;

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
        consumeBeansByType(BindableService.class, factory::addService);
        consumeBeansByType(ServerInterceptor.class, factory::intercept);
    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    private <T> void consumeBeansByType(Class<T> type, Consumer<Iterable<T>> consumer) {
        if (applicationContext != null) {
            Map<String, T> beanMap = applicationContext.getBeansOfType(type);
            consumer.accept(beanMap.values());
        }
    }
}
