package example.grpc.service;

import example.grpc.api.OrderServiceGrpc;
import example.grpc.api.OrderServiceOuterClass;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

/**
 * @author icodening
 * @date 2022.07.23
 */
@Service
public class OrderServiceImpl extends OrderServiceGrpc.OrderServiceImplBase {

    private final List<OrderServiceOuterClass.Order> orders = new ArrayList<>();

    @Override
    public void queryOrder(OrderServiceOuterClass.Query request, StreamObserver<OrderServiceOuterClass.Order> responseObserver) {
        String id = request.getId();
        OrderServiceOuterClass.Order order = orders.stream()
                .filter(ele -> ele.getId().equals(id))
                .findFirst()
                .orElse(null);
        if (order == null) {
            Status status = Status.UNAVAILABLE.withDescription("no such order [id=" + id + "]");
            responseObserver.onError(new StatusRuntimeException(status));
            return;
        }
        responseObserver.onNext(order);
        responseObserver.onCompleted();
    }

    @PostConstruct
    public void initialize() {
        orders.add(OrderServiceOuterClass.Order.newBuilder()
                .setId("1")
                .setGmtCreate(System.currentTimeMillis())
                .addProduct(buildProduct("Apple", 3))
                .addProduct(buildProduct("Banana", 2))
                .addProduct(buildProduct("Pear", 4))
                .setPrice(3 + 2 + 4)
                .build());

        orders.add(OrderServiceOuterClass.Order.newBuilder()
                .setId("2")
                .setGmtCreate(System.currentTimeMillis())
                .addProduct(buildProduct("Computer", 9999))
                .addProduct(buildProduct("Phone", 5999))
                .setPrice(9999 + 5999)
                .build());

        orders.add(OrderServiceOuterClass.Order.newBuilder()
                .setId("3")
                .setGmtCreate(System.currentTimeMillis())
                .addProduct(buildProduct("MineralWater", 3))
                .addProduct(buildProduct("Orange juice", 6))
                .setPrice(3 + 6)
                .build());
    }

    private OrderServiceOuterClass.Product buildProduct(String name, int price) {
        return OrderServiceOuterClass.Product.newBuilder()
                .setName(name)
                .setPrice(price)
                .build();
    }
}
