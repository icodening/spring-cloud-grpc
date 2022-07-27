# spring-cloud-grpc

An easy-to-use remote invocation solution based on Spring Cloud and grpc

# Quick Start

### 1. Grpc Stub Style

1. Add Spring Cloud Grpc to the classpath of a Spring Boot application

````xml

<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
    </dependency>
    <!-- spring cloud grpc -->
    <dependency>
        <groupId>com.icodening.cloud</groupId>
        <artifactId>spring-cloud-starter-grpc</artifactId>
        <version>${spring.cloud.grpc.version}</version>
    </dependency>
</dependencies>
````

2. add annotation ``@EnableGrpc`` to spring application

````java

@EnableGrpc
@SpringBootApplication
public class ExampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExampleApplication.class);
    }

}
````

3. add ``@Autowired`` annotation to grpc stub(``consumer side``)

````java
import org.springframework.beans.factory.annotation.Autowired;

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

4. add ``@Service`` annotation to grpc service impl(``provider side``)

````java
import org.springframework.stereotype.Service;

@Service
public class FooServiceImpl extends FooServiceGrpc.FooServiceImplBase {

    //business
}
````

### 2. OpenFeign Style

1. add annotation ``@GrpcClient`` to interface

````java
import org.springframework.cloud.grpc.annotation.GrpcClient;

@GrpcClient(application = "grpc-provider")
public interface AccountService {

    Account getAccount(Long id);

    List<Account> findAll();

    CompletableFuture<Account> getAccountCompletableFuture(Long id);

}
````

2. add annotation ``@Service`` to interface impl

````java
import org.springframework.stereotype.Service;

@Service
public class AccountServiceImpl implements AccountService {

    //business impl
}
````

3. inject interface via ``@Autowired``
````java
public class AccountController {

    @Autowired
    private AccountService accountService;
    
    //business ...
}

````
> For more information, please refer to ``spring-cloud-grpc-examples``