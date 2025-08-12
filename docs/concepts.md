# Concepts

Steppy helps you structure complex workflows into small, testable units called **steps**. This page introduces the main ideas from the top level down so you can understand how they fit together before diving into the details.

## Core Concepts

### Steps

Steps are the fundamental building blocks of Steppy workflows. Each step is a unit of work with a single, well-defined purpose:

- Takes an input and produces an output
- Can access configuration and state
- Can be easily tested in isolation
- Has one main responsibility, even if the implementation is complex

```java
@FunctionalInterface
public interface Step<C, I, R> {
    R invoke(Context<C> context, I input) throws ExecutionException;
}
```

The type parameters represent:
- `C`: Configuration type (what dependencies the step needs)
- `I`: Input type (what data the step receives)
- `R`: Return type (what data the step produces)

### Flows

Flows orchestrate multiple steps into a complete workflow. They can be:

- **Sequential**: Steps execute one after another
- **Concurrent**: Steps execute in parallel
- **Branched**: Different paths based on conditions
- **Nested**: Flows within flows

Flows provide a fluent builder API for composing steps:

```java
var flow = StaticFlowBuilderFactory
    .builder(String.class, String.class)
    .append(AppendAStep.class)
    .append(AppendBStep.class)
    .build();
```

### Context

The `Context` object provides each step with:

- **Configuration**: Information and dependencies the step needs
- **State**: Data that can be isolated or shared depending on scope
- **Execution control**: Ability to abort the flow
- **Scope management**: Different levels of state isolation

```java
public class MyStep implements Step<PaymentService, Order, Order> {
    @State
    Variable<Integer> counter;
    
    @Override
    public Order invoke(Context<PaymentService> context, Order order) {
        // Access configuration
        PaymentService service = context.getConfiguration();
        
        // Access state
        Integer current = counter.get(context);
        
        // Set state
        counter.set(context, current + 1);
        
        return order;
    }
}
```

### Configurations

Configurations provide information and dependencies that steps need at runtime. They can contain:

- Configuration parameters (URLs, timeouts, thresholds, etc.)
- Service objects (databases, external APIs, etc.)
- Shared resources and data
- Environment-specific settings

```java
var config = Configurations.of(
    new PaymentService(),
    new DatabaseConnection(),
    new EmailService(),
    new ApiConfiguration("https://api.example.com", 5000),
    new ThresholdConfig(1000, 500)
);

flow.invoke(config, input);
```

**Note**: This is different from CDI (Contexts and Dependency Injection). Steppy's configuration system is for providing runtime information and dependencies, while CDI is a separate dependency injection framework that can be used alongside Steppy. Steps can be CDI beans when properly configured.

### State Management

Steppy provides flexible state management with different scopes and patterns:

#### Local State Variables

- **STEP scope**: State is isolated to individual step executions (not shared between steps)
- **FLOW scope**: State is shared across all steps in a flow

```java
public class StatefulStep implements Step<None, Integer, Integer> {
    @State(scope = Scope.STEP)
    Variable<Integer> stepCounter;
    
    @State(scope = Scope.FLOW)
    Variable<Integer> flowCounter;
    
    @Override
    public Integer invoke(Context<None> context, Integer input) {
        // Step-scoped state is isolated per step execution
        // Each step gets its own instance of this variable
        Integer stepCount = stepCounter.get(context);
        stepCounter.set(context, (stepCount == null ? 0 : stepCount) + 1);
        
        // Flow-scoped state is shared across all steps
        // All steps in the flow can access and modify this variable
        Integer flowCount = flowCounter.get(context);
        flowCounter.set(context, (flowCount == null ? 0 : flowCount) + 1);
        
        return input;
    }
}
```

#### Provider/Consumer Pattern

For dependency injection and data sharing between steps, use the `@Provides` and `@Consumes` annotations:

```java
// Step that provides data
public class DataProviderStep implements Step<None, None, None> {
    @Provides
    Variable<UserData> userData;
    
    @Override
    public None invoke(Context<None> context, None input) {
        userData.set(context, fetchUserData());
        return None.value();
    }
}

// Step that consumes data
public class DataConsumerStep implements Step<None, None, String> {
    @Consumes
    Variable<UserData> userData;
    
    @Override
    public String invoke(Context<None> context, None input) {
        UserData data = userData.get(context);
        return "Hello, " + data.name() + "!";
    }
}
```

