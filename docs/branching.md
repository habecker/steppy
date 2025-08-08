# Branching & Nesting

## Conditional branches

Flows can route execution to different branches based on predicates. Each branch is itself a small builder.

```java
class IncrementerStep implements Step<None, Integer, Integer> {
    @Override
    public Integer invoke(Context<None> ctx, Integer in) {
        return in + 1;
    }
}

class AppendAStep implements Step<None, String, String> {
    @Override
    public String invoke(Context<None> ctx, String in) {
        return in + "A";
    }
}

class AppendBStep implements Step<None, String, String> {
    @Override
    public String invoke(Context<None> ctx, String in) {
        return in + "B";
    }
}

StaticStepRepository.register(IncrementerStep.class, AppendAStep.class, AppendBStep.class);
```

```java
var flow = StaticFlowBuilderFactory
    .builder(Integer.class, Integer.class)
    .branch(Integer.class, Integer.class, b -> b
        .when(i -> i < 10, br -> br.append(IncrementerStep.class))
        .otherwise(br -> br.append(IncrementerStep.class).append(IncrementerStep.class)))
    .build();

flow.invoke(0).getResult();  // 1
flow.invoke(10).getResult(); // 12
```

The first matching branch is executed. If none match, the flow fails with an `ExecutionException`.

## Nesting flows

Use `nest()` to embed a reusable sub-flow inside another flow. The nested flow receives the output of the previous step as its input.

```java
var flow = StaticFlowBuilderFactory
    .builder(String.class, String.class)
    .append(AppendAStep.class)
    .nest(String.class, b -> b.append(AppendBStep.class).append(AppendAStep.class))
    .build();

flow.invoke("").getResult(); // "ABA"
```

Nesting works with sequential and concurrent flows and is handy for composing common pipelines.
