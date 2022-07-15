package org.springframework.cloud.grpc.support;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.MethodDescriptor;
import org.springframework.util.Assert;

import java.util.function.Supplier;

/**
 * @author icodening
 * @date 2022.07.15
 */
public class GrpcReactiveClientInterceptor implements ClientInterceptor {

    private final Supplier<ClientInterceptor> delegateSupplier;

    private volatile ClientInterceptor delegate;

    public GrpcReactiveClientInterceptor(Supplier<ClientInterceptor> delegateSupplier) {
        Assert.notNull(delegateSupplier, "delegate supplier can not be null");
        this.delegateSupplier = delegateSupplier;
    }

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {
        obtainDelegateIfNecessary();
        return delegate.interceptCall(method, callOptions, next);
    }

    private void obtainDelegateIfNecessary() {
        if (delegate == null) {
            synchronized (this) {
                ClientInterceptor clientInterceptor = delegateSupplier.get();
                Assert.notNull(clientInterceptor, "clientInterceptor can not be null");
                this.delegate = clientInterceptor;
            }
        }
    }
}
