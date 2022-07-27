package org.springframework.cloud.grpc.support;

import io.grpc.Metadata;

/**
 * @author icodening
 * @date 2022.07.24
 */
public class GrpcMetaData {

    public static final Metadata.Key<String> EXCEPTION_TYPE = Metadata.Key.of("exception-type", Metadata.ASCII_STRING_MARSHALLER);
}
