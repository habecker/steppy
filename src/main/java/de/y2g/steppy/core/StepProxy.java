package de.y2g.steppy.core;

import de.y2g.steppy.api.Context;
import de.y2g.steppy.api.exception.ExecutionException;

import java.util.Set;

public interface StepProxy<C, I, R> {
    Typing<C, I, R> getTyping();

    R execute(Context<C> context, I input) throws ExecutionException;

    void onBeforeFlow(Context<C> context) throws ExecutionException;

    void onAfterFlow(Context<C> context) throws ExecutionException;

    void onBeforeStep(Context<C> context) throws ExecutionException;

    void onAfterStep(Context<C> context) throws ExecutionException;

    StepIdentifier getIdentifier();

    Set<Dependency> getDependencies();

    Set<Dependency> getFulfilledDependencies();
}
