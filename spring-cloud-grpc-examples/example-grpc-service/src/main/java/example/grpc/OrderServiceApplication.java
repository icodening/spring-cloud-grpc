package example.grpc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.grpc.annotation.EnableGrpc;

/**
 * @author icodening
 * @date 2022.07.23
 */
@SpringBootApplication
@EnableGrpc
public class OrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class);
    }
}
