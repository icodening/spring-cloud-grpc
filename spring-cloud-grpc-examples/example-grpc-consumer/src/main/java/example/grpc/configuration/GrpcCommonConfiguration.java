package example.grpc.configuration;

import com.alibaba.csp.sentinel.adapter.grpc.SentinelGrpcClientInterceptor;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall;
import io.grpc.ForwardingClientCallListener;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.Status;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.CompletionContext;
import org.springframework.cloud.client.loadbalancer.LoadBalancerLifecycle;
import org.springframework.cloud.client.loadbalancer.Request;
import org.springframework.cloud.client.loadbalancer.Response;
import org.springframework.cloud.grpc.loadbalancer.GrpcRequestContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author icodening
 * @date 2022.07.23
 */
@Configuration
public class GrpcCommonConfiguration {

    @Bean
    public SentinelGrpcClientInterceptor sentinelGrpcClientInterceptor() {
        return new SentinelGrpcClientInterceptor();
    }

    @Bean
    public ClientInterceptor statisticTimeInterceptor() {
        return new ClientInterceptor() {
            @Override
            public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {
                ClientCall<ReqT, RespT> call = next.newCall(method, callOptions);
                return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(call) {
                    @Override
                    public void start(Listener<RespT> responseListener, Metadata headers) {
                        long start = System.currentTimeMillis();
                        ForwardingClientCallListener.SimpleForwardingClientCallListener<RespT> newListener =
                                new ForwardingClientCallListener.SimpleForwardingClientCallListener<RespT>(responseListener) {

                                    @Override
                                    public void onClose(Status status, Metadata trailers) {
                                        super.onClose(status, trailers);
                                        long end = System.currentTimeMillis();
                                        System.out.println("request cost:" + (end - start) + "ms");
                                    }
                                };
                        super.start(newListener, headers);
                    }
                };
            }
        };
    }

    @Bean
    public LoadBalancerLifecycle<GrpcRequestContext, Object, ServiceInstance> loadBalancerLifecycle() {
        return new LoadBalancerLifecycle<GrpcRequestContext, Object, ServiceInstance>() {
            @Override
            public void onStart(Request<GrpcRequestContext> request) {
                GrpcRequestContext context = request.getContext();
                System.out.println("[onStart] application:" + context.getApplication()
                        + ", service:" + context.getServiceName()
                        + ", method:" + context.getBareMethodName());
            }

            @Override
            public void onStartRequest(Request<GrpcRequestContext> request, Response<ServiceInstance> lbResponse) {
                ServiceInstance server = lbResponse.getServer();
                System.out.println("[onStartRequest] instance:" + server.getHost() + ":" + server.getMetadata().get("grpc.server.port"));
            }

            @Override
            public void onComplete(CompletionContext<Object, ServiceInstance, GrpcRequestContext> completionContext) {
                System.out.println("[onComplete] status:" + completionContext.status().name());
            }
        };
    }
}
