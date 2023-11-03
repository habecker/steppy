package de.y2g.steppy.pojo;

import de.y2g.steppy.api.FlowBuilder;

import java.util.concurrent.Executor;

public class StaticFlowBuilderFactory {
    private static PojoFlowBuilderFactory instance = null;

    public static void initialize(Executor executor) {
        instance = new PojoFlowBuilderFactory(executor);
    }

    private static PojoFlowBuilderFactory instance() {
        if (instance == null) {
            throw new IllegalStateException("StaticFlowBuilderFactory was not initialized");
        }

        return instance;
    }

    static void reset() {
        instance = null;
    }

    public static <C, I, R> FlowBuilder<C, I, R> builder(Class<C> configType, Class<I> inputType, Class<R> returnType) {
        return instance().builder(configType, inputType, returnType);
    }

    private static class PojoFlowBuilderFactory implements de.y2g.steppy.api.FlowBuilderFactory {
        private final Executor executor;

        private PojoFlowBuilderFactory(Executor executor) {
            this.executor = executor;
        }

        @Override
        public <C, I, R> FlowBuilder<C, I, R> builder(Class<C> configType, Class<I> inputType, Class<R> returnType) {
            return new FlowBuilder<>(executor, StaticStepRepository.instance(), configType, inputType, returnType);
        }
    }
}
