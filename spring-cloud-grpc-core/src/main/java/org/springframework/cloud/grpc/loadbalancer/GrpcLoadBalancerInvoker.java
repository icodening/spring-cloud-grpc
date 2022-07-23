package org.springframework.cloud.grpc.loadbalancer;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.ByteString;
import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.grpc.ExchangerGrpc;
import org.springframework.cloud.grpc.GrpcExchanger;
import org.springframework.cloud.grpc.GrpcMessageSerializer;
import org.springframework.cloud.grpc.client.GrpcChannelManager;
import org.springframework.cloud.grpc.client.GrpcClientInvoker;
import org.springframework.cloud.grpc.support.DirectApplicationLoadBalancerInterceptor;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;

/**
 * @author icodening
 * @date 2022.07.12
 */
public class GrpcLoadBalancerInvoker implements GrpcClientInvoker {

    private final String application;

    private final GrpcMessageSerializer grpcMessageSerializer;

    private final ExchangerGrpc.ExchangerFutureStub futureStub;

    public GrpcLoadBalancerInvoker(String application, LoadBalancerClient loadBalancerClient,
                                   GrpcMessageSerializer grpcMessageSerializer,
                                   GrpcChannelManager grpcChannelManager,
                                   LoadBalancerClientFactory loadBalancerClientFactory) {
        Assert.notNull(application, "application can not be null");
        Assert.notNull(loadBalancerClient, "LoadBalancerClient can not be null");
        Assert.notNull(grpcMessageSerializer, "GrpcMessageConverter can not be null");
        this.application = application;
        this.grpcMessageSerializer = grpcMessageSerializer;
        Channel fakeChannel = ManagedChannelBuilder.forTarget(application)
                .intercept(new DirectApplicationLoadBalancerInterceptor(application, loadBalancerClient)
                        .setGrpcChannelManager(grpcChannelManager)
                        .setLoadBalancerClientFactory(loadBalancerClientFactory))
                .build();
        this.futureStub = ExchangerGrpc.newFutureStub(fakeChannel);
    }

    @Nullable
    @Override
    public Object invoke(@Nonnull MethodInvocation invocation) throws Throwable {
        GrpcExchanger.Message grpcRequest = buildRequest(invocation);
        ListenableFuture<GrpcExchanger.Message> grpcResponse = futureStub.exchange(grpcRequest);
        Class<?> returnType = invocation.getMethod().getReturnType();
        if (CompletableFuture.class.isAssignableFrom(returnType)) {
            CompletableFuture<Object> returnCompletableFuture = new CompletableFuture<>();
            grpcResponse.addListener(new Runnable() {
                @Override
                public void run() {
                    try {
                        //TODO optimize
                        GrpcExchanger.Message responseMessage = grpcResponse.get();
                        String actualType = responseMessage.getMetadataMap().get("actualType");
                        Class<?> type = ClassUtils.resolveClassName(actualType, ClassUtils.getDefaultClassLoader());
                        Object response = grpcMessageSerializer.deserialize(responseMessage.getMessage().toByteArray(), type);
                        returnCompletableFuture.complete(response);
                    } catch (InterruptedException | ExecutionException e) {
                        throw new RuntimeException(e);
                    }
                }
                //TODO customize executor
            }, ForkJoinPool.commonPool());
            return returnCompletableFuture;
        }
        GrpcExchanger.Message responseMessage = grpcResponse.get();
        return grpcMessageSerializer.deserialize(responseMessage.getMessage().toByteArray(), returnType);
    }

    @Override
    public String application() {
        return this.application;
    }

    private GrpcExchanger.Message buildRequest(MethodInvocation invocation) {
        GrpcExchanger.Message.Builder requestBuilder = GrpcExchanger.Message.newBuilder();
        determineInterface(requestBuilder, invocation);
        determineMethod(requestBuilder, invocation);
        determineParameterTypes(requestBuilder, invocation);
        byte[] arguments = grpcMessageSerializer.serialize(invocation.getArguments());
        requestBuilder.setMessage(ByteString.copyFrom(arguments));
        return requestBuilder.build();
    }

    private void determineInterface(GrpcExchanger.Message.Builder request, MethodInvocation invocation) {
        request.setInterfaceType(invocation.getMethod().getDeclaringClass().getName());
    }

    private void determineMethod(GrpcExchanger.Message.Builder requestBuilder, MethodInvocation invocation) {
        requestBuilder.setMethodName(invocation.getMethod().getName());
    }

    private void determineParameterTypes(GrpcExchanger.Message.Builder requestBuilder, MethodInvocation invocation) {
        Class<?>[] parameterTypes = invocation.getMethod().getParameterTypes();
        for (Class<?> parameterType : parameterTypes) {
            requestBuilder.addParameterType(parameterType.getName());
        }
    }
}
