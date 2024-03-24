package de.y2g.steppy.api.streaming;

import jakarta.validation.constraints.NotNull;

import java.time.Duration;
import java.util.function.Consumer;

public interface Source<I> {
    boolean isActive();

    boolean next(@org.jetbrains.annotations.NotNull @NotNull Duration timeout,
        @org.jetbrains.annotations.NotNull @NotNull Consumer<I> consumer) throws InterruptedException;

    void onFailure(@org.jetbrains.annotations.NotNull @NotNull I input, Throwable throwable);

    void close();
}
