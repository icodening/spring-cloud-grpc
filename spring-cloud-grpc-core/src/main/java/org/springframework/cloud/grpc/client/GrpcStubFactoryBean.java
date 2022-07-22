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
import org.springframework.cloud.grpc.support.LazyApplicationLoadBalancerInterceptor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.lang.NonNull;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * @author icodening
 * @date 2022.07.14
 */
public class GrpcStubFactoryBean<T extends AbstractStub<T>> implements FactoryBean<T>, ApplicationContextAware {

    private static final String SERVICE_NAME_FIELD = "SERVICE_NAME";

    private final Class<T> stubType;

    private ApplicationContext applicationContext;

    public GrpcStubFactoryBean(Class<T> stubType) {
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
        String serviceName = getServiceName();
        LoadBalancerClient loadBalancerClient = applicationContext.getBean(LoadBalancerClient.class);
        Supplier<String> applicationSupplier = applicationSupplier();
        ClientInterceptor interceptor = new GrpcReactiveClientInterceptor(() ->
                new LazyApplicationLoadBalancerInterceptor(applicationSupplier, loadBalancerClient)
                        .setGrpcChannelManagerSupplier(grpcChannelManagerSupplier(applicationSupplier)));
        return AbstractStub.newStub(stubFactory,
                ManagedChannelBuilder.forTarget(serviceName)
                        .intercept(interceptor)
                        .build());
    }

    private Supplier<String> applicationSupplier() {
        return () -> {
            try {
                String serviceName = getServiceName();
                return findApplication(serviceName);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    private Supplier<GrpcChannelManager> grpcChannelManagerSupplier(Supplier<String> applicationSupplier) {
        return () -> {
            String application = applicationSupplier.get();
            GrpcContext grpcContext = applicationContext.getBean(GrpcContext.class);
            return grpcContext.getInstance(application, GrpcChannelManager.class);
        };
    }

    private String getServiceName() throws Exception {
        String stubTypeName = stubType.getName();
        String grpcClassName = stubTypeName.substring(0, stubTypeName.indexOf("$"));
        Class<?> grpcClass = ClassUtils.resolveClassName(grpcClassName, ClassUtils.getDefaultClassLoader());
        return (String) grpcClass.getDeclaredField(SERVICE_NAME_FIELD).get(null);
    }

    private String findApplication(String interfaceServiceName) throws Exception {
        Map<String, ApplicationFinder> finderMap = applicationContext.getBeansOfType(ApplicationFinder.class);
        List<ApplicationFinder> finders = new ArrayList<>(finderMap.values());
        AnnotationAwareOrderComparator.sort(finders);
        String application = null;
        for (ApplicationFinder applicationFinder : finders) {
            application = applicationFinder.findApplication(interfaceServiceName);
            if (StringUtils.hasText(application)) {
                break;
            }
        }
        return application;
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
