package de.y2g.steppy.core;

import de.y2g.steppy.api.Context;
import de.y2g.steppy.api.exception.ExecutionException;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.BiPredicate;

// TODO: veriy otherwise continue
public class BranchedFlowProxy<C, I, R> implements StepProxy<C, I, R> {
    private final Typing<C, I, R> stepTyping;
    private final List<PredicatedFlow<C, I, R>> flows;
    private final boolean otherwiseContinue;

    public BranchedFlowProxy(List<PredicatedFlow<C, I, R>> flows, boolean otherwiseContinue) {
        // TODO throw exception if flows is empty
        this.stepTyping = flows.get(0).flow.getTyping();
        this.flows = flows;
        this.otherwiseContinue = otherwiseContinue;
    }

    @Override
    public void onBeforeFlow(Context<C> context) throws ExecutionException {
        for (var flow : flows) {
            flow.flow.callBefore(context);
        }
    }

    @Override
    public void onAfterFlow(Context<C> context) throws ExecutionException {
        for (var flow : flows) {
            flow.flow.callAfter(context);
        }
    }

    @Override
    public void onBeforeStep(Context<C> context) throws ExecutionException {
        // do nothing
    }

    @Override
    public void onAfterStep(Context<C> context) throws ExecutionException {
        // do nothing
    }

    @Override
    public Typing getTyping() {
        return stepTyping;
    }

    @Override
    public R execute(Context<C> context, I input) throws ExecutionException {

        var flow = flows.stream().filter(f -> f.predicate != null && f.predicate.test(context, input)).findFirst();

        if (!flow.isPresent())
            flow = flows.stream().filter(f -> f.isElse).findFirst();

        if (!flow.isPresent() && otherwiseContinue)
            return (R) input;

        if (!flow.isPresent())
            throw new ExecutionException("No branch found with fulfilled predicate.");

        var flowInstance = flow.get().flow;

        return flowInstance.invokeSingleItem(context, input).getResult();
    }

    @Override
    public StepIdentifier getIdentifier() {
        return new StepIdentifier(UUID.randomUUID() + "-nested-flow");
    }

    public static class PredicatedFlow<C, I, R> {
        private final FlowProxy<C, I, R> flow;
        private BiPredicate<Context<C>, I> predicate;
        private boolean isElse;

        public PredicatedFlow(BiPredicate<Context<C>, I> predicate, FlowProxy<C, I, R> flow) {
            this.flow = flow;
            this.predicate = predicate;
        }

        public PredicatedFlow(FlowProxy<C, I, R> flow) {
            this.flow = flow;
            isElse = true;
        }
    }
}
