package org.springframework.cloud.grpc.server;

import io.grpc.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.server.GracefulShutdownCallback;
import org.springframework.boot.web.server.WebServer;
import org.springframework.boot.web.server.WebServerException;

import java.io.IOException;

/**
 * @author icodening
 * @date 2022.07.14
 */
public class DefaultGrpcWebServer implements WebServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultGrpcWebServer.class);

    private final Server grpServer;

    public DefaultGrpcWebServer(Server grpServer) {
        this.grpServer = grpServer;
    }

    @Override
    public void start() throws WebServerException {
        try {
            grpServer.start();
            Thread holder = new Thread(() -> {
                try {
                    grpServer.awaitTermination();
                } catch (InterruptedException e) {
                    LOGGER.error("grpc server was interrupted", e);
                    grpServer.shutdown();
                }
            });
            holder.setDaemon(true);
            holder.setName("grpc-server-holder");
            holder.start();
            LOGGER.info("GrpcServer started on port(s): {}", getPort());
        } catch (IOException e) {
            throw new WebServerException(e.getMessage(), e);
        }
    }

    @Override
    public void stop() throws WebServerException {
        grpServer.shutdown();
        LOGGER.info("GrpcServer was shutdown");
    }

    @Override
    public int getPort() {
        return grpServer.getPort();
    }

    @Override
    public void shutDownGracefully(GracefulShutdownCallback callback) {
        WebServer.super.shutDownGracefully(callback);
    }
}
