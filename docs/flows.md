# Flows

A *flow* is an ordered chain of steps. Each step implements the `Step<C, I, O>` interface and the builder ensures that the output type of one step matches the input type of the next.

## Sequential execution

By default steps run one after another. Each step receives the previous result as input.

```java
var flow = StaticFlowBuilderFactory
    .builder(String.class, String.class)
    .append(AppendAStep.class)
    .append(AppendBStep.class)
    .build();

Result<String> result = flow.invoke("");
result.getResult(); // "AB"
```

The invocation returns a `Result` that captures the outcome and any thrown exception.

## Concurrent execution

Adding `concurrent()` switches the builder into a mode where a collection of inputs is processed in parallel using the globally configured executor.

```java
var flow = StaticFlowBuilderFactory
    .builder(String.class, String.class)
    .append(AppendAStep.class)
    .append(AppendBStep.class)
    .concurrent()
    .build();

List<Result<String>> outputs = flow.invoke(List.of("", "C"));
// outputs.get(0).getResult() == "AB"
// outputs.get(1).getResult() == "CAB"
```

The returned list mirrors the input order and retains the `Result` for each element.
