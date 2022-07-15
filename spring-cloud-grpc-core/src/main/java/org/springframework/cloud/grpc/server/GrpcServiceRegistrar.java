package org.springframework.cloud.grpc.server;

import io.grpc.BindableService;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.cloud.grpc.GrpcServiceRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author icodening
 * @date 2022.07.14
 */
public class GrpcServiceRegistrar implements SmartInitializingSingleton {

    private final ApplicationContext applicationContext;

    public GrpcServiceRegistrar(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterSingletonsInstantiated() {
        GrpcServiceRegistry grpcServiceRegistry = this.applicationContext.getBean(GrpcServiceRegistry.class);
        Map<String, Object> serviceBeans = this.applicationContext.getBeansWithAnnotation(Service.class);
        registerService(grpcServiceRegistry, serviceBeans);
    }

    private void registerService(GrpcServiceRegistry grpcServiceRegistry, Map<String, Object> serviceBeans) {
        for (Object serviceImpl : serviceBeans.values()) {
            if (serviceImpl instanceof BindableService) {
                continue;
            }
            Class<?> serviceType = serviceImpl.getClass();
            for (Class intf : serviceType.getInterfaces()) {
                grpcServiceRegistry.addService(intf, serviceImpl);
            }
        }
    }
}
