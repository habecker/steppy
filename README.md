# Steppy

[![CI/CD Pipeline](https://github.com/habecker/steppy/workflows/CI/CD%20Pipeline/badge.svg)](https://github.com/habecker/steppy/actions)
[![GitHub Pages](https://github.com/habecker/steppy/workflows/Deploy%20to%20GitHub%20Pages/badge.svg)](https://github.com/habecker/steppy/actions)
[![Maven Central](https://img.shields.io/maven-central/v/de.y2g/steppy)](https://search.maven.org/artifact/de.y2g/steppy)
[![Java 17+](https://img.shields.io/badge/java-17+-blue.svg)](https://openjdk.java.net/projects/jdk/17/)
[![License](https://img.shields.io/badge/license-MIT-green.svg)](LICENSE)

**Steppy** is a lightweight, Java framework for composing small, reusable steps into robust workflows. It focuses on readability, testability, and developer experience while providing powerful features for building complex business logic.

## ✨ Features

- **🔗 Type-Safe Composition** - Declare input/output types and let the framework wire them together
- **📊 State Management** - Share data across steps using @Provides and @Consumes annotations
- **⚡ Multiple Execution Models** - Sequential, concurrent, and streaming execution patterns
- **🌳 Branching & Nesting** - Build conditional flows and reusable sub-pipelines
- **🔄 Lifecycle Hooks** - Before/after callbacks for setup, teardown, and monitoring
- **⚙️ Configuration Injection** - Seamless integration with dependency injection containers
- **🔍 Validation** - Compile-time and runtime type compatibility checking
- **🧪 Test-Friendly** - Easy to unit test individual steps and flows

## 🚀 Quick Start

### Maven Dependency

```xml
<dependency>
    <groupId>de.y2g</groupId>
    <artifactId>steppy</artifactId>
    <version>0.3.0</version>
</dependency>
```

### Simple Example

```java
import de.y2g.steppy.api.*;

// Define your steps
public class StringLengthStep implements Step<String, Integer> {
    @Override
    public Integer invoke(String input) {
        return input.length();
    }
}

// Build and execute a flow
FlowBuilder builder = FlowBuilderFactory.create();
Flow flow = builder
    .add(new StringLengthStep())
    .build();

Integer result = flow.invoke("Hello, Steppy!");
// result = 14
```

### State Management Example

```java
import de.y2g.steppy.api.*;

// Step that provides data
public class DataProviderStep implements Step<None, None> {
    @Provides
    Variable<UserData> userData;
    
    @Override
    public None invoke(Context<None> context, None input) {
        userData.set(context, new UserData("John", "john@example.com"));
        return None.value();
    }
}

// Step that consumes data
public class DataConsumerStep implements Step<None, String> {
    @Consumes
    Variable<UserData> userData;
    
    @Override
    public String invoke(Context<None> context, None input) {
        UserData data = userData.get(context);
        return "Hello, " + data.name() + "!";
    }
}

// Build flow with state sharing
Flow flow = builder
    .add(new DataProviderStep())
    .add(new DataConsumerStep())
    .build();

String result = flow.invoke();
// result = "Hello, John!"
```

### Concurrent Execution

```java
Flow flow = builder
    .add(new FetchUserDataStep())
    .add(new FetchOrderDataStep())
    .concurrent()
    .add(new ProcessCombinedDataStep())
    .build();

Result result = flow.invoke(userId);
```

## 📚 Documentation

📖 **[Full Documentation](https://habecker.github.io/steppy/)** - Complete guide with examples and API reference

### Key Topics

- **[Concepts](https://habecker.github.io/steppy/concepts/)** - Core concepts and architecture
- **[Getting Started](https://habecker.github.io/steppy/getting-started/)** - Step-by-step tutorial
- **[Flows](https://habecker.github.io/steppy/flows/)** - Building and executing workflows
- **[Branching & Nesting](https://habecker.github.io/steppy/branching/)** - Conditional logic and reusable flows
- **[Streaming](https://habecker.github.io/steppy/streaming/)** - Infinite and finite stream processing
- **[State & Lifecycle](https://habecker.github.io/steppy/state/)** - Managing state and lifecycle events
- **[Configuration](https://habecker.github.io/steppy/configuration/)** - Dependency injection integration
- **[Validation](https://habecker.github.io/steppy/validation/)** - Type safety and error handling

## 🛠️ Development

### Prerequisites

- Java 17 or higher
- Maven 3.6+

### Building from Source

```bash
# Clone the repository
git clone https://github.com/habecker/steppy.git
cd steppy

# Build the project
mvn clean verify

# Run tests
mvn test
```

### Local Development

If you have [just](https://github.com/casey/just) installed:

```bash
# Run tests
just test

# Build with coverage
just test-with-coverage

# Serve documentation locally
just docs-serve

# Build documentation
just docs-build
```

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
