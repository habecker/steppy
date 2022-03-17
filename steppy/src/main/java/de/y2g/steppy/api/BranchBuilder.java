package de.y2g.steppy.api;

import de.y2g.steppy.core.BranchedFlow;
import de.y2g.steppy.core.BranchedFlowProxy;
import de.y2g.steppy.core.StepProxy;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

public class BranchBuilder<C,I,R> {
    List<BranchedFlow.PredicatedFlow<C,I,R>> flows;
    private StepRepository repository;
    private Class<C> configType;
    private Class<I> inputType;
    private Class<R> returnType;
    private Executor executor;
    private boolean otherwiseContinue;

    BranchBuilder(Executor executor, StepRepository repository, Class<C> configType, Class<I> inputType, Class<R> returnType) {
        this.executor = executor;
        this.flows = new ArrayList<>();
        this.repository = repository;
        this.configType = configType;
        this.inputType = inputType;
        this.returnType = returnType;
    }

    public BranchBuilder<C,I,R> when(BiPredicate<Context<C>, I> predicate, Consumer<FlowBuilder<C,I,R>> consumer) {
        FlowBuilder<C,I,R> builder = new FlowBuilder<>(executor, repository, configType, inputType, returnType);
        consumer.accept(builder);
        flows.add(new BranchedFlow.PredicatedFlow<C,I,R>(predicate, builder.buildBranched()));
        return this;
    }

    public BranchBuilder<C,I,R> otherwise(Consumer<FlowBuilder<C,I,R>> consumer) {
        FlowBuilder<C,I,R> builder = new FlowBuilder<>(executor, repository, configType, inputType, returnType);
        consumer.accept(builder);
        flows.add(new BranchedFlow.PredicatedFlow<C,I,R>(builder.buildBranched()));
        return this;
    }

    public BranchBuilder<C,I,R> otherwiseContinue() {
        otherwiseContinue = true;
        return this;
    }

    StepProxy<C,I,R> build()
    {
        return new BranchedFlow<>(flows, otherwiseContinue);
    }
}
