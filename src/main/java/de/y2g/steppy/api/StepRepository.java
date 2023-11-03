package de.y2g.steppy.api;

@SuppressWarnings("rawtypes")
public abstract class StepRepository {
    protected abstract Step create(String name);

    protected abstract Step create(Class<? extends Step> stepType);
}
