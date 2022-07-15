package org.springframework.cloud.grpc;

/**
 * @author icodening
 * @date 2022.07.13
 */
public interface GrpcMessageSerializer {

    <T> T deserialize(byte[] sourceMessage, Class<T> desireType);

    byte[] serialize(Object obj);

}
