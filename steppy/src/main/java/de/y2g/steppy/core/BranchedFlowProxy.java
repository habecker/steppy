package de.y2g.steppy.core;

import de.y2g.steppy.api.Context;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.function.BiPredicate;

public class BranchedFlowProxy<C, I, R> extends FlowProxy<C, I, R> {
    private final BiPredicate<Context<C>, I> predicate;
    private final boolean isElse;

    protected BranchedFlowProxy(BiPredicate<Context<C>, I> predicate, Typing<C, I, R> typing, @NotNull List<StepProxy> steps, boolean isElse) {
        super(typing, steps);
        this.predicate = predicate;
        this.isElse = isElse;
    }

    public BiPredicate<Context<C>, I> getPredicate() {
        return predicate;
    }
}