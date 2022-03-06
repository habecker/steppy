package de.y2g.steppy;

import de.y2g.steppy.api.FlowBuilder;
import de.y2g.steppy.api.FlowBuilderFactory;
import de.y2g.steppy.api.Result;
import de.y2g.steppy.api.StepRepository;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@Singleton
public class WeldFlowBuilderFactory implements FlowBuilderFactory {
    private final StepRepository stepRepository;
    private final ExecutorService executorService;

    @Inject
    public WeldFlowBuilderFactory(StepRepository stepRepository, ExecutorService executorService) {
        this.stepRepository = stepRepository;
        this.executorService = executorService;
    }


    @Override
    public <C, I, R> FlowBuilder<C, I, R> builder(Class<C> configType, Class<I> inputType, Class<R> returnType) {
        return new FlowBuilder<>(
                supplier -> {
                    CompletableFuture<Result<R>> future = new CompletableFuture<>();
                    executorService.execute(() -> future.complete(supplier.get()));
                    return future;
                },
                stepRepository, configType, inputType, returnType);
    }
}
