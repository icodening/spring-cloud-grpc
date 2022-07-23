package example.grpc.controller;

import com.google.common.util.concurrent.ListenableFuture;
import example.grpc.api.OrderServiceGrpc;
import example.grpc.api.OrderServiceOuterClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

/**
 * @author icodening
 * @date 2022.07.23
 */
@RestController
@RequestMapping("/orders")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private OrderServiceGrpc.OrderServiceBlockingStub orderServiceBlockingStub;

    @Autowired
    private OrderServiceGrpc.OrderServiceFutureStub orderServiceFutureStub;

    @GetMapping("/{orderId}")
    public Object queryOrder(@PathVariable(name = "orderId") String orderId) {
        OrderServiceOuterClass.Query query = OrderServiceOuterClass.Query.newBuilder().setId(orderId).build();
        OrderServiceOuterClass.Order order = orderServiceBlockingStub.queryOrder(query);
        ListenableFuture<OrderServiceOuterClass.Order> orderListenableFuture = orderServiceFutureStub.queryOrder(query);
        orderListenableFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    OrderServiceOuterClass.Order futureResult = orderListenableFuture.get();
                    logger.info("queryOrder success via OrderServiceFutureStub: {}", OrderController.this.orderToMap(futureResult));
                } catch (InterruptedException | ExecutionException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }, ForkJoinPool.commonPool());
        return orderToMap(order);
    }

    private Map<String, Object> orderToMap(OrderServiceOuterClass.Order order) {
        if (order == null) {
            return null;
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", order.getId());
        result.put("price", order.getPrice());
        List<Map<String, Object>> products = order.getProductList().stream().map(this::producToMap).collect(Collectors.toList());
        result.put("products", products);
        return result;
    }

    private Map<String, Object> producToMap(OrderServiceOuterClass.Product product) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("name", product.getName());
        result.put("price", product.getPrice());
        return result;
    }
}
