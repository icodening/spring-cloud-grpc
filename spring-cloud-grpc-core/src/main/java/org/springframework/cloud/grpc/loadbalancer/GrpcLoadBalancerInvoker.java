package org.springframework.cloud.grpc.loadbalancer;

import com.google.protobuf.ByteString;
import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.grpc.ExchangerGrpc;
import org.springframework.cloud.grpc.GrpcExchanger;
import org.springframework.cloud.grpc.GrpcMessageSerializer;
import org.springframework.cloud.grpc.client.GrpcChannelManager;
import org.springframework.cloud.grpc.client.GrpcClientInvoker;
import org.springframework.cloud.grpc.internal.GrpcExchangeMetadata;
import org.springframework.cloud.grpc.support.DirectApplicationLoadBalancerInterceptor;
import org.springframework.cloud.grpc.support.GrpcCallOptions;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

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

    private final ExchangerGrpc.ExchangerStub asyncStub;

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
        this.asyncStub = ExchangerGrpc.newStub(fakeChannel)
                .withOption(GrpcCallOptions.APPLICATION, application);
    }

    @Nullable
    @Override
    public Object invoke(@Nonnull MethodInvocation invocation) throws Throwable {
        GrpcExchanger.Request grpcRequest = buildRequest(invocation);
        CompletableFuture<Object> objectCompletableFuture = new CompletableFuture<>();
        asyncStub.withOption(GrpcCallOptions.SERVICE, invocation.getMethod().getDeclaringClass().getSimpleName())
                .withOption(GrpcCallOptions.METHOD, invocation.getMethod().getName())
                .withExecutor(ForkJoinPool.commonPool())
                .exchange(grpcRequest, new StreamObserver<GrpcExchanger.Response>() {
                    @Override
                    public void onNext(GrpcExchanger.Response responseMessage) {
                        String isNullFlag = responseMessage.getMetadataOrDefault(GrpcExchangeMetadata.IS_NULL, Boolean.FALSE.toString());
                        if (Boolean.parseBoolean(isNullFlag)) {
                            objectCompletableFuture.complete(null);
                            return;
                        }
                        String actualType = responseMessage.getMetadataMap().get(GrpcExchangeMetadata.ACTUAL_TYPE);
                        Class<?> type = invocation.getMethod().getReturnType();
                        if (StringUtils.hasText(actualType)) {
                            type = ClassUtils.resolveClassName(actualType, ClassUtils.getDefaultClassLoader());
                        }
                        Object response = grpcMessageSerializer.deserialize(responseMessage.getMessage().toByteArray(), type);
                        objectCompletableFuture.complete(response);
                    }

                    @Override
                    public void onError(Throwable t) {
                        objectCompletableFuture.completeExceptionally(t);
                    }

                    @Override
                    public void onCompleted() {
                        System.out.println("complete");
                    }
                });
        Class<?> returnType = invocation.getMethod().getReturnType();
        if (CompletableFuture.class.isAssignableFrom(returnType)) {
            return objectCompletableFuture;
        }
        try {
            return objectCompletableFuture.get();
        } catch (ExecutionException e) {
            throw e.getCause();
        }
    }

    @Override
    public String application() {
        return this.application;
    }

    private GrpcExchanger.Request buildRequest(MethodInvocation invocation) {
        GrpcExchanger.Request.Builder requestBuilder = GrpcExchanger.Request.newBuilder();
        determineInterface(requestBuilder, invocation);
        determineMethod(requestBuilder, invocation);
        determineParameterMap(requestBuilder, invocation);
        return requestBuilder.build();
    }

    private void determineInterface(GrpcExchanger.Request.Builder request, MethodInvocation invocation) {
        request.setInterfaceType(invocation.getMethod().getDeclaringClass().getName());
    }

    private void determineMethod(GrpcExchanger.Request.Builder requestBuilder, MethodInvocation invocation) {
        requestBuilder.setMethodName(invocation.getMethod().getName());
    }

    private void determineParameterMap(GrpcExchanger.Request.Builder requestBuilder, MethodInvocation invocation) {
        Class<?>[] parameterTypes = invocation.getMethod().getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            byte[] serialized = grpcMessageSerializer.serialize(invocation.getArguments()[i]);
            requestBuilder.putParameters(parameterTypes[i].getName(), ByteString.copyFrom(serialized));
        }
    }
}
