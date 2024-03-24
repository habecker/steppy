package de.y2g.steppy.core;

import de.y2g.steppy.api.Configurations;
import de.y2g.steppy.api.Context;
import de.y2g.steppy.api.None;
import de.y2g.steppy.api.Result;
import de.y2g.steppy.api.exception.ExecutionException;
import de.y2g.steppy.api.exception.MissingConfigurationException;
import de.y2g.steppy.api.streaming.Source;
import de.y2g.steppy.api.validation.ValidationError;
import de.y2g.steppy.api.validation.ValidationErrorType;
import de.y2g.steppy.api.validation.ValidationException;
import jakarta.validation.constraints.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings({ "unchecked", "rawtypes" })
public abstract class FlowProxy<I, R> implements Verifiable {
    private final Logger logger;

    private final Typing<Configurations, I, R> typing;

    private final List<StepProxy> steps;

    protected FlowProxy(Typing<Configurations, I, R> typing, @NotNull List<StepProxy> steps) {
        // assert not empty
        this.typing = typing;
        this.steps = steps;
        this.logger = Logger.getLogger(
            String.format("flow-%s-%s-%s", typing.getConfigType().getSimpleName(), typing.getInputType().getSimpleName(),
                typing.getReturnType().getSimpleName()));
    }

    private static void verifyWhenVerifiable(List<ValidationError> errors, StepProxy current) {
        try {
            if (current instanceof Verifiable verifiable) {
                verifiable.verify();
            }
        } catch (ValidationException e) {
            errors.addAll(e.getErrors());
        }
    }

    protected void callBefore(Context<Configurations> context) throws ExecutionException {
        for (StepProxy step: steps) {
            step.onBeforeFlow(context.sub(System.identityHashCode(step) + "", step.getTyping().getConfigType()));
        }
    }

    protected void callAfter(Context<Configurations> context) throws ExecutionException {
        for (StepProxy step: steps) {
            step.onAfterFlow(context.sub(System.identityHashCode(step) + "", step.getTyping().getConfigType()));
        }
    }

    protected void callBefore(Context<Configurations> context, StepProxy step) throws ExecutionException {
        step.onBeforeStep(context);
    }

    protected void callAfter(Context<Configurations> context, StepProxy step) throws ExecutionException {
        step.onAfterStep(context);
    }

    @Override
    public void verify() throws ValidationException {
        Class<?> configType = typing.getConfigType();
        List<ValidationError> errors = new ArrayList<>();

        /*steps.forEach(step -> {
            if (!Typing.isConfigTypeCompatible(configType, step.getTyping())) {
                errors.add(new ValidationError(ValidationErrorType.CONFIGURATION_TYPE_INCOMPATIBLE, step.getIdentifier()));
            }
        });*/

        // TODO validate source generic type
        if (!Typing.isInputTypeCompatible(steps.get(0).getTyping().getInputType(),
            typing) && (!(this instanceof NestedSerialFlow && typing.getInputType()
            .equals(Source.class))) && (!(this instanceof NestedConcurrentFlow && typing.getInputType().equals(Source.class)))) {
            errors.add(new ValidationError(ValidationErrorType.FLOW_INPUT_TYPE_INCOMPATIBLE, steps.get(0).getIdentifier()));
        }

        verifySteps(this.steps, errors);

        verifyReturnStep(errors);

        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }

    private void verifyReturnStep(List<ValidationError> errors) {
        if (!(this instanceof NestedConcurrentFlow) && !(this instanceof NestedSerialFlow) && !Typing.isReturnTypeCompatible(
            typing.getReturnType(), steps.get(steps.size() - 1).getTyping())) {
            errors.add(new ValidationError(ValidationErrorType.FLOW_RETURN_TYPE_INCOMPATIBLE, steps.get(steps.size() - 1).getIdentifier()));
        }
    }

    private void verifySteps(List<StepProxy> steps, List<ValidationError> errors) {
        var iterator = steps.iterator();
        StepProxy current = iterator.next();

        while (iterator.hasNext()) {
            StepProxy next = iterator.next();

            if (!Typing.isInputTypeCompatible(current.getTyping(), next.getTyping())) {
                errors.add(
                    new ValidationError(ValidationErrorType.STEP_INPUT_TYPE_INCOMPATIBLE, current.getIdentifier(), next.getIdentifier()));
            }

            verifyWhenVerifiable(errors, current);

            current = next;
        }

        verifyWhenVerifiable(errors, current);
    }

    protected Result<R> invokeSingleItem(Context<Configurations> context, I input) throws ExecutionException {
        Object data = input;
        LinkedList<StepProxy> steps = new LinkedList<>(getSteps());
        while (!steps.isEmpty()) {
            var current = steps.pop();
            var subContext = context.sub(System.identityHashCode(current) + "", current.getTyping().getConfigType());
            try {
                callBefore(subContext, current);
                //TODO document this behaviour
                if (subContext.isAbort()) {
                    this.logger.log(Level.FINE,
                        String.format("Aborted for input %s by step %s in @Before handler", input, current.getIdentifier()));
                    return new Result<R>(Result.Type.ABORTED);
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

    public Typing<Configurations, I, R> getTyping() {
        return typing;
    }

    private boolean addDefaultConfiguration(Configurations configurations, Class<?> configType) {
        try {
            var defaultConstructor = configType.getDeclaredConstructor();
            defaultConstructor.setAccessible(true);
            var defaultConfig = defaultConstructor.newInstance();
            configurations.put(defaultConfig);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
            logger.log(Level.SEVERE, "Error while adding default configuration", e);
            return false;
        }
    }

    protected final void preprocessConfiguration(Configurations configurations) throws MissingConfigurationException {
        var missingConfigurations = new ArrayList<Class<?>>();
        for (StepProxy step: steps) {
            var configType = step.getTyping().getConfigType();
            if (!None.class.isAssignableFrom(configType) && !configType.isAssignableFrom(Object.class) && !configType.equals(
                Configurations.class) && !configurations.contains(configType) && !addDefaultConfiguration(configurations, configType)) {
                missingConfigurations.add(step.getTyping().getConfigType());
            }
        }
        if (!missingConfigurations.isEmpty()) {
            throw new MissingConfigurationException("Missing configuration", missingConfigurations);
        }
    }
}
