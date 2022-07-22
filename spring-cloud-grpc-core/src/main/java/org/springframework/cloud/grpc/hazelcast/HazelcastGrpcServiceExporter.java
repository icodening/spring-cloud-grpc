package org.springframework.cloud.grpc.hazelcast;

import io.grpc.ServerServiceDefinition;
import io.grpc.ServiceDescriptor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.cloud.grpc.server.GrpcWebServer;
import org.springframework.context.ApplicationListener;
import org.springframework.lang.NonNull;

import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author icodening
 * @date 2022.07.21
 */
public class HazelcastGrpcServiceExporter implements ApplicationListener<HazelcastInitializedEvent> {

    private final Registration registration;

    private final CacheManager hazelcastCacheManager;

    private final GrpcWebServer grpcWebServer;

    public HazelcastGrpcServiceExporter(Registration registration,
                                        CacheManager hazelcastCacheManager,
                                        GrpcWebServer grpcWebServer) {
        this.registration = registration;
        this.hazelcastCacheManager = hazelcastCacheManager;
        this.grpcWebServer = grpcWebServer;
    }

    @Override
    public void onApplicationEvent(@NonNull HazelcastInitializedEvent event) {
        Cache cache = hazelcastCacheManager.getCache(HazelcastConstants.SERVICE_EXPORT_CACHE_NAME);
        Objects.requireNonNull(cache);
        String selfServiceId = registration.getServiceId();
        grpcWebServer.getServices()
                .stream()
                .map(ServerServiceDefinition::getServiceDescriptor)
                .map(ServiceDescriptor::getName)
                .collect(Collectors.toSet())
                .forEach(serviceName ->
                        cache.putIfAbsent(serviceName, selfServiceId));
    }
}
