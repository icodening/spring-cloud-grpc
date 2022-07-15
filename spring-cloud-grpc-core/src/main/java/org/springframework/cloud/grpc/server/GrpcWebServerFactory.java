package org.springframework.cloud.grpc.server;

/**
 * @author icodening
 * @date 2022.07.14
 */
public interface GrpcWebServerFactory {

    GrpcWebServer getWebServer();
}
