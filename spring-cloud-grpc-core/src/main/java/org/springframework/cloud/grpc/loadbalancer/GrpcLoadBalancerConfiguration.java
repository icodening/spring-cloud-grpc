package org.springframework.cloud.grpc.loadbalancer;

import org.springframework.cloud.grpc.annotation.EnableGrpc;
import org.springframework.context.annotation.Configuration;

/**
 * @author icodening
 * @date 2022.07.12
 */
@Configuration
@EnableGrpc(defaultConfigurations = GrpcClientLoadBalancerConfiguration.class)
public class GrpcLoadBalancerConfiguration {

}
