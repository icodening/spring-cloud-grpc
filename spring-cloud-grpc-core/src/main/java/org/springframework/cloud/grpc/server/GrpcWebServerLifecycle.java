package org.springframework.cloud.grpc.server;

import org.springframework.boot.web.server.WebServer;
import org.springframework.context.SmartLifecycle;
import org.springframework.util.Assert;

/**
 * @author icodening
 * @date 2022.07.14
 */
public class GrpcWebServerLifecycle implements SmartLifecycle {

    private final WebServer webServer;

    private volatile boolean running = false;

    public GrpcWebServerLifecycle(WebServer webServer) {
        Assert.notNull(webServer, "webServer can not be null");
        this.webServer = webServer;
    }

    @Override
    public void start() {
        webServer.start();
        running = true;
    }

    @Override
    public void stop() {
        webServer.stop();
        running = false;
    }

    @Override
    public boolean isRunning() {
        return running;
    }
}
