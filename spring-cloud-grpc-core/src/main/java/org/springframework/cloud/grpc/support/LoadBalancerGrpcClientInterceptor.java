package org.springframework.cloud.grpc.support;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.MethodDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.grpc.client.GrpcChannelManager;

/**
 * @author icodening
 * @date 2022.07.15
 */
public class LoadBalancerGrpcClientInterceptor implements ClientInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoadBalancerGrpcClientInterceptor.class);

    private static final String GRPC_PORT_KEY = "grpc.server.port";

    private final String application;

    private final LoadBalancerClient loadBalancerClient;

    private final GrpcChannelManager channelManager;

    public LoadBalancerGrpcClientInterceptor(String application,
                                             LoadBalancerClient loadBalancerClient,
                                             GrpcChannelManager channelManager) {
        this.application = application;
        this.loadBalancerClient = loadBalancerClient;
        this.channelManager = channelManager;
    }

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel channel) {
        ServiceInstance serviceInstance = loadBalancerClient.choose(application);
        String host = serviceInstance.getHost();
        String port = serviceInstance.getMetadata().get(GRPC_PORT_KEY);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("The result for the [SpringCloudLoadBalancer] is: {}:{}", host, port);
        }
        Channel realChannel = channelManager.getOrCreate(host, Integer.parseInt(port));
        return realChannel.newCall(method, callOptions);
    }
}
