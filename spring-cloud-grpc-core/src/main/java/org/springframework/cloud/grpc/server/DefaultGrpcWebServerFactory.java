package org.springframework.cloud.grpc.server;

import io.grpc.BindableService;
import io.grpc.ServerBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static io.grpc.internal.GrpcUtil.getThreadFactory;

/**
 * @author icodening
 * @date 2022.07.14
 */
public class DefaultGrpcWebServerFactory implements ConfigurableGrpcServerFactory, GrpcWebServerFactory {

    private int port;

    private int maxInboundMessageSize;

    private final List<BindableService> bindableServices = new ArrayList<>();

    private int maximumPoolSize = 1;

    private int corePoolSize = 1;

    private int threadsQueue = 0;

    private final ThreadFactory threadFactory = getThreadFactory("grpc-server-handler" + "-%d", true);

    @Override
    public ConfigurableGrpcServerFactory port(int port) {
        this.port = port;
        return this;
    }

    @Override
    public ConfigurableGrpcServerFactory maxInboundMessageSize(int maxInboundMessageSize) {
        this.maxInboundMessageSize = maxInboundMessageSize;
        return this;
    }

    @Override
    public ConfigurableGrpcServerFactory addService(BindableService bindableService) {
        bindableServices.add(bindableService);
        return this;
    }

    @Override
    public ConfigurableGrpcServerFactory maximumPoolSize(int maximumPoolSize) {
        this.maximumPoolSize = maximumPoolSize;
        return this;
    }

    @Override
    public ConfigurableGrpcServerFactory corePoolSize(int corePoolSize) {
        this.corePoolSize = corePoolSize;
        return this;
    }

    @Override
    public ConfigurableGrpcServerFactory threadsQueue(int queueSize) {
        this.threadsQueue = queueSize;
        return this;
    }

    @Override
    public GrpcWebServer getWebServer() {
        ServerBuilder<?> serverBuilder = ServerBuilder.forPort(port)
                .maxInboundMessageSize(maxInboundMessageSize);
        for (BindableService bindableService : bindableServices) {
            serverBuilder = serverBuilder.addService(bindableService);
        }
        if (maximumPoolSize == 1) {
            serverBuilder.directExecutor();
        } else {
            ThreadPoolExecutor threadPoolExecutor;
            if (threadsQueue > 1) {
                threadPoolExecutor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, 60, TimeUnit.SECONDS, new ArrayBlockingQueue<>(threadsQueue), threadFactory);
            } else {
                threadPoolExecutor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, 60, TimeUnit.SECONDS, new SynchronousQueue<>(), threadFactory);
            }
            serverBuilder.executor(threadPoolExecutor);
        }
        return new DefaultGrpcWebServer(serverBuilder.build());
    }
}
