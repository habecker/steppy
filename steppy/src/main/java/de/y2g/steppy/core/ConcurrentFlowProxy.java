package de.y2g.steppy.core;

import de.y2g.steppy.api.Context;
import de.y2g.steppy.api.Flow;
import de.y2g.steppy.api.Result;
import de.y2g.steppy.api.exception.ExecutionException;
import de.y2g.steppy.api.streaming.Sink;
import de.y2g.steppy.api.streaming.Source;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Phaser;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@SuppressWarnings({"unchecked", "rawtypes"})
public class ConcurrentFlowProxy<C,I,R> extends FlowProxy<C,I,R> implements Flow<C,I,R> {

    private final BiConsumer<Supplier<Result<R>>, CompletableFuture<Result<R>>> taskExecutor;
    private Executor executor;

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
        var asynchronousExecutions = new ArrayList<Supplier< Result<R> >>( inputs.size() );
        try {
            callBefore(context);

            for (I input : inputs)
            {
                asynchronousExecutions.add( () -> {
                    try
                    {
                        return invokeSingleItem(context, input);
                    }
                    catch( ExecutionException e )
                    {
                        logger.log(Level.FINE, String.format("Failed for input %s. Error is %s", input, e.getMessage()) );
                        return new Result<R>(Result.Type.FAILED, e);
                    }
                    catch( Throwable e )
                    {
                        logger.log(Level.SEVERE, "Error executing in parallel", e );
                        return new Result<R>(Result.Type.FAILED, e);
                    }
                } );
            }

            var exec = asynchronousExecutions
                    .stream()
                    .map(supplier -> {
                        var c = new CompletableFuture<Result<R>>();
                        c.exceptionally(throwable -> new Result<R>(Result.Type.FAILED, throwable));
                        taskExecutor.accept(supplier, c);
                        return c;
                    })
                    .collect( Collectors.toList() );

            return exec.stream().map(CompletableFuture::join)
                    .collect( Collectors.toList() );
        } finally {
            callAfter(context);
        }
    }

    @Override
    public void stream(C configuration, Source<I> source, Sink<Result<R>> sink) {
        executor.execute(() -> {
        Logger logger = Logger.getLogger(String.format("flow-%s-%s-%s", getTyping().getConfigType().getSimpleName(), getTyping().getInputType().getSimpleName(), getTyping().getReturnType().getSimpleName()));
        // replace Phase to maintain lang level 11
        var context = new Context<>(configuration);

        Phaser executions = new Phaser(0);
            try {
                try {
                    callBefore(context);
                } catch (ExecutionException e) {
                    logger.log(Level.SEVERE, "Error occured during flow-streaming: " + e.getMessage(), e);
                }

                while (source.isActive()) {
                    try {
                        // TODO make this configurable
                        if(!source.next(Duration.ofSeconds(1), input -> {
                            var c = new CompletableFuture<Result<R>>();
                            c.exceptionally(throwable -> new Result<R>(Result.Type.FAILED, throwable)).thenAccept((result) -> {
                                sink.accept(result);
                                executions.arriveAndDeregister();
                            });


                            executions.register();
                            // TODO add timeout for max execution time per item
                            taskExecutor.accept(() -> {
                                try
                                {
                                    return invokeSingleItem(context, input);
                                }
                                catch( ExecutionException e )
                                {
                                    logger.log(Level.SEVERE, "Error executing in parallel", e );
                                    return new Result<R>(Result.Type.FAILED, e);
                                }
                            }, c);
                        })) {
                            logger.log(Level.FINE, "Source became inactive while waiting.");
                            break;
                        }

                    } catch (InterruptedException e) {
                        logger.log(Level.SEVERE, "Error occured during flow-streaming: " + e.getMessage(), e);
                        break;
                    }
                }
            } finally {
                try {
                    callAfter(context);
                } catch (ExecutionException e) {
                    logger.log(Level.SEVERE, "Error occured during flow-streaming: " + e.getMessage(), e);
                }
            }

            if (!source.isActive()) {
                logger.log(Level.INFO, "Source became inactive, flow is terminated.");
            } else {
                logger.log(Level.INFO, "Flow is terminated.");
            }

            if (executions.isTerminated()){
                sink.close();
            } else {
                executor.execute(() -> {
                    executions.arriveAndAwaitAdvance();
                    sink.close();
                });
            }
        });
    }

    public void invoke(C configuration, Collection<I> inputs, Consumer<Result<R>> consumer) throws ExecutionException {
        Logger logger = Logger.getLogger(String.format("flow-%s-%s-%s", getTyping().getConfigType().getSimpleName(), getTyping().getInputType().getSimpleName(), getTyping().getReturnType().getSimpleName()));
        var context = new Context<>(configuration);
        var asynchronousExecutions = new ArrayList<Supplier< Result<R> >>( inputs.size() );
        try {
            callBefore(context);

            for (I input : inputs)
            {
                asynchronousExecutions.add( () -> {
                    try
                    {
                        return invokeSingleItem(context, input);
                    }
                    catch( ExecutionException e )
                    {
                        logger.log(Level.SEVERE, "Error executing in parallel", e );
                        return new Result<R>(Result.Type.FAILED, e);
                    }
                } );
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
