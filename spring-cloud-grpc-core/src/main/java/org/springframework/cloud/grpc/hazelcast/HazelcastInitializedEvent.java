package org.springframework.cloud.grpc.hazelcast;

import com.hazelcast.core.HazelcastInstance;
import org.springframework.context.ApplicationEvent;

/**
 * @author icodening
 * @date 2022.07.22
 */
public class HazelcastInitializedEvent extends ApplicationEvent {

    public HazelcastInitializedEvent(HazelcastInstance source) {
        super(source);
    }

    public HazelcastInstance getHazelcastInstance() {
        return (HazelcastInstance) getSource();
    }
}
