package org.springframework.cloud.grpc.server;

import io.grpc.Server;
import io.grpc.ServerServiceDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.server.GracefulShutdownCallback;
import org.springframework.boot.web.server.WebServerException;

import java.io.IOException;
import java.util.List;

/**
 * @author icodening
 * @date 2022.07.14
 */
public class DefaultGrpcWebServer implements GrpcWebServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultGrpcWebServer.class);

    private final Server grpcServer;

    public DefaultGrpcWebServer(Server grpcServer) {
        this.grpcServer = grpcServer;
    }

    @Override
    public void start() throws WebServerException {
        try {
            grpcServer.start();
            Thread holder = new Thread(() -> {
                try {
                    grpcServer.awaitTermination();
                } catch (InterruptedException e) {
                    LOGGER.error("grpc server was interrupted", e);
                    grpcServer.shutdown();
                }
            });
            holder.setName("grpc-server-holder");
            holder.start();
            LOGGER.info("GrpcServer started on port(s): {}", getPort());
        } catch (IOException e) {
            throw new WebServerException(e.getMessage(), e);
        }
    }

    @Override
    public void stop() throws WebServerException {
        grpcServer.shutdown();
        LOGGER.info("GrpcServer was shutdown");
    }

    @Override
    public int getPort() {
        return grpcServer.getPort();
    }

    @Override
    public void shutDownGracefully(GracefulShutdownCallback callback) {
        GrpcWebServer.super.shutDownGracefully(callback);
    }

    @Override
    public List<ServerServiceDefinition> getServices() {
        return grpcServer.getServices();
    }
}
