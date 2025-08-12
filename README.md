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
import de.y2g.steppy.pojo.StaticFlowBuilderFactory;
import de.y2g.steppy.pojo.StaticStepRepository;
import java.util.concurrent.Executors;

// Define your steps
public class StringLengthStep implements Step<None, String, Integer> {
    @Override
    public Integer invoke(Context<None> context, String input) {
        return input.length();
    }
}

// Register steps and initialize
StaticStepRepository.register(StringLengthStep.class);
StaticFlowBuilderFactory.initialize(Executors.newFixedThreadPool(4));

// Build and execute a flow
var flow = StaticFlowBuilderFactory
    .builder(String.class, Integer.class)
    .append(StringLengthStep.class)
    .build();

Result<Integer> result = flow.invoke("Hello, Steppy!");
// result.getResult() = 14
```

### State Management Example

```java
import de.y2g.steppy.api.*;
import de.y2g.steppy.pojo.StaticFlowBuilderFactory;
import de.y2g.steppy.pojo.StaticStepRepository;
import java.util.concurrent.Executors;

// Step that provides data
public class DataProviderStep implements Step<None, None, None> {
    @Provides
    Variable<UserData> userData;
    
    @Override
    public None invoke(Context<None> context, None input) {
        userData.set(context, new UserData("John", "john@example.com"));
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

// Register steps and initialize
StaticStepRepository.register(DataProviderStep.class, DataConsumerStep.class);
StaticFlowBuilderFactory.initialize(Executors.newFixedThreadPool(4));

// Build flow with state sharing
var flow = StaticFlowBuilderFactory
    .builder(None.class, String.class)
    .append(DataProviderStep.class)
    .append(DataConsumerStep.class)
    .build();

Result<String> result = flow.invoke();
// result.getResult() = "Hello, John!"
```

### Concurrent Execution

```java
import de.y2g.steppy.api.*;
import de.y2g.steppy.pojo.StaticFlowBuilderFactory;
import de.y2g.steppy.pojo.StaticStepRepository;
import java.util.concurrent.Executors;

// Register steps and initialize
StaticStepRepository.register(FetchUserDataStep.class, FetchOrderDataStep.class, ProcessCombinedDataStep.class);
StaticFlowBuilderFactory.initialize(Executors.newFixedThreadPool(4));

var flow = StaticFlowBuilderFactory
    .builder(UserId.class, CombinedData.class)
    .append(FetchUserDataStep.class)
    .append(FetchOrderDataStep.class)
    .concurrent()
    .append(ProcessCombinedDataStep.class)
    .build();

Result<CombinedData> result = flow.invoke(userId);
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
