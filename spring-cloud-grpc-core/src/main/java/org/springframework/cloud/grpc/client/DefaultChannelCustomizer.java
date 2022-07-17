package org.springframework.cloud.grpc.client;

import io.grpc.ClientInterceptor;
import org.springframework.beans.BeansException;
import org.springframework.cloud.grpc.GrpcProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.NonNull;

import java.util.Map;

/**
 * @author icodening
 * @date 2022.07.15
 */
public class DefaultChannelCustomizer implements ConfigurableGrpcChannelFactoryCustomizer, ApplicationContextAware {

    private final GrpcProperties.Client clientProperties;

    private ApplicationContext applicationContext;

    public DefaultChannelCustomizer(GrpcProperties.Client clientProperties) {
        this.clientProperties = clientProperties;
    }

    @Override
    public void customize(ConfigurableGrpcChannelFactory configurableGrpcChannelFactory) {
        configurableGrpcChannelFactory
                .maxInboundMessageSize((int) clientProperties.getMaxInboundMessageSize().toBytes())
                .usePlainText(clientProperties.isUsePlainText());
        if (applicationContext != null) {
            Map<String, ClientInterceptor> clientInterceptorMap = applicationContext.getBeansOfType(ClientInterceptor.class);
            configurableGrpcChannelFactory.intercept(clientInterceptorMap.values());
        }
    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
