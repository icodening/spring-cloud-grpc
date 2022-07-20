package org.springframework.cloud.grpc.server;

import io.grpc.ServerServiceDefinition;
import org.springframework.boot.web.server.WebServer;

import java.util.List;

/**
 * @author icodening
 * @date 2022.07.16
 */
public interface GrpcWebServer extends WebServer {

    List<ServerServiceDefinition> getServices();
}
