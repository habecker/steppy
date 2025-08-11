# State & Lifecycle

Steppy provides several mechanisms for managing state and sharing data between steps. This includes local state variables, flow-scoped state, and the powerful `@Provides`/`@Consumes` pattern for dependency injection.

## Local State Variables

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

## Provider/Consumer Pattern

The `@Provides` and `@Consumes` annotations enable a powerful dependency injection pattern where steps can provide data that other steps consume. This creates a clean separation of concerns and allows for flexible data flow.

### Providing Data

Use `@Provides` on a `Variable<T>` field to indicate that a step provides data of type `T`:

```java
class UserDataProviderStep implements Step<None, None> {
    @Provides
    Variable<UserData> userData;
    
    @Override
    public None invoke(Context<None> context, None input) {
        // Fetch user data from database
        UserData data = fetchUserFromDatabase();
        userData.set(context, data);
        return None.value();
    }
}
```

### Consuming Data

Use `@Consumes` on a `Variable<T>` field to indicate that a step requires data of type `T`:

```java
class UserDataConsumerStep implements Step<None, String> {
    @Consumes
    Variable<UserData> userData;
    
    @Override
    public String invoke(Context<None> context, None input) {
        UserData data = userData.get(context);
        return "Hello, " + data.name() + "!";
    }
}
```

### Building Flows with Providers/Consumers

When building flows, Steppy automatically validates that all consumed data is provided by some step in the flow:

```java
Flow flow = builder
    .add(new UserDataProviderStep())  // Provides UserData
    .add(new UserDataConsumerStep())  // Consumes UserData
    .build();

String result = flow.invoke();
```

### Validation

Steppy performs validation to ensure that all consumed data is provided. If a step consumes data that isn't provided by any step in the flow, a `ValidationException` is thrown:

```java
// This will throw ValidationException
Flow invalidFlow = builder
    .add(new UserDataConsumerStep())  // Consumes UserData but no provider
    .build();
```

### Nested Flows

The provider/consumer pattern works seamlessly with nested flows. Data provided in a parent flow is available to all nested flows:

```java
Flow flow = builder
    .add(new UserDataProviderStep())
    .nest(None.class, nestedBuilder -> {
        nestedBuilder.add(new UserDataConsumerStep());
    })
    .build();
```

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
