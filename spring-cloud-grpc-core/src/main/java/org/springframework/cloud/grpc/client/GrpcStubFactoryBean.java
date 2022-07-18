package org.springframework.cloud.grpc.client;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientInterceptor;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.AbstractStub;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.grpc.GrpcContext;
import org.springframework.cloud.grpc.support.GrpcReactiveClientInterceptor;
import org.springframework.cloud.grpc.support.LoadBalancerGrpcClientInterceptor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.NonNull;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Constructor;

/**
 * @author icodening
 * @date 2022.07.14
 */
public class GrpcStubFactoryBean<T extends AbstractStub<T>> implements FactoryBean<T>, ApplicationContextAware {

    private final String application;

    private final Class<T> stubType;

    private ApplicationContext applicationContext;

    public GrpcStubFactoryBean(String application, Class<T> stubType) {
        this.application = application;
        this.stubType = stubType;
    }

    @Override
    public T getObject() throws Exception {
        AbstractStub.StubFactory<T> stubFactory = new AbstractStub.StubFactory<T>() {
            @Override
            public T newStub(Channel channel, CallOptions callOptions) {
                try {
                    Constructor<T> constructor = ReflectionUtils.accessibleConstructor(stubType, Channel.class, CallOptions.class);
                    return BeanUtils.instantiateClass(constructor, channel, callOptions);
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
            }
        };
        LoadBalancerClient loadBalancerClient = applicationContext.getBean(LoadBalancerClient.class);
        GrpcContext grpcContext = applicationContext.getBean(GrpcContext.class);
        ClientInterceptor interceptor = new GrpcReactiveClientInterceptor(() ->
                new LoadBalancerGrpcClientInterceptor(application,
                        loadBalancerClient,
                        grpcContext.getInstance(application, GrpcChannelManager.class)));
        return AbstractStub.newStub(stubFactory,
                ManagedChannelBuilder.forTarget(application)
                        .intercept(interceptor)
                        .build());
    }

    @Override
    public Class<?> getObjectType() {
        return stubType;
    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
