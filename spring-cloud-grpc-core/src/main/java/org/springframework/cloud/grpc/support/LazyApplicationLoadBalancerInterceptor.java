package org.springframework.cloud.grpc.support;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.MethodDescriptor;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.grpc.client.GrpcChannelManager;

import java.util.function.Supplier;

/**
 * @author icodening
 * @date 2022.07.21
 */
public class LazyApplicationLoadBalancerInterceptor extends AbstractLoadBalancerGrpcClientInterceptor {

    private final Supplier<String> applicationSupplier;

    private Supplier<GrpcChannelManager> grpcChannelManagerSupplier;

    private volatile String application;

    private volatile GrpcChannelManager grpcChannelManager;

    public LazyApplicationLoadBalancerInterceptor(Supplier<String> applicationSupplier,
                                                  LoadBalancerClient loadBalancerClient) {
        super(loadBalancerClient);
        this.applicationSupplier = applicationSupplier;
    }

    public LazyApplicationLoadBalancerInterceptor setGrpcChannelManagerSupplier(Supplier<GrpcChannelManager> grpcChannelManagerSupplier) {
        this.grpcChannelManagerSupplier = grpcChannelManagerSupplier;
        return this;
    }

    @Override
    protected <RespT, ReqT> String determineApplication(MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel channel) {
        if (application == null) {
            synchronized (this) {
                if (application == null) {
                    application = applicationSupplier.get();
                }
            }
        }
        return application;
    }


    @Override
    protected <RespT, ReqT> GrpcChannelManager determineChannelManager(String application,
                                                                       MethodDescriptor<ReqT, RespT> method,
                                                                       CallOptions callOptions,
                                                                       Channel channel) {
        if (grpcChannelManager == null) {
            synchronized (this) {
                if (grpcChannelManager == null) {
                    grpcChannelManager = grpcChannelManagerSupplier.get();
                }
            }
        }
        return grpcChannelManager;
    }
}
