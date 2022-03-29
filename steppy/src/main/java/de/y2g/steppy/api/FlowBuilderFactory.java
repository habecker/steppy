package de.y2g.steppy.api;

public interface FlowBuilderFactory {
    <C, I, R> FlowBuilder<C, I, R> builder(Class<C> configType, Class<I> inputType, Class<R> returnType);
}
