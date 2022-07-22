package org.springframework.cloud.grpc.support;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.MethodDescriptor;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.grpc.client.GrpcChannelManager;
import org.springframework.util.Assert;

/**
 * @author icodening
 * @date 2022.07.21
 */
public class DirectApplicationLoadBalancerInterceptor extends AbstractLoadBalancerGrpcClientInterceptor {

    private final String application;

    private GrpcChannelManager grpcChannelManager;

    public DirectApplicationLoadBalancerInterceptor(String application,
                                                    LoadBalancerClient loadBalancerClient) {
        super(loadBalancerClient);
        Assert.hasText(application, "application name can not be empty");
        this.application = application;
    }

    public DirectApplicationLoadBalancerInterceptor setGrpcChannelManager(GrpcChannelManager grpcChannelManager) {
        Assert.notNull(grpcChannelManager, "grpcChannelManager name can not be null");
        this.grpcChannelManager = grpcChannelManager;
        return this;
    }

    @Override
    protected <RespT, ReqT> GrpcChannelManager determineChannelManager(String application,
                                                                       MethodDescriptor<ReqT, RespT> method,
                                                                       CallOptions callOptions,
                                                                       Channel channel) {
        return grpcChannelManager;
    }

    @Override
    protected <RespT, ReqT> String determineApplication(MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel channel) {
        return application;
    }
}
