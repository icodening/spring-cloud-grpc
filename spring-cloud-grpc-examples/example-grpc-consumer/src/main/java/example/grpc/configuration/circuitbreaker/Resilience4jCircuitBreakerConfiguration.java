package example.grpc.configuration.circuitbreaker;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JAutoConfiguration;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * @author icodening
 * @date 2022.07.25
 */
@Configuration
@ConditionalOnClass(Resilience4JCircuitBreakerFactory.class)
@ConditionalOnProperty(name = { "spring.cloud.circuitbreaker.resilience4j.enabled",
        "spring.cloud.circuitbreaker.resilience4j.reactive.enabled" }, matchIfMissing = true)
@AutoConfigureAfter(Resilience4JAutoConfiguration.class)
public class Resilience4jCircuitBreakerConfiguration {

    @Bean
    public Customizer<Resilience4JCircuitBreakerFactory> circuitBreakerFactoryCustomizer() {
        return resilience4JCircuitBreakerFactory -> {
            System.out.println("grpc client interceptor use [Resilience4j CircuitBreaker]");
            CircuitBreakerRegistry circuitBreakerRegistry = resilience4JCircuitBreakerFactory.getCircuitBreakerRegistry();
            CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                    .failureRateThreshold(80)
                    .slowCallDurationThreshold(Duration.ofMillis(500))
                    .slidingWindow(1, 1, CircuitBreakerConfig.SlidingWindowType.TIME_BASED)
                    .build();
            circuitBreakerRegistry.addConfiguration("grpc-provider/OrderService/queryOrder", config);
        };
    }
}
