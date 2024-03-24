package de.y2g.steppy.api;

import de.y2g.steppy.core.BranchedFlowProxy;
import de.y2g.steppy.core.StepProxy;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class BranchBuilder<I, R> {
    private final StepRepository repository;

    private final Class<I> inputType;

    private final Class<R> returnType;

    private final Executor executor;

    List<BranchedFlowProxy.PredicatedFlow<I, R>> flows;

    private boolean otherwiseContinue;

    BranchBuilder(Executor executor, StepRepository repository, Class<I> inputType, Class<R> returnType) {
        this.executor = executor;
        this.flows = new ArrayList<>();
        this.repository = repository;
        this.inputType = inputType;
        this.returnType = returnType;
    }

    public BranchBuilder<I, R> when(Predicate<I> predicate, Consumer<FlowBuilder<I, R>> consumer) {
        FlowBuilder<I, R> builder = new FlowBuilder<>(executor, repository, inputType, returnType);
        consumer.accept(builder);
        flows.add(new BranchedFlowProxy.PredicatedFlow<>(predicate, builder.buildBranched()));
        return this;
    }

    public BranchBuilder<I, R> otherwise(Consumer<FlowBuilder<I, R>> consumer) {
        FlowBuilder<I, R> builder = new FlowBuilder<>(executor, repository, inputType, returnType);
        consumer.accept(builder);
        flows.add(new BranchedFlowProxy.PredicatedFlow<>(builder.buildBranched()));
        return this;
    }

    public BranchBuilder<I, R> otherwiseContinue() {
        otherwiseContinue = true;
        return this;
    }

    StepProxy<Configurations, I, R> build() {
        return new BranchedFlowProxy<>(flows, otherwiseContinue);
    }
}
