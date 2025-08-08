# Getting started

This quick example shows how Steppy orchestrates a simple order fulfilment pipeline.

## 1. Define the steps

```java
record Order(String id, int total) {}

class ValidateOrder implements Step<None, Order, Order> {
    @Override
    public Order invoke(Context<None> ctx, Order order) throws ExecutionException {
        if (order.total() <= 0) {
            throw new ExecutionException("total must be positive");
        }
        return order;
    }
}

class ChargePayment implements Step<PaymentService, Order, Order> {
    @Override
    public Order invoke(Context<PaymentService> ctx, Order order) throws ExecutionException {
        ctx.getConfiguration().charge(order.id(), order.total());
        return order;
    }
}

class SendConfirmation implements Step<None, Order, None> {
    @Override
    public None invoke(Context<None> ctx, Order order) {
        System.out.println("confirmation for " + order.id());
        return None.value();
    }
}

class PaymentService {
    void charge(String id, int amount) { /* ... */ }
}
```

## 2. Register steps and initialize

```java
StaticStepRepository.register(
    ValidateOrder.class, ChargePayment.class, SendConfirmation.class);
StaticFlowBuilderFactory.initialize(Executors.newFixedThreadPool(4));
```

## 3. Build and run the flow

```java
var flow = StaticFlowBuilderFactory
    .builder(Order.class, None.class)
    .append(ValidateOrder.class)
    .append(ChargePayment.class)
    .append(SendConfirmation.class)
    .build();

var service = new PaymentService();
var order = new Order("A42", 50);
flow.invoke(Configurations.of(service), order);
```

The pipeline validates the order, charges the configured payment service and sends a confirmation.
