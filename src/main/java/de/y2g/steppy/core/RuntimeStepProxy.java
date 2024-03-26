package de.y2g.steppy.core;

import de.y2g.steppy.api.After;
import de.y2g.steppy.api.Before;
import de.y2g.steppy.api.Consumes;
import de.y2g.steppy.api.Context;
import de.y2g.steppy.api.Scope;
import de.y2g.steppy.api.State;
import de.y2g.steppy.api.Step;
import de.y2g.steppy.api.Variable;
import de.y2g.steppy.api.exception.ExecutionException;
import jakarta.validation.constraints.Null;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;

@SuppressWarnings({ "unchecked", "rawtypes" })
public final class RuntimeStepProxy<C, I, R> implements StepProxy<C, I, R> {

    private final StepIdentifier identifier;

    private final Step<C, I, R> delegate;

    private final Typing<C, I, R> typing;

    private final List<Method> beforeStepMethods;

    private final List<Method> afterStepMethods;

    private final List<Method> beforeFlowMethods;

    private final List<Method> afterFlowMethods;

    private final Lock lock = new ReentrantLock();

    private final List<Dependency> dependencies = new ArrayList<>();

    public RuntimeStepProxy(StepIdentifier identifier, Step<C, I, R> step) {
        this.identifier = identifier;
        this.delegate = step;

        beforeStepMethods = ReflectionUtils.findMethodsByAnnotation(delegate.getClass(), Before.class, a -> a.value() == Scope.STEP,
            Context.class);
        beforeStepMethods.addAll(ReflectionUtils.findMethodsByAnnotation(delegate.getClass(), Before.class, a -> a.value() == Scope.STEP));

        afterStepMethods = ReflectionUtils.findMethodsByAnnotation(delegate.getClass(), After.class, a -> a.value() == Scope.STEP,
            Context.class);
        afterStepMethods.addAll(ReflectionUtils.findMethodsByAnnotation(delegate.getClass(), After.class, a -> a.value() == Scope.STEP));

        beforeFlowMethods = ReflectionUtils.findMethodsByAnnotation(delegate.getClass(), Before.class, a -> a.value() == Scope.FLOW,
            Context.class);
        beforeFlowMethods.addAll(ReflectionUtils.findMethodsByAnnotation(delegate.getClass(), Before.class, a -> a.value() == Scope.FLOW));

        afterFlowMethods = ReflectionUtils.findMethodsByAnnotation(delegate.getClass(), After.class, a -> a.value() == Scope.FLOW,
            Context.class);
        afterFlowMethods.addAll(ReflectionUtils.findMethodsByAnnotation(delegate.getClass(), After.class, a -> a.value() == Scope.FLOW));

        // get step interface
        var stepType = findParameterizedStepType(step.getClass());

        Class<?> inputType = Object.class;
        Class<?> returnType = Object.class;
        Class<?> configType = Object.class;

        Type[] actualTypeArguments = stepType.getActualTypeArguments();
        try {
            if (actualTypeArguments[1] instanceof ParameterizedType)
                inputType = Class.forName(((ParameterizedType)actualTypeArguments[1]).getRawType().getTypeName());
            else
                inputType = Class.forName(actualTypeArguments[1].getTypeName());

            if (actualTypeArguments[2] instanceof ParameterizedType)
                returnType = Class.forName(((ParameterizedType)actualTypeArguments[2]).getRawType().getTypeName());
            else
                returnType = Class.forName(actualTypeArguments[2].getTypeName());

            if (actualTypeArguments[0] instanceof ParameterizedType)
                configType = Class.forName(((ParameterizedType)actualTypeArguments[0]).getRawType().getTypeName());
            else
                configType = Class.forName(actualTypeArguments[0].getTypeName());
        } catch (ClassNotFoundException e) {
            throw new UnsupportedOperationException("Unexpected error: ", e);
        }
        typing = new Typing<>((Class<C>)configType, (Class<I>)inputType, (Class<R>)returnType);

        Field[] fields = delegate.getClass().getDeclaredFields();
        for (Field field: fields) {
            State state = ReflectionUtils.getAnnotation(field, State.class);
            if (state != null && field.getType().equals(Variable.class)) {
                String fieldName = field.getName();

                if (!state.name().isEmpty()) {
                    fieldName = state.name();
                }

                Class<Variable> type = (Class<Variable>)field.getType();
                try {
                    // TODO: document behaviour
                    Variable variable = type.getDeclaredConstructor(Scope.class, String.class).newInstance(state.scope(), fieldName);
                    boolean isAccessible = field.canAccess(delegate);
                    field.setAccessible(true);
                    field.set(delegate, variable);
                    field.setAccessible(isAccessible);
                } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException |
                    NoSuchMethodException | SecurityException e) {
                    throw new IllegalStateException(e);
                }
            }

            Consumes consumes = ReflectionUtils.getAnnotation(field, Consumes.class);
            if (consumes != null) {
                dependencies.add(new Dependency(consumes.name(), field.getType()));
            }
        }
    }

    private static ParameterizedType findParameterizedStepType(final Class<?> clazz) {
        Objects.requireNonNull(clazz, "Class must not be null.");

        Class<?> current = clazz;
        ParameterizedType type = getParameterizedStepType(current);
        while (current != null && type == null) {
            current = current.getSuperclass();
            type = getParameterizedStepType(current);
        }
        if (type == null) {
            throw new IllegalArgumentException("Step " + clazz.getCanonicalName() + " does not implement parameterized step interface.");
        }
        return type;
    }

    @Null
    private static ParameterizedType getParameterizedStepType(final Class<?> clazz) {
        if (clazz == null)
            return null;

        var types = Stream.of(clazz.getGenericInterfaces())
            .filter(i -> i instanceof ParameterizedType && ((ParameterizedType)i).getRawType().equals(Step.class)).toList();

        if (types.isEmpty())
            return null;
        else if (types.size() > 1)
            throw new IllegalArgumentException("Step implements Step interface multiple times.");

        return (ParameterizedType)types.get(0);
    }

    @Override
    public Typing<C, I, R> getTyping() {
        return typing;
    }

    @Override
    public R execute(Context<C> context, I input) throws ExecutionException {
        return delegate.invoke(context, input);
    }

    @Override
    public void onBeforeFlow(Context<C> context) throws ExecutionException {
        invokeCallbacks(beforeFlowMethods, context);
    }

    @Override
    public void onAfterFlow(Context<C> context) throws ExecutionException {
        invokeCallbacks(afterFlowMethods, context);
    }

    @Override
    public void onBeforeStep(Context<C> context) throws ExecutionException {
        invokeCallbacks(beforeStepMethods, context);
    }

    @Override
    public void onAfterStep(Context<C> context) throws ExecutionException {
        invokeCallbacks(afterStepMethods, context);
    }

    private void invokeCallbacks(List<Method> methods, Context<C> context) throws ExecutionException {
        try {
            for (Method method: methods) {
                method.setAccessible(true);
                if (method.getParameterCount() == 0) {
                    method.invoke(delegate);
                } else {
                    method.invoke(delegate, context);
                }
            }
        } catch (InvocationTargetException e) {
            throw new ExecutionException("An unknown error occurred during lifecycle callbacks.", e.getTargetException());
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("An internal error occurred during lifecycle callbacks.", e);
        }
    }

    @Override
    public StepIdentifier getIdentifier() {
        return identifier;
    }

    public List<Dependency> getDependencies() {
        return dependencies;
    }
}
