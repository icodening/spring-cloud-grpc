package org.springframework.cloud.grpc;

import io.grpc.stub.AbstractStub;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.cloud.grpc.annotation.EnableGrpc;
import org.springframework.cloud.grpc.annotation.GrpcClient;
import org.springframework.cloud.grpc.client.GrpcClientFactoryBean;
import org.springframework.cloud.grpc.client.GrpcClientSpecification;
import org.springframework.cloud.grpc.client.GrpcStubFactoryBean;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author icodening
 * @date 2022.07.12
 */
public class EnableGrpcRegistrar implements ImportBeanDefinitionRegistrar, EnvironmentAware, ResourceLoaderAware {

    private static final String GRPC_CLIENT_BEAN_SUFFIX = "&GrpcClient";

    private static final String GRPC_STUB_BEAN_SUFFIX = "&GrpcStub";

    private ResourceLoader resourceLoader;

    private Environment environment;

    @Override
    public void registerBeanDefinitions(@NonNull AnnotationMetadata importingClassMetadata, @NonNull BeanDefinitionRegistry registry) {
        registerDefaultConfiguration(importingClassMetadata, registry);
        registerGrpcClients(importingClassMetadata, registry);
        registerGrpcServices(importingClassMetadata, registry);
    }

    private void registerGrpcServices(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        ClassPathBeanDefinitionScanner scanner = new ClassPathBeanDefinitionScanner(registry);
        scanner.setResourceLoader(this.resourceLoader);
        scanner.addIncludeFilter(new AnnotationTypeFilter(Service.class));
        Set<String> basePackages = getBasePackages(importingClassMetadata);
        scanner.scan(basePackages.toArray(new String[0]));
    }

    private void registerDefaultConfiguration(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
        MergedAnnotation<EnableGrpc> enableGrpcMergedAnnotation = metadata.getAnnotations().get(EnableGrpc.class);
        Class<?>[] defaultConfigurations = enableGrpcMergedAnnotation.getClassArray("defaultConfigurations");
        String defaultConfigurationName;
        if (metadata.hasEnclosingClass()) {
            defaultConfigurationName = "default." + metadata.getEnclosingClassName();
        } else {
            defaultConfigurationName = "default." + metadata.getClassName();
        }
        registerClientConfiguration(registry, defaultConfigurationName, defaultConfigurations);
    }

    private void registerGrpcClients(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        LinkedHashSet<BeanDefinition> candidateComponents = new LinkedHashSet<>();
        ClassPathScanningCandidateComponentProvider scanner = getScanner();
        scanner.setResourceLoader(this.resourceLoader);
        scanner.addIncludeFilter(new AnnotationTypeFilter(GrpcClient.class));
        Set<String> basePackages = getBasePackages(importingClassMetadata);
        for (String basePackage : basePackages) {
            candidateComponents.addAll(scanner.findCandidateComponents(basePackage));
        }
        for (BeanDefinition candidateComponent : candidateComponents) {
            if (candidateComponent instanceof AnnotatedBeanDefinition) {
                AnnotatedBeanDefinition beanDefinition = (AnnotatedBeanDefinition) candidateComponent;
                AnnotationMetadata annotationMetadata = beanDefinition.getMetadata();
                MergedAnnotation<GrpcClient> grpcClientMergedAnnotation = annotationMetadata.getAnnotations().get(GrpcClient.class);
                String application = grpcClientMergedAnnotation.getString("application");
                Class<?>[] configurations = grpcClientMergedAnnotation.getClassArray("configurations");
                if (annotationMetadata.isInterface()) {
                    registerClientConfiguration(registry, application, configurations);
                    registerGrpcClient(registry, application, annotationMetadata);
                    continue;
                }
                Class<?> stubType = ClassUtils.resolveClassName(annotationMetadata.getClassName(), ClassUtils.getDefaultClassLoader());
                if (AbstractStub.class.isAssignableFrom(stubType)) {
                    registerGrpcClientWithStub(registry, stubType, application, annotationMetadata);
                }
            }
        }
    }

    private void registerGrpcClientWithStub(BeanDefinitionRegistry registry,
                                            Class<?> stubType,
                                            String application,
                                            AnnotationMetadata annotationMetadata) {
        BeanDefinitionBuilder stubBuilder = BeanDefinitionBuilder.genericBeanDefinition(GrpcStubFactoryBean.class);
        stubBuilder.addConstructorArgValue(application);
        stubBuilder.addConstructorArgValue(stubType);
        stubBuilder.setLazyInit(true);
        String className = annotationMetadata.getClassName();
        registry.registerBeanDefinition(className + GRPC_STUB_BEAN_SUFFIX, stubBuilder.getBeanDefinition());
    }

    private void registerGrpcClient(BeanDefinitionRegistry registry,
                                    String application,
                                    AnnotationMetadata annotationMetadata) {
        String className = annotationMetadata.getClassName();
        Class<?> clientInterface = ClassUtils.resolveClassName(className, ClassUtils.getDefaultClassLoader());
        BeanDefinitionBuilder grpcClientFactoryBeanBuilder = BeanDefinitionBuilder.genericBeanDefinition(GrpcClientFactoryBean.class);
        grpcClientFactoryBeanBuilder.addConstructorArgValue(application);
        grpcClientFactoryBeanBuilder.addConstructorArgValue(clientInterface);
        registry.registerBeanDefinition(className + GRPC_CLIENT_BEAN_SUFFIX, grpcClientFactoryBeanBuilder.getBeanDefinition());
    }

    protected ClassPathScanningCandidateComponentProvider getScanner() {
        return new ClassPathScanningCandidateComponentProvider(false, this.environment) {
            @Override
            protected boolean isCandidateComponent(@NonNull AnnotatedBeanDefinition beanDefinition) {
                boolean isCandidate = false;
                if (beanDefinition.getMetadata().isIndependent()) {
                    if (!beanDefinition.getMetadata().isAnnotation()) {
                        isCandidate = true;
                    }
                }
                return isCandidate;
            }
        };
    }

    protected Set<String> getBasePackages(AnnotationMetadata importingClassMetadata) {
        MergedAnnotation<EnableGrpc> enableGrpcMergedAnnotation = importingClassMetadata.getAnnotations().get(EnableGrpc.class);
        Set<String> basePackages = new HashSet<>();
        for (String pkg : enableGrpcMergedAnnotation.getStringArray("value")) {
            if (StringUtils.hasText(pkg)) {
                basePackages.add(pkg);
            }
        }
        for (String pkg : enableGrpcMergedAnnotation.getStringArray("basePackages")) {
            if (StringUtils.hasText(pkg)) {
                basePackages.add(pkg);
            }
        }
        if (basePackages.isEmpty()) {
            basePackages.add(ClassUtils.getPackageName(importingClassMetadata.getClassName()));
        }
        return basePackages;
    }

    private void registerClientConfiguration(BeanDefinitionRegistry registry, String name, Class<?>[] configuration) {
        String beanName = name + "." + GrpcClientSpecification.class.getSimpleName();
        if (registry.containsBeanDefinition(beanName)) {
            return;
        }
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(GrpcClientSpecification.class);
        builder.addConstructorArgValue(name);
        builder.addConstructorArgValue(configuration);
        registry.registerBeanDefinition(beanName, builder.getBeanDefinition());
    }

    @Override
    public void setEnvironment(@NonNull Environment environment) {
        this.environment = environment;
    }

    @Override
    public void setResourceLoader(@NonNull ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }
}
