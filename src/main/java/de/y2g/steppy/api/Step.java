package de.y2g.steppy.api;

import de.y2g.steppy.api.exception.ExecutionException;
import jakarta.validation.constraints.NotNull;

@FunctionalInterface
public interface Step<C, I, R> {
    @org.jetbrains.annotations.NotNull @NotNull R invoke(@org.jetbrains.annotations.NotNull @NotNull Context<C> context,
        @org.jetbrains.annotations.NotNull @NotNull I input) throws ExecutionException;
}
