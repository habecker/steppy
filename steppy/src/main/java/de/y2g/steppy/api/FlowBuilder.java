package de.y2g.steppy.api;

import de.y2g.steppy.api.validation.VerificationException;
import de.y2g.steppy.core.*;

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

    public <T> FlowBuilder<C, I, R> branch(Consumer<BranchBuilder<C, T, ?>> consumer) {
        Class<T> iType = (Class<T>) inputType;

        if (!steps.isEmpty())
            iType = steps.get(steps.size() - 1).getTyping().getInputType();
        BranchBuilder<C, T, ?> builder = new BranchBuilder<>(executor, repository, configType, iType, Any.class);
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
    public FlowBuilder<C, I, R> nest(Consumer<FlowBuilder<C, ?, Void>> consumer) {
        FlowBuilder<C, ?, Void> nestedBuilder = new FlowBuilder<>(executor, repository, configType, steps.get(steps.size() - 1).getTyping().getReturnType(), Void.class);
        consumer.accept(nestedBuilder);
        steps.add(nestedBuilder.buildNested());
        return this;
    }

    public FlowBuilder<C, I, R> nest(Consumer<FlowBuilder<C, ?, Void>> consumer, String resultSupplier) {
        FlowBuilder<C, ?, Void> nestedBuilder = new FlowBuilder<>(executor, repository, configType, steps.get(steps.size() - 1).getTyping().getReturnType(), Void.class);
        consumer.accept(nestedBuilder);
        steps.add(nestedBuilder.buildNested());
        append(resultSupplier);
        return this;
    }

    // TODO: doc that this is always executed AFTER the flow and subsequent addings must fail
    public FlowBuilder<C, I, R> repeat(BiPredicate<Context<C>, R> predicate) {
        if (this.repetitionPredicate == null) {
            throw new IllegalStateException("Predicate was already set");
        }
        if (!this.inputType.equals(this.returnType)) {
            throw new IllegalStateException("Cannot use repetition as the input type does not match the return type");
        }
        this.repetitionPredicate = predicate;
        return this;
    }

    public Flow<C, I, R> build() throws VerificationException {

        if (returnType.equals(Any.class))
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

    private StepProxy<C, Object, Void> buildNested() {
        StepProxy<C, Object, Void> nestedFlow;
        if (concurrent) {
            nestedFlow = new NestedConcurrentFlow<>(new Typing<>(configType, inputType, returnType), steps, executor);
        } else {
            nestedFlow = new NestedSerialFlow<>(new Typing<>(configType, inputType, returnType), steps);
        }
        return nestedFlow;
    }

    protected FlowProxy<C, I, R> buildBranched() {
        return new SerialFlowProxy<>(new Typing<>(configType, inputType, returnType), steps);
    }

}
