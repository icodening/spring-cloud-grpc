package org.springframework.cloud.grpc.support;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall;
import io.grpc.ForwardingClientCallListener;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.grpc.client.GrpcChannelManager;
import org.springframework.util.Assert;

/**
 * @author icodening
 * @date 2022.07.15
 */
public abstract class AbstractLoadBalancerGrpcClientInterceptor implements ClientInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractLoadBalancerGrpcClientInterceptor.class);

    private static final String GRPC_PORT_KEY = "grpc.server.port";

    private final LoadBalancerClient loadBalancerClient;

    public AbstractLoadBalancerGrpcClientInterceptor(LoadBalancerClient loadBalancerClient) {
        Assert.notNull(loadBalancerClient, "loadBalancerClient can not be null");
        this.loadBalancerClient = loadBalancerClient;
    }

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel channel) {
        String application = determineApplication(method, callOptions, channel);
        ServiceInstance serviceInstance = loadBalancerClient.choose(application);
        String host = serviceInstance.getHost();
        String port = serviceInstance.getMetadata().get(GRPC_PORT_KEY);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("The result for the [SpringCloudLoadBalancer] is: {}:{}", host, port);
        }
        GrpcChannelManager channelManager = determineChannelManager(application, method, callOptions, channel);
        Channel realChannel = channelManager.getOrCreate(host, Integer.parseInt(port));
        ClientCall<ReqT, RespT> delegate = realChannel.newCall(method, callOptions);
        return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(delegate) {
            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                ForwardingClientCallListener.SimpleForwardingClientCallListener<RespT> newListener =
                        new ForwardingClientCallListener.SimpleForwardingClientCallListener<RespT>(responseListener) {
                            @Override
                            public void onMessage(RespT message) {
                                super.onMessage(message);
                                if (LOGGER.isDebugEnabled()) {
                                    LOGGER.debug("request success for channel [{}:{}]", host, port);
                                }
                            }
                        };
                super.start(newListener, headers);
            }
        };
    }

    protected abstract <RespT, ReqT> String determineApplication(MethodDescriptor<ReqT, RespT> method,
                                                                 CallOptions callOptions,
                                                                 Channel channel);

    protected abstract <RespT, ReqT> GrpcChannelManager determineChannelManager(String application,
                                                                                MethodDescriptor<ReqT, RespT> method,
                                                                                CallOptions callOptions,
                                                                                Channel channel);

}
