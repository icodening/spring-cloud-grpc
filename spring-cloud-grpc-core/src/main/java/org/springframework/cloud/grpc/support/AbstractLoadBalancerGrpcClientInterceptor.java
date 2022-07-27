package org.springframework.cloud.grpc.support;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall;
import io.grpc.ForwardingClientCallListener;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.CompletionContext;
import org.springframework.cloud.client.loadbalancer.DefaultRequest;
import org.springframework.cloud.client.loadbalancer.DefaultResponse;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.client.loadbalancer.LoadBalancerLifecycle;
import org.springframework.cloud.client.loadbalancer.LoadBalancerLifecycleValidator;
import org.springframework.cloud.grpc.client.GrpcChannelManager;
import org.springframework.cloud.grpc.loadbalancer.GrpcRequestContext;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.util.Assert;

import java.util.Set;

import static org.springframework.cloud.grpc.support.GrpcCallOptions.APPLICATION;

/**
 * @author icodening
 * @date 2022.07.15
 */
@SuppressWarnings("all")
public abstract class AbstractLoadBalancerGrpcClientInterceptor implements ClientInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractLoadBalancerGrpcClientInterceptor.class);

    private static final String GRPC_PORT_KEY = "grpc.server.port";

    private final LoadBalancerClient loadBalancerClient;

    private LoadBalancerClientFactory loadBalancerClientFactory;

    public AbstractLoadBalancerGrpcClientInterceptor(LoadBalancerClient loadBalancerClient) {
        Assert.notNull(loadBalancerClient, "loadBalancerClient can not be null");
        this.loadBalancerClient = loadBalancerClient;
    }

    public AbstractLoadBalancerGrpcClientInterceptor setLoadBalancerClientFactory(LoadBalancerClientFactory loadBalancerClientFactory) {
        this.loadBalancerClientFactory = loadBalancerClientFactory;
        return this;
    }

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel channel) {
        String application = determineApplication(method, callOptions, channel);
        GrpcRequestContext grpcRequestContext = new GrpcRequestContext(application)
                .setServiceName(method.getServiceName())
                .setBareMethodName(method.getBareMethodName());
        DefaultRequest<GrpcRequestContext> lbRequest = new DefaultRequest<>(grpcRequestContext);
        Set<LoadBalancerLifecycle> supportedLifecycleProcessors = getSupportedLifecycleProcessors(application);
        supportedLifecycleProcessors.forEach(lifecycle -> lifecycle.onStart(lbRequest));
        ServiceInstance serviceInstance = loadBalancerClient.choose(application);
        if (serviceInstance == null) {
            throw new IllegalStateException("No instances available for " + application);
        }
        callOptions = callOptions.withOption(APPLICATION, application);
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
            public void sendMessage(ReqT message) {
                supportedLifecycleProcessors.forEach(lifecycle ->
                        lifecycle.onStartRequest(lbRequest, new DefaultResponse(serviceInstance)));
                super.sendMessage(message);
            }

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
                                supportedLifecycleProcessors
                                        .forEach(lifecycle -> lifecycle.onComplete(new CompletionContext<>(CompletionContext.Status.SUCCESS,
                                                lbRequest, new DefaultResponse(serviceInstance), message)));
                            }

                            @Override
                            public void onClose(Status status, Metadata trailers) {
                                if (!status.isOk()) {
                                    status = status.withCause(new RuntimeException(status.getDescription()));
                                }
                                super.onClose(status, trailers);
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


    private Set<LoadBalancerLifecycle> getSupportedLifecycleProcessors(String serviceId) {
        return LoadBalancerLifecycleValidator.getSupportedLifecycleProcessors(
                loadBalancerClientFactory.getInstances(serviceId, LoadBalancerLifecycle.class),
                GrpcRequestContext.class, Object.class, ServiceInstance.class);
    }
}
