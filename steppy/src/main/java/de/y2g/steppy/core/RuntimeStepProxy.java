package de.y2g.steppy.core;

import de.y2g.steppy.api.After;
import de.y2g.steppy.api.Before;
import de.y2g.steppy.api.Concurrency;
import de.y2g.steppy.api.Context;
import de.y2g.steppy.api.DependsOn;
import de.y2g.steppy.api.Phase;
import de.y2g.steppy.api.State;
import de.y2g.steppy.api.Step;
import de.y2g.steppy.api.Variable;
import de.y2g.steppy.api.exception.ExecutionException;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@SuppressWarnings({"unchecked", "rawtypes"})
public final class RuntimeStepProxy<C, I, R> implements StepProxy<C, I, R> {
    private static final String GLOBAL_STATE_SCOPE = "global";
    private final StepIdentifier identifier;
    private final Step<C, I, R> delegate;
    private final Typing<C, I, R> typing;
    private final List<Method> beforeStepMethods;
    private final List<Method> afterStepMethods;
    private final List<Method> beforeFlowMethods;
    private final List<Method> afterFlowMethods;
    private final Concurrency.Type concurrencyType;
    private final Lock lock = new ReentrantLock();
    private final List<String> dependsOn;

    public RuntimeStepProxy(StepIdentifier identifier, Step<C, I, R> step) {
        this.identifier = identifier;
        this.delegate = step;


        DependsOn dependsOn = step.getClass().getAnnotation(DependsOn.class);
        if (dependsOn != null) {
            this.dependsOn = Arrays.asList(dependsOn.value());
        } else {
            this.dependsOn = Collections.emptyList();
        }

        Concurrency concurrency = step.getClass().getAnnotation(Concurrency.class);
        if (concurrency != null) {
            concurrencyType = concurrency.value();
        } else {
            concurrencyType = Concurrency.Type.ALLOW;
        }

        beforeStepMethods = ReflectionUtils.findMethodsByAnnotation(delegate.getClass(), Before.class,
                a -> a.value() == Phase.STEP, Context.class);
        beforeStepMethods.addAll(ReflectionUtils.findMethodsByAnnotation(getClass(), Before.class,
                a -> a.value() == Phase.STEP));

        afterStepMethods = ReflectionUtils.findMethodsByAnnotation(delegate.getClass(), After.class,
                a -> a.value() == Phase.STEP, Context.class);
        afterStepMethods.addAll(ReflectionUtils.findMethodsByAnnotation(getClass(), After.class,
                a -> a.value() == Phase.STEP));

        beforeFlowMethods = ReflectionUtils.findMethodsByAnnotation(delegate.getClass(), Before.class,
                a -> a.value() == Phase.FLOW, Context.class);
        beforeFlowMethods.addAll(ReflectionUtils.findMethodsByAnnotation(getClass(), Before.class,
                a -> a.value() == Phase.FLOW));

        afterFlowMethods = ReflectionUtils.findMethodsByAnnotation(delegate.getClass(), After.class,
                a -> a.value() == Phase.FLOW, Context.class);
        afterFlowMethods.addAll(ReflectionUtils.findMethodsByAnnotation(getClass(), After.class,
                a -> a.value() == Phase.FLOW));

        var types = step.getClass().getGenericInterfaces();

        Class<?> inputType = Object.class;
        Class<?> returnType = Object.class;
        Class<?> configType = Object.class;

        for (Type type : types) {
            if (type instanceof ParameterizedType && ((ParameterizedType) type).getRawType().equals(Step.class)) {
                var pt = (ParameterizedType) type;
                Type[] actualTypeArguments = pt.getActualTypeArguments();
                try {
                    if (actualTypeArguments[1] instanceof ParameterizedType)
                        inputType = Class.forName(((ParameterizedType) actualTypeArguments[1]).getRawType().getTypeName());
                    else
                        inputType = Class.forName(actualTypeArguments[1].getTypeName());

                    if (actualTypeArguments[2] instanceof ParameterizedType)
                        returnType = Class.forName(((ParameterizedType) actualTypeArguments[2]).getRawType().getTypeName());
                    else
                        returnType = Class.forName(actualTypeArguments[2].getTypeName());

                    if (actualTypeArguments[0] instanceof ParameterizedType)
                        configType = Class.forName(((ParameterizedType) actualTypeArguments[0]).getRawType().getTypeName());
                    else
                        configType = Class.forName(actualTypeArguments[0].getTypeName());
                } catch (ClassNotFoundException e) {
                    throw new UnsupportedOperationException("Unexpected error: ", e);
                }
            }
        }
        typing = new Typing<>((Class<C>) configType, (Class<I>) inputType, (Class<R>) returnType);

        Field[] fields = delegate.getClass().getDeclaredFields();
        for (Field field : fields) {
            State state = field.getAnnotation(State.class);
            if (state != null && field.getType()
                    .equals(Variable.class)) {
                String fieldName = field.getName();

                if (!state.name().isEmpty()) {
                    fieldName = state.name();
                }

                Class<Variable> type = (Class<Variable>) field.getType();
                try {
                    String scopeIdentifier = GLOBAL_STATE_SCOPE;

                    if (!state.global())
                        scopeIdentifier = String.valueOf(System.identityHashCode(this));

                    // TODO: document behaviour
                    Variable variable = type.getConstructor(String.class, String.class)
                            .newInstance(scopeIdentifier, fieldName + "-" + field.getGenericType().getTypeName());
                    boolean isAccessible = field.canAccess(delegate);
                    field.setAccessible(true);
                    field.set(delegate, variable);
                    field.setAccessible(isAccessible);
                } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                        | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                    throw new IllegalStateException(e);
                }
            }
        }
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
        if (concurrencyType == Concurrency.Type.LOCK) {
            lock.lock();
        }

        invokeCallbacks(beforeStepMethods, context);
    }

    @Override
    public void onAfterStep(Context<C> context) throws ExecutionException {
        invokeCallbacks(afterStepMethods, context);

        if (concurrencyType == Concurrency.Type.LOCK) {
            lock.unlock();
        }
    }

    private void invokeCallbacks(List<Method> methods, Context<C> context) throws ExecutionException {
        try {
            for (Method method : methods) {
                method.setAccessible(true);
                method.invoke(delegate, context);
            }
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new ExecutionException(e);
        }
    }

    @Override
    public StepIdentifier getIdentifier() {
        return identifier;
    }

    @Override
    public List<String> getDependsOn() {
        return dependsOn;
    }
}
