package de.y2g.steppy.cdi;

import de.y2g.steppy.api.FlowBuilder;
import de.y2g.steppy.api.FlowBuilderFactory;
import de.y2g.steppy.api.StepRepository;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import java.util.concurrent.ExecutorService;

@Dependent
public class CdiFlowBuilderFactory implements FlowBuilderFactory {
    @Inject
    private StepRepository stepRepository;

    @Inject
    private ExecutorService executorService;

    @Override
    public <C, I, R> FlowBuilder<C, I, R> builder(Class<C> configType, Class<I> inputType, Class<R> returnType) {
        return new FlowBuilder<>(executorService, stepRepository, configType, inputType, returnType);
    }
}
