package org.springframework.cloud.grpc.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author icodening
 * @date 2022.07.12
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface GrpcClient {

    /**
     * remote application name
     *
     * @return application name
     */
    String application();

    /**
     * specification the grpc client configuration classes
     *
     * @return configuration class array
     */
    Class<?>[] configurations() default {};
}
