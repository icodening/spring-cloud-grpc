# spring-cloud-grpc-examples

### example-grpc-interface

a grpc stub module

### example-grpc-service

a grpc service implement of ``OrderService``

### example-grpc-consumer

a grpc service consumer of ``OrderService``

# usage
### 1. compile project
### 2. start ``service-registry(Eureka/Nacos/...)``
### 3. start ``example-grpc-service``
### 4. start ``example-grpc-consumer``
### 5. request ``http://localhost:9191/orders/1``
The successful result is as follows
````json
{
  "id": "1",
  "price": 9,
  "products": [
    {
      "name": "Apple",
      "price": 3
    },
    {
      "name": "Banana",
      "price": 2
    },
    {
      "name": "Pear",
      "price": 4
    }
  ]
}
````