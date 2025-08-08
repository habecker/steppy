# Steppy

**Steppy** is a tiny framework for composing small, type-safe Java steps into robust workflows called _flows_. Each step declares the types it consumes and produces and the builder wires them into an executable pipeline.

The library focuses on readability and testability. You assemble flows programmatically without external descriptors and run them sequentially, concurrently or even as an infinite stream.

## What you can do

- Run steps sequentially or in parallel
- Branch to conditional sub-flows and nest reusable pipelines
- Stream results from sources to sinks
- Share state and hook into flow lifecycle
- Inject configuration and integrate with DI containers
- Validate type compatibility and capture errors

Jump to the chapters above to dive into each capability in detail.
