package org.springframework.cloud.grpc.server;

import org.springframework.boot.web.server.WebServerFactoryCustomizer;

/**
 * @author icodening
 * @date 2022.07.14
 */
public interface ConfigurableGrpcServerFactoryCustomizer extends WebServerFactoryCustomizer<ConfigurableGrpcServerFactory> {

}
