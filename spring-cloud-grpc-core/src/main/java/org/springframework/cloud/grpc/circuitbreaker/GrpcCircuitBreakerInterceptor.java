package org.springframework.cloud.grpc.circuitbreaker;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall;
import io.grpc.ForwardingClientCallListener;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.Status;

import javax.annotation.Nullable;
import java.util.Optional;

import static org.springframework.cloud.grpc.support.GrpcCallOptions.APPLICATION;
import static org.springframework.cloud.grpc.support.GrpcCallOptions.METHOD;
import static org.springframework.cloud.grpc.support.GrpcCallOptions.SERVICE;

/**
 * @author icodening
 * @date 2022.07.24
 */
public class GrpcCircuitBreakerInterceptor implements ClientInterceptor {

    private final GrpcCircuitBreakerFactory grpcCircuitBreakerFactory;

    public GrpcCircuitBreakerInterceptor(GrpcCircuitBreakerFactory grpcCircuitBreakerFactory) {
        this.grpcCircuitBreakerFactory = grpcCircuitBreakerFactory;
    }

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {
        String application = callOptions.getOption(APPLICATION);
        String serviceName = Optional.ofNullable(callOptions.getOption(SERVICE)).orElse(method.getServiceName());
        String methodName = Optional.ofNullable(callOptions.getOption(METHOD)).orElse(method.getBareMethodName());
        GrpcCircuitBreaker grpcCircuitBreaker = grpcCircuitBreakerFactory.createGrpcCircuitBreaker(application, serviceName + "/" + methodName);
        ClientCall<ReqT, RespT> call = next.newCall(method, callOptions);
        return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(call) {
            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                grpcCircuitBreaker.onStart();
                ForwardingClientCallListener.SimpleForwardingClientCallListener<RespT> listener = new ForwardingClientCallListener.SimpleForwardingClientCallListener<RespT>(responseListener) {
                    @Override
                    public void onClose(Status status, Metadata trailers) {
                        if (status.isOk()) {
                            grpcCircuitBreaker.onSuccess();
                        } else {
                            grpcCircuitBreaker.onError(status.asRuntimeException());
                        }
                        super.onClose(status, trailers);

                    }
                };
                super.start(listener, headers);
            }

            @Override
            public void cancel(@Nullable String message, @Nullable Throwable cause) {
                if (cause != null) {
                    grpcCircuitBreaker.onError(cause);
                }
                super.cancel(message, cause);
            }
        };
    }
}
