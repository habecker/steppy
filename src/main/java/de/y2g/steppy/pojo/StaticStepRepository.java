package de.y2g.steppy.pojo;

import de.y2g.steppy.api.Step;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("rawtypes")
public class StaticStepRepository extends de.y2g.steppy.api.StepRepository {
    private static StaticStepRepository repository = null;

    protected static StaticStepRepository instance() {
        if (repository == null)
            repository = new StaticStepRepository();
        return repository;
    }

    private StaticStepRepository() {
        super();
    }

    private static final Map<String, Step> steps = new HashMap<>();

    public static void register(String name, Step step) {
        steps.put(name, step);
        steps.put(step.getClass().getCanonicalName(), step);
    }

    public static void register(Class<? extends Step>... stepType) {
        for (Class<? extends Step> type : stepType) {
            register(type);
        }
    }

    public static void register(Class<? extends Step> stepType) {
        try {
            var constructor = stepType.getDeclaredConstructor();
            constructor.setAccessible(true);
            steps.put(stepType.getCanonicalName(), constructor.newInstance());
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new IllegalArgumentException(String.format("Step %s could not be instantiated.", stepType.getCanonicalName()), e);
        }
    }

    @Override
    protected Step create(String name) {
        if (!steps.containsKey(name)) {
            throw new IllegalArgumentException(String.format("Step with name %s was not found in the repository.", name));
        }
        return steps.get(name);
    }

    @Override
    protected Step create(Class<? extends Step> stepType) {
        return create(stepType.getCanonicalName());
    }
}
