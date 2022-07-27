package org.springframework.cloud.grpc.server;

import com.google.protobuf.ByteString;
import io.grpc.stub.StreamObserver;
import org.springframework.cloud.grpc.ExchangerGrpc;
import org.springframework.cloud.grpc.GrpcExchanger;
import org.springframework.cloud.grpc.GrpcMessageSerializer;
import org.springframework.cloud.grpc.GrpcServiceRegistry;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * @author icodening
 * @date 2022.07.14
 */
public class GrpcServerHandler extends ExchangerGrpc.ExchangerImplBase {

    private final GrpcServiceRegistry grpcServiceRegistry;

    private final GrpcMessageSerializer grpcMessageSerializer;

    public GrpcServerHandler(GrpcServiceRegistry grpcServiceRegistry, GrpcMessageSerializer grpcMessageSerializer) {
        this.grpcServiceRegistry = grpcServiceRegistry;
        this.grpcMessageSerializer = grpcMessageSerializer;
    }

    /**
     * FIXME refactor and optimize
     */
    @Override
    public void exchange(GrpcExchanger.Request request, StreamObserver<GrpcExchanger.Response> responseObserver) {
        String interfaceTypeString = request.getInterfaceType();
        Class<?> interfaceType = ClassUtils.resolveClassName(interfaceTypeString, ClassUtils.getDefaultClassLoader());
        Object serviceImpl = grpcServiceRegistry.getService(interfaceType);
        if (serviceImpl == null) {
            //service not found
            //TODO
            responseObserver.onError(new RuntimeException("service not found"));
            return;
        }
        String methodName = request.getMethodName();
        Class<?>[] paramTypes = new Class[request.getParametersMap().size()];
        Object[] args = new Object[request.getParametersMap().size()];
        int index = 0;
        for (Map.Entry<String, ByteString> entry : request.getParametersMap().entrySet()) {
            paramTypes[index] = ClassUtils.resolveClassName(entry.getKey(), ClassUtils.getDefaultClassLoader());
            args[index] = grpcMessageSerializer.deserialize(entry.getValue().toByteArray(), paramTypes[index]);
        }
        Method method = ReflectionUtils.findMethod(interfaceType, methodName, paramTypes);
        if (method == null) {
            responseObserver.onError(new RuntimeException("method not found"));
            return;
        }
        Object returnValue = ReflectionUtils.invokeMethod(method, serviceImpl, args);
        if (returnValue == null) {
            responseObserver.onCompleted();
            return;
        }
        if (CompletableFuture.class.isAssignableFrom(method.getReturnType())) {
            CompletableFuture<?> returnCompletableFuture = ((CompletableFuture<?>) returnValue);
            returnCompletableFuture.thenAccept((actualReturnValue) -> {
                GrpcExchanger.Response.Builder responseMessageBuilder = GrpcExchanger.Response.newBuilder();
                if (actualReturnValue == null) {
                    responseObserver.onCompleted();
                    return;
                }
                byte[] data = grpcMessageSerializer.serialize(actualReturnValue);
                GrpcExchanger.Response respMessage = responseMessageBuilder.putMetadata("actualType", actualReturnValue.getClass().getName())
                        .setMessage(ByteString.copyFrom(data)).build();
                responseObserver.onNext(respMessage);
                responseObserver.onCompleted();
            });
            return;
        }
        GrpcExchanger.Response.Builder responseMessageBuilder = GrpcExchanger.Response.newBuilder();
        byte[] data = grpcMessageSerializer.serialize(returnValue);
        GrpcExchanger.Response respMessage = responseMessageBuilder
                .setMessage(ByteString.copyFrom(data)).build();
        responseObserver.onNext(respMessage);
        responseObserver.onCompleted();
    }
}
