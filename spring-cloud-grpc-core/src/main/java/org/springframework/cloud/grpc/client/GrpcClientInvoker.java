package org.springframework.cloud.grpc.client;

import org.aopalliance.intercept.MethodInterceptor;

/**
 * @author icodening
 * @date 2022.07.12
 */
public interface GrpcClientInvoker extends MethodInterceptor {

    String application();
}
