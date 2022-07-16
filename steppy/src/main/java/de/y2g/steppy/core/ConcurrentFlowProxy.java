package de.y2g.steppy.core;

import de.y2g.steppy.api.Context;
import de.y2g.steppy.api.Flow;
import de.y2g.steppy.api.Result;
import de.y2g.steppy.api.exception.ExecutionException;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@SuppressWarnings({"unchecked", "rawtypes"})
public class ConcurrentFlowProxy<C, I, R> extends FlowProxy<C, I, R> implements Flow<C, I, R> {

    private final BiConsumer<Supplier<Result<R>>, CompletableFuture<Result<R>>> taskExecutor;
    private final Executor executor;

    public ConcurrentFlowProxy(Typing<C, I, R> typing, @Nonnull List<StepProxy> steps, Executor executor) {
        super(typing, steps);
        this.taskExecutor = (supplier, future) -> {
            future.completeAsync(supplier, executor);
        };
        this.executor = executor;
    }

    @Override
    public Collection<Result<R>> invoke(C configuration, Collection<I> inputs) throws ExecutionException {
        Logger logger = Logger.getLogger(String.format("flow-%s-%s-%s", getTyping().getConfigType().getSimpleName(), getTyping().getInputType().getSimpleName(), getTyping().getReturnType().getSimpleName()));
        var context = new Context<>(configuration);
        var asynchronousExecutions = new ArrayList<Supplier<Result<R>>>(inputs.size());
        try {
            callBefore(context);

            for (I input : inputs) {
                asynchronousExecutions.add(() -> {
                    try {
                        return invokeSingleItem(context, input);
                    } catch (ExecutionException e) {
                        logger.log(Level.FINE, String.format("Failed for input %s. Error is %s", input, e.getMessage()));
                        return new Result<R>(Result.Type.FAILED, e);
                    } catch (Throwable e) {
                        logger.log(Level.SEVERE, "Error executing in parallel", e);
                        return new Result<R>(Result.Type.FAILED, e);
                    }
                });
            }

            var exec = asynchronousExecutions
                    .stream()
                    .map(supplier -> {
                        var c = new CompletableFuture<Result<R>>();
                        c.exceptionally(throwable -> new Result<R>(Result.Type.FAILED, throwable));
                        taskExecutor.accept(supplier, c);
                        return c;
                    })
                    .collect(Collectors.toList());

            return exec.stream().map(CompletableFuture::join)
                    .collect(Collectors.toList());
        } finally {
            callAfter(context);
        }
    }

    public void invoke(C configuration, Collection<I> inputs, Consumer<Result<R>> consumer) throws ExecutionException {
        Logger logger = Logger.getLogger(String.format("flow-%s-%s-%s", getTyping().getConfigType().getSimpleName(), getTyping().getInputType().getSimpleName(), getTyping().getReturnType().getSimpleName()));
        var context = new Context<>(configuration);
        var asynchronousExecutions = new ArrayList<Supplier<Result<R>>>(inputs.size());
        try {
            callBefore(context);

            for (I input : inputs) {
                asynchronousExecutions.add(() -> {
                    try {
                        return invokeSingleItem(context, input);
                    } catch (ExecutionException e) {
                        logger.log(Level.SEVERE, "Error executing in parallel", e);
                        return new Result<R>(Result.Type.FAILED, e);
                    }
                });
            }

            asynchronousExecutions
                    .parallelStream()
                    .forEach(supplier -> {
                        var c = new CompletableFuture<Result<R>>();
                        c.exceptionally(throwable -> new Result<R>(Result.Type.FAILED, throwable)).thenAccept(consumer);
                        taskExecutor.accept(supplier, c);
                    });
        } finally {
            callAfter(context);
        }
    }
}
