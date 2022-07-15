package org.springframework.cloud.grpc.server;

import org.springframework.context.ApplicationEvent;

/**
 * @author icodening
 * @date 2022.07.16
 */
public class GrpcServerInitializedEvent extends ApplicationEvent {

    public GrpcServerInitializedEvent(GrpcWebServer source) {
        super(source);
    }

    @Override
    public GrpcWebServer getSource() {
        return (GrpcWebServer) super.getSource();
    }
}
