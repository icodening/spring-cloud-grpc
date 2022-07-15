package org.springframework.cloud.grpc.server;

import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.context.ApplicationListener;
import org.springframework.lang.NonNull;

/**
 * @author icodening
 * @date 2022.07.15
 */
public class GrpcServerMetadataCustomizer implements ApplicationListener<GrpcServerInitializedEvent> {

    private final Registration registration;

    public GrpcServerMetadataCustomizer(Registration registration) {
        this.registration = registration;
    }

    @Override
    public void onApplicationEvent(@NonNull GrpcServerInitializedEvent event) {
        GrpcWebServer grpcWebServer = event.getSource();
        registration.getMetadata().put("grpc.server.port", String.valueOf(grpcWebServer.getPort()));
    }
}
