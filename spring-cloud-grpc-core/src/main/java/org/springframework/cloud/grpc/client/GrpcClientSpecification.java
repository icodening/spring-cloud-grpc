package org.springframework.cloud.grpc.client;

import org.springframework.cloud.context.named.NamedContextFactory;

/**
 * @author icodening
 * @date 2022.07.12
 */
public class GrpcClientSpecification implements NamedContextFactory.Specification {

    private final String name;

    private final Class<?>[] configuration;

    public GrpcClientSpecification(String name, Class<?>[] configuration) {
        this.name = name;
        this.configuration = configuration;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Class<?>[] getConfiguration() {
        return configuration;
    }
}
