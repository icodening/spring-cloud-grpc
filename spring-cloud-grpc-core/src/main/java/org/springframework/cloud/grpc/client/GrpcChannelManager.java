package org.springframework.cloud.grpc.client;

import io.grpc.Channel;

/**
 * @author icodening
 * @date 2022.07.15
 */
public interface GrpcChannelManager {

    Channel getOrCreate(String host, int port);

    Channel getChannel(String host, int port);
}
