# Validation & Errors

Steppy validates type compatibility when building flows and captures failures during execution.

## Result types

Flow invocation returns a `Result` indicating success, failure or abort.

```java
var flow = StaticFlowBuilderFactory
    .builder(String.class, String.class)
    .append(AppendAStep.class)
    .append(FailStep.class)
    .build();

Result<String> result = flow.invoke("");
result.getType();                 // FAILED
result.getException().getMessage(); // "Fail"
```

## Aborting a flow

```java
class AbortStep implements Step<None, String, String> {
    @Override
    public String invoke(Context<None> ctx, String in) {
        ctx.abort();
        return null;
    }
}
```

Calling `abort()` stops the flow and returns a result of type `ABORTED`.

## Type validation

The builder checks that the input and output types of all steps line up. Mismatches throw a `ValidationException`.

```java
var builder = StaticFlowBuilderFactory.builder(String.class, Integer.class);
builder.append(AppendAStep.class).build(); // throws ValidationException
```

## Generic types

Steps may declare parameterized types. Steppy keeps that information for validation.

```java
class SumOptional implements Step<Optional<Integer>, Optional<Integer>, Optional<Integer>> {
    @Override
    public Optional<Integer> invoke(Context<Optional<Integer>> ctx,
                                     Optional<Integer> in) {
        return Optional.of(in.orElse(0) + ctx.getConfiguration().orElse(0));
    }
}

var flow = StaticFlowBuilderFactory.builder(Optional.class, Optional.class)
    .append(SumOptional.class).build();
flow.invoke(Configurations.of(Optional.of(1)), Optional.of(2)).getResult(); // Optional[3]
```
