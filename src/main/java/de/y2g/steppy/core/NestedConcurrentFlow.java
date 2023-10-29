package de.y2g.steppy.core;

import de.y2g.steppy.api.Context;
import de.y2g.steppy.api.Result;
import de.y2g.steppy.api.exception.ExecutionException;
import de.y2g.steppy.api.streaming.Source;
import jakarta.validation.constraints.NotNull;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Phaser;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NestedConcurrentFlow<C, I, R> extends FlowProxy<C, I, R> implements StepProxy<C, I, R> {

    private final BiConsumer<Supplier<Result<R>>, CompletableFuture<Result<R>>> taskExecutor;
    private final Typing<C, I, R> stepTyping;
    private final Executor executor;

    public NestedConcurrentFlow(Typing<C, I, R> typing, @NotNull List<StepProxy> steps, Executor executor) {
        super(typing, steps);
        this.executor = executor;
        this.taskExecutor = (supplier, future) -> {
            future.completeAsync(supplier, executor);
        };
        stepTyping = typing;
    }

    @Override
    public void onBeforeFlow(Context<C> context) throws ExecutionException {
        callBefore(context);
    }

    @Override
    public void onAfterFlow(Context<C> context) throws ExecutionException {
        callAfter(context);
    }

    @Override
    public void onBeforeStep(Context<C> context) throws ExecutionException {
        // do nothing
    }

    @Override
    public void onAfterStep(Context<C> context) throws ExecutionException {
        // do nothing
    }


    @Override
    public Typing getTyping() {
        return stepTyping;
    }

    @Override
    public R execute(Context<C> context, I input) throws ExecutionException {
        Logger logger = Logger.getLogger(String.format("nested-flow-%s-%s-%s", getTyping().getConfigType().getSimpleName(), getTyping().getInputType().getSimpleName(), getTyping().getReturnType().getSimpleName()));

        if (input instanceof Source) {
            Phaser executions = new Phaser(0);
            var source = (Source) input;

            while (source.isActive()) {
                try {
                    // TODO make this configurable
                    if (!source.next(Duration.ofSeconds(1), in -> {
                        var c = new CompletableFuture<Result<R>>();
                        c.exceptionally(throwable -> {
                            logger.log(Level.SEVERE, "Error occured during flow-streaming: " + throwable.getMessage(), throwable);
                            source.onFailure(input, throwable);
                            return null;
                        }).thenAccept((result) -> {
                            if (result.getType() == Result.Type.FAILED) {
                                source.onFailure(input, result.getException());
                            }
                            executions.arriveAndDeregister();
                        });

                        executions.register();
                        // TODO add timeout for max execution time per item
                        taskExecutor.accept(() -> {
                            try {
                                return invokeSingleItem(context, (I) in);
                            } catch (ExecutionException e) {
                                logger.log(Level.SEVERE, "Error executing in parallel", e);
                                return new Result<R>(Result.Type.FAILED, e);
                            }
                        }, c);
                    })) {
                        logger.log(Level.FINE, "Source became inactive while waiting.");
                        break;
                    }

                } catch (InterruptedException e) {
                    logger.log(Level.SEVERE, "Error occured during flow-streaming: " + e.getMessage(), e);
                    source.close();
                    break;
                }
            }
            executions.arriveAndAwaitAdvance();
        } else {
            Result<R> data;
            try {
                data = invokeSingleItem(context, (I) input);
            } catch (ExecutionException e) {
                data = new Result<>(Result.Type.FAILED, e);
            }

            switch (data.getType()) {
                case SUCCEEDED:
                    return data.getResult();
                case ABORTED:
                    context.abort();
                    break;
                case FAILED:
                    var throwable = data.getException();
                    if (throwable instanceof ExecutionException)
                        throw (ExecutionException) throwable;
                    throw new ExecutionException("Nested flow failed", throwable);
            }
        }

        return null;
    }

    @Override
    public StepIdentifier getIdentifier() {
        return new StepIdentifier(UUID.randomUUID() + "-nested-flow");
    }
}
