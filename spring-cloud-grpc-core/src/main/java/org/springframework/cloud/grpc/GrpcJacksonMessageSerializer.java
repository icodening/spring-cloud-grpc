package org.springframework.cloud.grpc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.util.Assert;

import java.io.IOException;

/**
 * @author icodening
 * @date 2022.07.13
 */
public class GrpcJacksonMessageSerializer implements GrpcMessageSerializer {

    private final ObjectMapper objectMapper;

    public GrpcJacksonMessageSerializer(ObjectMapper objectMapper) {
        Assert.notNull(objectMapper, "ObjectMapper can not be null");
        this.objectMapper = objectMapper;
    }

    @Override
    public <T> T deserialize(byte[] sourceMessage, Class<T> desireType) {
        try {
            return objectMapper.readValue(sourceMessage, desireType);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] serialize(Object obj) {
        try {
            return objectMapper.writeValueAsBytes(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
