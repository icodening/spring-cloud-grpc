package example.grpc.configuration.circuitbreaker;

import com.alibaba.cloud.circuitbreaker.sentinel.SentinelCircuitBreakerAutoConfiguration;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

/**
 * @author icodening
 * @date 2022.07.25
 */
@Configuration
@ConditionalOnClass({SphU.class})
@ConditionalOnProperty(name = "spring.cloud.circuitbreaker.sentinel.enabled",
        havingValue = "true", matchIfMissing = true)
@AutoConfigureAfter(SentinelCircuitBreakerAutoConfiguration.class)
public class SentinelCircuitBreakerConfiguration {

    @Bean
    public SmartInitializingSingleton initDegradeRule() {
        return () -> {
            System.out.println("grpc client interceptor use [alibaba Sentinel CircuitBreaker]");
            DegradeRule degradeRule = new DegradeRule("OrderService/queryOrder");
            degradeRule.setMinRequestAmount(1)
                    .setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_COUNT)
                    .setCount(1)
                    .setTimeWindow(10)
                    .setMinRequestAmount(1)
                    .setStatIntervalMs(10_000);
            DegradeRuleManager.loadRules(Collections.singletonList(degradeRule));
        };
    }
}
