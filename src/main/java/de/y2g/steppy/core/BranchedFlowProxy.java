package de.y2g.steppy.core;

import de.y2g.steppy.api.Context;
import de.y2g.steppy.api.exception.ExecutionException;
import de.y2g.steppy.api.validation.ValidationException;

import java.util.List;
import java.util.UUID;
import java.util.function.BiPredicate;

// TODO: veriy otherwise continue
@SuppressWarnings("unchecked")
public class BranchedFlowProxy<C, I, R> implements StepProxy<C, I, R>, Verifiable {

    private final Typing<C, I, R> stepTyping;

    private final List<PredicatedFlow<C, I, R>> flows;

    private final boolean otherwiseContinue;

    private final UUID uuid = UUID.randomUUID();

    public BranchedFlowProxy(List<PredicatedFlow<C, I, R>> flows, boolean otherwiseContinue) {
        // TODO throw exception if flows is empty
        this.stepTyping = flows.get(0).flow.getTyping();
        this.flows = flows;
        this.otherwiseContinue = otherwiseContinue;
    }

    @Override
    public void onBeforeFlow(Context<C> context) throws ExecutionException {
        for (var flow: flows) {
            flow.flow.callBefore(context);
        }
    }

    @Override
    public void onAfterFlow(Context<C> context) throws ExecutionException {
        for (var flow: flows) {
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
    public Typing<C, I, R> getTyping() {
        return stepTyping;
    }

    @Override
    public R execute(Context<C> context, I input) throws ExecutionException {

        var flow = flows.stream().filter(f -> f.predicate != null && f.predicate.test(context, input)).findFirst();

        if (flow.isEmpty())
            flow = flows.stream().filter(f -> f.isElse).findFirst();

        if (flow.isEmpty() && otherwiseContinue)
            return (R)input;

        if (flow.isEmpty())
            throw new ExecutionException("No branch found with fulfilled predicate.");

        var flowInstance = flow.get().flow;

        return flowInstance.invokeSingleItem(context, input).getResult();
    }

    @Override
    public StepIdentifier getIdentifier() {
        return new StepIdentifier(uuid + "-branched-flow");
    }

    @Override
    public void verify() throws ValidationException {
        // TODO: verify that R assignable from I when otherwiseContinue
        Verifiable.verifyAll(flows);
    }

    public static class PredicatedFlow<C, I, R> implements Verifiable {
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

        @Override
        public void verify() throws ValidationException {
            flow.verify();
        }
    }
}
