# Concepts

Steppy helps you structure complex workflows into small, testable units called **steps**.  This page introduces the main ideas from the top level down so you can understand how they fit together before diving into the details.

## Workflows

A *workflow* orchestrates a series of steps to reach a business goal.  You describe the workflow using a `Flow` that wires steps together and controls how they execute.

```java
Flow<String> flow = Flow.sequential(
    registry.step("ValidateOrder"),
    registry.step("ChargePayment"),
    registry.step("SendConfirmation")
);
```

`Flow.sequential` runs the steps one after another and passes the result of one step to the next.

## Steps

A step is a small unit of work.  It receives an input and returns a result wrapped in an `Outcome` to signal success or failure.

```java
public class ValidateOrder implements Step<Order, Order> {
  @Override
  public Outcome<Order> run(Order order, StepContext ctx) {
    if (order.isValid()) {
      return Outcome.ok(order);
    }
    return Outcome.fail("invalid order");
  }
}
```

## Execution modes

Workflows can execute steps in different ways depending on the problem you are solving.

### Sequential and concurrent flows

*Sequential* flows execute one step at a time.  *Concurrent* flows run several steps in parallel and merge their outputs.

```java
Flow<String> concurrent = Flow.concurrent(
    registry.step("AppendA"),
    registry.step("AppendB")
);
```

### Branching and nesting

You can branch based on runtime data and even embed flows inside other flows.

```java
Flow<String> branched = Flow.branch(
    ctx -> ctx.get("shouldAppendB"),
    registry.step("AppendB"),
    registry.step("AppendC")
);
```

### Streaming

For large or unbounded data sets, Steppy streams items through a source, optional processors, and a sink.

```java
Flow.stream(
    registry.step("ReadLines"),
    List.of(registry.step("Filter")),
    registry.step("Persist")
);
```

## State and lifecycle

Each step can store temporary state in a `StepContext` and react to lifecycle hooks such as `before` and `after`.

```java
public class AuditStep implements Step<String, String> {
  @Override
  public void before(StepContext ctx) {
    ctx.put("start", Instant.now());
  }
}
```

## Integration

### Configuration

Steps can read configuration values from the `StepContext` or an injected configuration object.

```java
String url = ctx.config("payment.url");
```

### Dependency injection

Steppy integrates with DI containers.  Register the step class and the framework resolves its dependencies when the flow runs.

```java
registry.register("ChargePayment", new ChargePayment(service));
```

## Validation and errors

Outcomes can fail.  You can abort a flow early or handle failures explicitly.

```java
Outcome<String> result = flow.run("start");
if (result.isFail()) {
  // handle error
}
```

With these concepts in mind, explore the other chapters for deeper explanations and full examples.

