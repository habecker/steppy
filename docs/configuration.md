# Configuration

Configurations are typed objects supplied when invoking or streaming a flow. They typically hold external services or settings that steps depend on.

```java
class ReturnConfigStep implements Step<Integer, None, Integer> {
    @Override
    public Integer invoke(Context<Integer> ctx, None in) {
        return ctx.getConfiguration();
    }
}

var flow = StaticFlowBuilderFactory
    .builder(None.class, Integer.class)
    .append(ReturnConfigStep.class)
    .build();

flow.invoke(Configurations.of(42)).getResult(); // 42
```

The generic parameter on `Step<C, I, O>` defines the expected configuration type. If a required configuration is missing, a `MissingConfigurationException` is thrown.
