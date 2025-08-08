# Streaming

Besides processing finite inputs, flows can continuously pull values from a `Source` and forward the results to a `Sink`. This enables long running pipelines for message queues, file processing or real time feeds.

```java
var flow = StaticFlowBuilderFactory
    .builder(String.class, String.class)
    .append(AppendAStep.class)
    .append(AppendBStep.class)
    .build();

var source = new SimpleSource<>(Stream.of("", "C"));
var sink = new SimpleSink<String>();
flow.stream(source, sink);
// sink.getResult() == ["AB", "CAB"]
```

The flow keeps requesting values until the source is exhausted. Streaming works for sequential and concurrent flows and nested streaming is supported as well.
