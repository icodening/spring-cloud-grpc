package org.springframework.cloud.grpc;

import org.springframework.cloud.context.named.NamedContextFactory;
import org.springframework.cloud.grpc.client.GrpcClientDefaultConfiguration;
import org.springframework.cloud.grpc.client.GrpcClientSpecification;

/**
 * @author icodening
 * @date 2022.07.12
 */
public class GrpcContext extends NamedContextFactory<GrpcClientSpecification> {

    public GrpcContext() {
        super(GrpcClientDefaultConfiguration.class, "grpc", "grpc.client.name");
    }
}
