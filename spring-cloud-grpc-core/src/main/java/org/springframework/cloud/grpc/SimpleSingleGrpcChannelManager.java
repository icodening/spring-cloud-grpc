package org.springframework.cloud.grpc;

import io.grpc.Channel;
import org.springframework.cloud.grpc.client.ConfigurableGrpcChannelFactory;
import org.springframework.cloud.grpc.client.GrpcChannelManager;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author icodening
 * @date 2022.07.15
 */
public class SimpleSingleGrpcChannelManager implements GrpcChannelManager {

    //TODO clear channel when channel was closed
    private final ConcurrentMap<String, ChannelHolder> channelConcurrentMap = new ConcurrentHashMap<>();

    private final ConfigurableGrpcChannelFactory channelFactory;

    public SimpleSingleGrpcChannelManager(ConfigurableGrpcChannelFactory channelFactory) {
        this.channelFactory = channelFactory;
    }

    @Override
    public Channel getOrCreate(String name, int port) {
        String key = asKey(name, port);
        ChannelHolder channelHolder = channelConcurrentMap.computeIfAbsent(key, (k) -> new ChannelHolder());
        Channel channel = channelHolder.getChannel();
        if (channel == null) {
            synchronized (channelHolder) {
                if (channel == null) {
                    channelHolder.setChannel(channelFactory.getChannel(name, port));
                    channel = channelHolder.getChannel();
                }
            }
        }
        return channel;
    }

    @Override
    public Channel getChannel(String name, int port) {
        String key = asKey(name, port);
        ChannelHolder channelHolder = channelConcurrentMap.computeIfAbsent(key, (k) -> new ChannelHolder());
        return channelHolder.getChannel();
    }

    private String asKey(String name, int port) {
        return name + ":" + port;
    }

    private static class ChannelHolder {

        private Channel channel;

        public Channel getChannel() {
            return channel;
        }

        public ChannelHolder setChannel(Channel channel) {
            this.channel = channel;
            return this;
        }
    }
}
