# spring-cloud-grpc
An easy-to-use remote invocation solution based on Spring Cloud and grpc

# Quick Start
1. Add Spring Cloud Grpc to the classpath of a Spring Boot application

````xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <!-- spring cloud grpc -->
    <dependency>
        <groupId>com.icodening.cloud</groupId>
        <artifactId>spring-cloud-starter-grpc</artifactId>
        <version>${spring.cloud.grpc.version}</version>
    </dependency>
</dependencies>
````

2. add annotation @EnableGrpc to spring application

````java
@EnableGrpc
@SpringBootApplication
public class ExampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExampleApplication.class);
    }

}
````

3. add ``@Autowired`` annotation to grpc stub

````java
@RestController
@RequestMapping("/foo")
public class FooController {

    @Autowired
    private FooServiceGrpc.FooServiceBlockingStub fooServiceBlockingStub;

    @Autowired
    private FooServiceGrpc.FooServiceFutureStub fooServiceFutureStub;

    @Autowired
    private FooServiceGrpc.FooServiceStub fooServiceStub;

    // to use
}
````