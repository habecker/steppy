package de.y2g.steppy.core;

import de.y2g.steppy.api.Context;
import de.y2g.steppy.api.Result;
import de.y2g.steppy.api.exception.ExecutionException;
import de.y2g.steppy.api.streaming.Source;
import de.y2g.steppy.api.validation.VerificationError;
import de.y2g.steppy.api.validation.VerificationErrorType;
import de.y2g.steppy.api.validation.VerificationException;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@SuppressWarnings({"unchecked", "rawtypes"})
public abstract class FlowProxy<C,I,R> {
    private final Logger logger;
    private final Typing<C, I, R> typing;


    private final List<StepProxy> steps;

    protected FlowProxy(Typing<C,I,R> typing, @Nonnull List<StepProxy> steps){
        // assert not empty
        this.typing = typing;
        this.steps = steps;
        this.logger = Logger.getLogger(String.format("flow-%s-%s-%s", typing.getConfigType().getSimpleName(), typing.getInputType().getSimpleName(), typing.getReturnType().getSimpleName()));
    }

    protected void callBefore(Context<C> context) throws ExecutionException {
        for (StepProxy step:
             steps
        ) {
            step.onBeforeFlow(context);
        }
    }

    protected void callAfter(Context<C> context) throws ExecutionException {
        for (StepProxy step:
                steps
        ) {
            step.onAfterFlow(context);
        }
    }

    protected void callBefore(Context<C> context, StepProxy step) throws ExecutionException {
        step.onBeforeStep(context);
    }

    protected void callAfter(Context<C> context, StepProxy step) throws ExecutionException {
        step.onAfterStep(context);
    }

    public void verify() throws VerificationException {
        Class<?> configType = typing.getConfigType();
        List<VerificationError> errors = new ArrayList<>();

        steps.forEach(step -> {
            if (!Typing.isConfigTypeCompatible(configType, step.getTyping())){
                errors.add(new VerificationError(VerificationErrorType.CONFIGURATION_TYPE_INCOMPATIBLE, step.getIdentifier()));
            }
        });

        // TODO validate source generic type
        if (!Typing.isInputTypeCompatible(typing.getInputType(), steps.get(0).getTyping())
                && (!(this instanceof NestedSerialFlow && typing.getInputType().equals(Source.class)))
                && (!(this instanceof NestedConcurrentFlow && typing.getInputType().equals(Source.class))))
        {
            errors.add(new VerificationError(VerificationErrorType.FLOW_INPUT_TYPE_INCOMPATIBLE, steps.get(0).getIdentifier()));
        }

        verifySteps(this.steps, errors);
        verifyDependents(this.steps, errors);

        verifyReturnStep(errors);

        if (!errors.isEmpty())
        {
            throw new VerificationException("Flow verification failed with errors.", errors);
        }
    }

    private void verifyDependents(List<StepProxy> steps, List<VerificationError> errors)
    {
        Set<StepIdentifier> stepNames = new HashSet<>();

        for (StepProxy step :
                steps) {
            List<String> dependencies = step.getDependsOn();

            if (!stepNames.containsAll(dependencies.stream().map(StepIdentifier::new).collect(Collectors.toList()))) {
                errors.add(new VerificationError(VerificationErrorType.DEPENDENCIES_UNFULFILLED, step.getIdentifier()));
            }
            stepNames.add(step.getIdentifier());
        }
    }

    private void verifyReturnStep(List<VerificationError> errors) {
        if (!(this instanceof NestedConcurrentFlow) && !(this instanceof NestedSerialFlow) && !Typing.isReturnTypeCompatible(typing.getReturnType(), steps.get(steps.size() - 1).getTyping()))
        {
            errors.add(new VerificationError(VerificationErrorType.FLOW_RETURN_TYPE_INCOMPATIBLE, steps.get(steps.size() - 1).getIdentifier()));
        }
    }

    private void verifySteps(List<StepProxy> steps, List<VerificationError> errors) {
        var iterator = steps.iterator();
        StepProxy current = iterator.next();

        while (iterator.hasNext()){
            StepProxy next = iterator.next();

            if (!Typing.isInputTypeCompatible(current.getTyping(),next.getTyping())){
                errors.add(new VerificationError(VerificationErrorType.STEP_INPUT_TYPE_INCOMPATIBLE, current.getIdentifier(), next.getIdentifier()));
            }

            try {
                if (current instanceof NestedConcurrentFlow)
                {
                    ((NestedConcurrentFlow)current).verify();
                } else if (current instanceof NestedSerialFlow)
                {
                    ((NestedSerialFlow)current).verify();
                }
            } catch (VerificationException e) {
                errors.addAll(e.getErrors());
            }

            current = next;
        }

        try {
            if (current instanceof NestedConcurrentFlow)
            {
                ((NestedConcurrentFlow)current).verify();
            } else if (current instanceof NestedSerialFlow)
            {
                ((NestedSerialFlow)current).verify();
            }
        } catch (VerificationException e) {
            errors.addAll(e.getErrors());
        }
    }


    protected Result<R> invokeSingleItem(Context<C> context, I input) throws ExecutionException {
        Object data = input;
        LinkedList<StepProxy> steps = new LinkedList<>(getSteps());
        while (!steps.isEmpty())
        {
            var current = steps.pop();
            var subContext = context.sub();
            try {
                callBefore(subContext, current);
                //TODO document this behaviour
                if (subContext.isAbort()) {
                    this.logger.log(Level.FINE, String.format("Aborted for input %s by step %s in @Before handler", input, current.getIdentifier()));
                    return null;
                }

                data = current.execute(subContext, data);

            } finally {
                callAfter(subContext, current);
            }
            if (subContext.isAbort()) {
                this.logger.log(Level.FINE, String.format("Aborted for input %s by step %s", input, current.getIdentifier()));
                return new Result<R>(Result.Type.ABORTED);
            }
        }
        return new Result<>((R)data, Result.Type.SUCCEEDED);
    }

    public List<StepProxy> getSteps() {
        return steps;
    }

    public Typing<C, I, R> getTyping() {
        return typing;
    }
}
