package org.springframework.cloud.grpc.server;

import org.springframework.boot.web.server.WebServer;

/**
 * @author icodening
 * @date 2022.07.14
 */
public interface GrpcWebServerFactory {

    WebServer getWebServer();
}
