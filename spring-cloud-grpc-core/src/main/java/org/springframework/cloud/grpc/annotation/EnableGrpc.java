package org.springframework.cloud.grpc.annotation;

import org.springframework.cloud.grpc.EnableGrpcRegistrar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author icodening
 * @date 2022.07.12
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(EnableGrpcRegistrar.class)
public @interface EnableGrpc {

    String[] value() default {};

    String[] basePackages() default {};

    Class<?>[] defaultConfigurations() default {};
}
