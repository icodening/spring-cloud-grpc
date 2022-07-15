package org.springframework.cloud.grpc.client;

import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.beans.BeansException;
import org.springframework.cloud.grpc.GrpcContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.NonNull;

/**
 * @author icodening
 * @date 2022.07.12
 */
public class GrpcClientFactoryBean extends ProxyFactoryBean implements ApplicationContextAware {

    private final String application;

    private ApplicationContext applicationContext;

    public GrpcClientFactoryBean(String application, Class<?> interfaceType) {
        this.application = application;
        this.addInterface(interfaceType);
    }

    @Override
    public Object getObject() throws BeansException {
        preparedGrpcClientProxy(application, applicationContext);
        return super.getObject();
    }

    protected void preparedGrpcClientProxy(String application,
                                           ApplicationContext applicationContext) {
        if (applicationContext == null) {
            return;
        }
        GrpcContext grpcContext = applicationContext.getBean(GrpcContext.class);
        GrpcClientInvoker grpcClientInvoker = grpcContext.getInstance(application, GrpcClientInvoker.class);
        this.addAdvice(grpcClientInvoker);
    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
