package de.y2g.steppy.spring;

import de.y2g.steppy.api.FlowBuilder;
import de.y2g.steppy.api.FlowBuilderFactory;
import de.y2g.steppy.api.Result;
import de.y2g.steppy.api.StepRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class SpringFlowBuilderFactory implements FlowBuilderFactory {
    private final StepRepository stepRepository;
    private final TaskExecutor flowTaskExecutor;

    @Autowired
    public SpringFlowBuilderFactory(StepRepository stepRepository, TaskExecutor flowTaskExecutor) {
        this.stepRepository = stepRepository;
        this.flowTaskExecutor = flowTaskExecutor;
    }

    @Override
    public <C,I,R> FlowBuilder<C,I,R> builder(Class<C> configType, Class<I> inputType, Class<R> returnType)
    {
        return new FlowBuilder<>(
                flowTaskExecutor,
                stepRepository, configType, inputType, returnType);
    }
}
