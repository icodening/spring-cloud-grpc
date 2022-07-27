package org.springframework.cloud.grpc.support;

import io.grpc.CallOptions;

/**
 * @author icodening
 * @date 2022.07.24
 */
public class GrpcCallOptions {

    public static final CallOptions.Key<String> APPLICATION = CallOptions.Key.create("application");

    public static final CallOptions.Key<String> SERVICE = CallOptions.Key.create("service");

    public static final CallOptions.Key<String> METHOD = CallOptions.Key.create("method");
}
