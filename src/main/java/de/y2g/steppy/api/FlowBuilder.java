package de.y2g.steppy.api;

import de.y2g.steppy.api.validation.ValidationException;
import de.y2g.steppy.core.ConcurrentFlowProxy;
import de.y2g.steppy.core.FlowProxy;
import de.y2g.steppy.core.NestedConcurrentFlow;
import de.y2g.steppy.core.NestedSerialFlow;
import de.y2g.steppy.core.RuntimeStepProxy;
import de.y2g.steppy.core.SerialFlowProxy;
import de.y2g.steppy.core.StepIdentifier;
import de.y2g.steppy.core.StepProxy;
import de.y2g.steppy.core.Typing;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

@SuppressWarnings({ "unchecked", "rawtypes" })
public final class FlowBuilder<I, R> {
    private final Executor executor;

    private final StepRepository repository;

    private final Class<I> inputType;

    private final List<StepProxy> steps = new LinkedList<>();

    private Class<R> returnType;

    private boolean concurrent = false;

    public FlowBuilder(Executor executor, StepRepository repository, Class<I> inputType, Class<R> returnType) {
        this.executor = executor;
        this.repository = repository;
        this.inputType = inputType;
        this.returnType = returnType;
    }

    public FlowBuilder<I, R> append(String name) {
        Step step = repository.create(name);
        steps.add(new RuntimeStepProxy(new StepIdentifier(name), step));
        return this;
    }

    public FlowBuilder<I, R> append(Class<? extends Step> stepType) {
        Step step = repository.create(stepType);
        steps.add(new RuntimeStepProxy(new StepIdentifier(stepType.getCanonicalName()), step));
        return this;
    }

    public <T, BR> FlowBuilder<I, R> branch(Class<T> inputType, Class<BR> returnType, Consumer<BranchBuilder<T, ?>> consumer) {
        if (!steps.isEmpty())
            inputType = steps.get(steps.size() - 1).getTyping().getInputType();
        BranchBuilder<T, BR> builder = new BranchBuilder<>(executor, repository, inputType, returnType);
        consumer.accept(builder);
        steps.add(builder.build());
        return this;
    }

    // TODO: add executor as parameter here
    public FlowBuilder<I, R> concurrent() {
        concurrent = true;
        return this;
    }

    // TODO: result handler?
    public <NR> FlowBuilder<I, R> nest(Class<NR> returnType, Consumer<FlowBuilder<?, NR>> consumer) {
        Class<?> nestedInputType = null;
        if (!steps.isEmpty()) {
            nestedInputType = steps.get(steps.size() - 1).getTyping().getReturnType();
        } else {
            nestedInputType = this.inputType;
        }
        FlowBuilder<?, NR> nestedBuilder = new FlowBuilder<>(executor, repository, nestedInputType, returnType);
        consumer.accept(nestedBuilder);
        steps.add(nestedBuilder.buildNested());
        return this;
    }

    public Flow<I, R> build() throws ValidationException {

        if (returnType.equals(None.class))
            returnType = steps.get(steps.size() - 1).getTyping().getReturnType();

        Flow<I, R> flow;
        if (concurrent) {
            flow = new ConcurrentFlowProxy<>(new Typing<>(Configurations.class, inputType, returnType), steps, executor);
            ((ConcurrentFlowProxy)flow).verify();
        } else {
            flow = new SerialFlowProxy<>(new Typing<>(Configurations.class, inputType, returnType), steps);
            ((SerialFlowProxy)flow).verify();
        }
        return flow;
    }

    private StepProxy<Configurations, I, R> buildNested() {
        StepProxy<Configurations, I, R> nestedFlow;
        if (concurrent) {
            nestedFlow = new NestedConcurrentFlow<>(new Typing<>(Configurations.class, inputType, returnType), steps, executor);
        } else {
            nestedFlow = new NestedSerialFlow<>(new Typing<>(Configurations.class, inputType, returnType), steps);
        }
        return nestedFlow;
    }

    FlowProxy<I, R> buildBranched() {
        return new SerialFlowProxy<>(new Typing<>(Configurations.class, inputType, returnType), steps);
    }
}
