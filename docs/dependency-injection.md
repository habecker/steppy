# Dependency injection

Steps are supplied by a `StepRepository`. For small examples the provided `StaticStepRepository` registers classes or instances manually:

```java
StaticStepRepository.register(AppendAStep.class, AppendBStep.class);
var flow = StaticFlowBuilderFactory
    .builder(String.class, String.class)
    .append(AppendAStep.class)
    .append(AppendBStep.class)
    .build();
```

In larger applications you typically want steps to participate in your dependency injection container. Modules for CDI and Spring include repositories that look up step beans from the application context, enabling constructor injection and other framework features.

To integrate with Spring for example, use the `SpringStepRepository` and register your step classes as beans. The flow builder will resolve them through the repository.
