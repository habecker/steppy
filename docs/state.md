# State & Lifecycle

The execution context can hold variables that survive between steps. Mark a field with `@State` and Steppy injects a typed `Variable` that reads and writes the value.

```java
class StateStep implements Step<None, Integer, Integer> {
    @State
    Variable<Integer> counter;

    @Override
    public Integer invoke(Context<None> ctx, Integer input) {
        Integer current = counter.get(ctx);
        counter.set(ctx, current == null ? input : current + input);
        return input;
    }
}
```

Variables are by default scoped to the declaring step. Setting `@State(scope = Scope.FLOW)` shares the variable across all steps in the flow. Marking a variable as `readOnly` makes it immutable.

## Lifecycle hooks

Use `@Before` and `@After` on methods to run code at specific times. Hooks may target `Scope.STEP` or `Scope.FLOW` and are often used for resource management.

```java
class LifecycleStep implements Step<None, None, None> {
    @Before(Scope.FLOW)
    void open(Context<None> ctx) {
        // allocate resources
    }

    @After(Scope.FLOW)
    void close(Context<None> ctx) {
        // release resources
    }

    @Override
    public None invoke(Context<None> ctx, None in) { return None.value(); }
}
```
