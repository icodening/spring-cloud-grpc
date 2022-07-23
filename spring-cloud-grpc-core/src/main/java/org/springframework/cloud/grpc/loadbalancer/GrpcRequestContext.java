package org.springframework.cloud.grpc.loadbalancer;

/**
 * @author icodening
 * @date 2022.07.23
 */
public class GrpcRequestContext {

    private final String application;

    private String serviceName;

    private String bareMethodName;

    public GrpcRequestContext(String application) {
        this.application = application;
    }

    public String getApplication() {
        return application;
    }

    public String getServiceName() {
        return serviceName;
    }

    public GrpcRequestContext setServiceName(String serviceName) {
        this.serviceName = serviceName;
        return this;
    }

    public String getBareMethodName() {
        return bareMethodName;
    }

    public GrpcRequestContext setBareMethodName(String bareMethodName) {
        this.bareMethodName = bareMethodName;
        return this;
    }
}
