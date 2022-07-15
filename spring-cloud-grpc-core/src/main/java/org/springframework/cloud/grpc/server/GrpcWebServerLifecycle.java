package org.springframework.cloud.grpc.server;

import org.springframework.context.ApplicationContext;
import org.springframework.context.SmartLifecycle;
import org.springframework.util.Assert;

/**
 * @author icodening
 * @date 2022.07.14
 */
public class GrpcWebServerLifecycle implements SmartLifecycle {

    private final GrpcWebServer webServer;

    private final ApplicationContext applicationContext;

    private volatile boolean running = false;

    public GrpcWebServerLifecycle(ApplicationContext applicationContext, GrpcWebServer webServer) {
        Assert.notNull(applicationContext, "applicationContext can not be null");
        Assert.notNull(webServer, "webServer can not be null");
        this.webServer = webServer;
        this.applicationContext = applicationContext;
    }

    @Override
    public void start() {
        webServer.start();
        running = true;
        applicationContext.publishEvent(new GrpcServerInitializedEvent(webServer));
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

    @Override
    public int getPhase() {
        return Integer.MIN_VALUE + 1000;
    }
}