This pattern enables clean separation of concerns and automatic validation that all consumed data is provided.

## Flow Patterns

### Sequential Flows

The simplest pattern where steps execute one after another:

```java
var flow = StaticFlowBuilderFactory
    .builder(String.class, String.class)
    .append(ValidateStep.class)
    .append(ProcessStep.class)
    .append(NotifyStep.class)
    .build();
```

### Concurrent Flows

Steps execute in parallel for better performance:

```java
var flow = StaticFlowBuilderFactory
    .builder(String.class, String.class)
    .append(ValidateStep.class)
    .append(ProcessStep.class)
    .append(NotifyStep.class)
    .concurrent()
    .build();
```

### Branched Flows

Conditional execution paths based on input data:

```java
var flow = StaticFlowBuilderFactory
    .builder(Order.class, Order.class)
    .branch(Order.class, Order.class, builder -> 
        builder.when(order -> order.amount() > 1000, 
            b -> b.append(HighValueProcessStep.class))
        .when(order -> order.amount() > 100, 
            b -> b.append(MediumValueProcessStep.class))
        .otherwise(b -> b.append(StandardProcessStep.class))
    )
    .build();
```

### Nested Flows

Flows can contain other flows for complex orchestration:

```java
var flow = StaticFlowBuilderFactory
    .builder(Order.class, Order.class)
    .append(ValidateStep.class)
    .nest(Order.class, nestedBuilder -> 
        nestedBuilder
            .append(ProcessPaymentStep.class)
            .append(SendConfirmationStep.class)
    )
    .append(LogStep.class)
    .build();
```

## Advanced Features

### Streaming

For processing large datasets or continuous data streams:

```java
// Define source and sink
var source = new SimpleSource<>(dataStream);
var sink = new SimpleSink<Result>();

// Stream processing
flow.stream(source, sink);
```

### Lifecycle Hooks

Steps can define lifecycle methods using annotations:

```java
public class LifecycleStep implements Step<None, String, String> {
    @Before(Scope.STEP)
    public void beforeStep() {
        // Called before step execution
    }
    
    @After(Scope.STEP)
    public void afterStep() {
        // Called after step execution
    }
    
    @Override
    public String invoke(Context<None> context, String input) {
        return input + " processed";
    }
}
```

### Error Handling

Steppy provides comprehensive error handling:

- **ExecutionException**: Business logic errors
- **ValidationException**: Flow configuration errors
- **Result types**: SUCCESS, FAILED, ABORTED

```java
var result = flow.invoke(input);
switch (result.getType()) {
    case SUCCESS:
        // Handle successful execution
        break;
    case FAILED:
        // Handle failure
        Exception error = result.getException();
        break;
    case ABORTED:
        // Handle manual abort
        break;
}
```

### Validation

Steppy validates flows at build time to catch configuration errors early:

- Type compatibility between steps
- State variable declarations
- Branch condition completeness

## Integration Patterns

### Dependency Injection Integration

Steppy can integrate with popular DI frameworks for step creation and management:

- **CDI**: `CdiFlowBuilderFactory` and `CdiStepRepository` - Steps can be CDI beans
- **Spring**: `SpringFlowBuilderFactory` and `SpringStepRepository` - Steps can be Spring beans
- **POJO**: `StaticFlowBuilderFactory` and `StaticStepRepository` - Simple POJO-based approach

When using CDI or Spring, steps can leverage the full power of their respective DI frameworks (constructor injection, lifecycle management, etc.) while still using Steppy's configuration system for runtime dependencies.

### Testing

Steps are designed to be easily testable:

```java
@Test
void testStep() throws ExecutionException {
    var step = new MyStep();
    var context = new Context<>(Configurations.empty(), None.class);
    var result = step.invoke(context, "test input");
    
    assertThat(result).isEqualTo("expected output");
}
```



## Summary

Steppy provides a powerful yet simple framework for building complex workflows. By breaking down workflows into focused, testable steps and providing flexible orchestration patterns, it helps you create maintainable and scalable business logic.