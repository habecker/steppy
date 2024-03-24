package de.y2g.steppy.api;

public interface FlowBuilderFactory {
    <I, R> FlowBuilder<I, R> builder(Class<I> inputType, Class<R> returnType);
}
