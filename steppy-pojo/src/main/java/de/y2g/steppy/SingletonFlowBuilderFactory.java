package de.y2g.steppy;

import de.y2g.steppy.api.FlowBuilder;

import java.util.concurrent.ExecutorService;

public class SingletonFlowBuilderFactory {
    private static PojoFlowBuilderFactory instance = null;

    public static void initialize(ExecutorService executorService) {
        instance = new PojoFlowBuilderFactory(executorService);
    }

    private static PojoFlowBuilderFactory instance() {
        if (instance == null) {
            throw new IllegalArgumentException("SingletonFlowBuilderFactory was not initialized");
        }

        return instance;
    }


    public static <C, I, R> FlowBuilder<C, I, R> builder(Class<C> configType, Class<I> inputType, Class<R> returnType) {
        return instance().builder(configType, inputType, returnType);
    }

    public static <C> FlowBuilder<C, Void, Void> builder(Class<C> configType) {
        return instance().builder(configType, Void.class, Void.class);
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
                    SingletonStepRepository.instance(), configType, inputType, returnType);
        }
    }
}
