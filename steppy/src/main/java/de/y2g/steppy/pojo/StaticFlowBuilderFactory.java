package de.y2g.steppy.pojo;

import de.y2g.steppy.api.FlowBuilder;

import java.util.concurrent.ExecutorService;

public class StaticFlowBuilderFactory {
    private static PojoFlowBuilderFactory instance = null;

    public static void initialize(ExecutorService executorService) {
        instance = new PojoFlowBuilderFactory(executorService);
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
        private final ExecutorService executorService;

        private PojoFlowBuilderFactory(ExecutorService executorService) {
            this.executorService = executorService;
        }

        @Override
        public <C, I, R> FlowBuilder<C, I, R> builder(Class<C> configType, Class<I> inputType, Class<R> returnType) {
            return new FlowBuilder<>(
                    executorService,
                    StaticStepRepository.instance(), configType, inputType, returnType);
        }
    }
}
