package de.y2g.steppy;

import de.y2g.steppy.api.Step;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("rawtypes")
public class SingletonStepRepository extends de.y2g.steppy.api.StepRepository {
    private static SingletonStepRepository repository = null;

    protected static SingletonStepRepository instance() {
        if (repository == null)
            repository = new SingletonStepRepository();
        return repository;
    }

    private static final Map<String, Step> steps = new HashMap<>();

    public static void register(String name, Step step) {
        steps.put(name, step);
    }

    @Override
    protected Step create(String name) {
        if (!this.steps.containsKey(name)) {
            throw new IllegalArgumentException(String.format("Step with name %s was not found in the repository.", name));
        }
        return this.steps.get(name);
    }
}
