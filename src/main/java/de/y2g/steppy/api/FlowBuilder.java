package de.y2g.steppy.api;

import de.y2g.steppy.api.validation.ValidationEception;
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
import java.util.function.BiPredicate;
import java.util.function.Consumer;

@SuppressWarnings({"unchecked", "rawtypes"})
public final class FlowBuilder<C, I, R> {
    private final Executor executor;
    private final StepRepository repository;
    private final Class<C> configType;
    private final Class<I> inputType;
    private Class<R> returnType;
    private final List<StepProxy> steps = new LinkedList<>();
    private boolean concurrent = false;
    private BiPredicate<Context<C>, R> repetitionPredicate;

    public FlowBuilder(Executor executor, StepRepository repository, Class<C> configType, Class<I> inputType, Class<R> returnType) {
        this.executor = executor;
        this.repository = repository;
        this.configType = configType;
        this.inputType = inputType;
        this.returnType = returnType;
    }

    public FlowBuilder<C, I, R> append(String name) {
        Step step = repository.create(name);
        steps.add(new RuntimeStepProxy(new StepIdentifier(name), step));
        return this;
    }

    public FlowBuilder<C, I, R> append(Class<? extends Step> stepType) {
        return append(stepType.getCanonicalName());
    }
    public <T, BR> FlowBuilder<C, I, R> branch(Class<T> inputType, Class<BR> returnType, Consumer<BranchBuilder<C, T, ?>> consumer) {
        if (!steps.isEmpty())
            inputType = steps.get(steps.size() - 1).getTyping().getInputType();
        BranchBuilder<C, T, BR> builder = new BranchBuilder<>(executor, repository, configType, inputType, returnType);
        consumer.accept(builder);
        steps.add(builder.build());
        return this;
    }

    // TODO: add executor as parameter here
    public FlowBuilder<C, I, R> concurrent() {
        concurrent = true;
        return this;
    }

    // TODO: result handler?
    public <NR> FlowBuilder<C, I, R> nest(Class<NR> returnType, Consumer<FlowBuilder<C, ?, NR>> consumer) {
        FlowBuilder<C, ?, NR> nestedBuilder = new FlowBuilder<>(executor, repository, configType, steps.get(steps.size() - 1).getTyping().getReturnType(), returnType);
        consumer.accept(nestedBuilder);
        steps.add(nestedBuilder.buildNested());
        return this;
    }

    public Flow<C, I, R> build() throws ValidationEception {

        if (returnType.equals(None.class))
            returnType = steps.get(steps.size() - 1).getTyping().getReturnType();

        Flow<C, I, R> flow;
        if (concurrent) {
            flow = new ConcurrentFlowProxy<>(new Typing<>(configType, inputType, returnType), steps, executor);
            ((ConcurrentFlowProxy) flow).verify();
        } else {
            flow = new SerialFlowProxy<>(new Typing<>(configType, inputType, returnType), steps);
            ((SerialFlowProxy) flow).verify();
        }
        return flow;
    }

    private StepProxy<C, I, R> buildNested() {
        StepProxy<C, I, R> nestedFlow;
        if (concurrent) {
            nestedFlow = new NestedConcurrentFlow<>(new Typing<>(configType, inputType, returnType), steps, executor);
        } else {
            nestedFlow = new NestedSerialFlow<>(new Typing<>(configType, inputType, returnType), steps);
        }
        return nestedFlow;
    }

    FlowProxy<C, I, R> buildBranched() {
        return new SerialFlowProxy<>(new Typing<>(configType, inputType, returnType), steps);
    }
}
