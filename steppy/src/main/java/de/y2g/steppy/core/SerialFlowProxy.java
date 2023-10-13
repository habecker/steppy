package de.y2g.steppy.core;

import de.y2g.steppy.api.Context;
import de.y2g.steppy.api.Flow;
import de.y2g.steppy.api.Result;
import de.y2g.steppy.api.exception.ExecutionException;
import de.y2g.steppy.api.streaming.Sink;
import de.y2g.steppy.api.streaming.Source;
import jakarta.validation.constraints.NotNull;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings({"unchecked", "rawtypes"})
public class SerialFlowProxy<C, I, R> extends FlowProxy<C, I, R> implements Flow<C, I, R> {

    public SerialFlowProxy(Typing<C, I, R> typing, @NotNull List<StepProxy> steps) {
        super(typing, steps);
    }

    @Override
    public Collection<Result<R>> invoke(C configuration, Collection<I> inputs) throws ExecutionException {
        var context = new Context<>(configuration);
        var result = new ArrayList<Result<R>>(inputs.size());
        try {
            callBefore(context);


            for (I input : inputs) {
                try {
                    Result<R> data = invokeSingleItem(context, input);
                    result.add(data);
                } catch (ExecutionException e) {
                    result.add(new Result<R>(Result.Type.FAILED, e));
                }
            }
        } finally {
            callAfter(context);
        }

        return result;
    }

    @Override
    public void stream(C configuration, Source<I> source, Sink<Result<R>> sink) throws ExecutionException {
        Logger logger = Logger.getLogger(String.format("flow-%s-%s-%s", getTyping().getConfigType().getSimpleName(), getTyping().getInputType().getSimpleName(), getTyping().getReturnType().getSimpleName()));
        var context = new Context<>(configuration);
        try {
            callBefore(context);

            while (source.isActive()) {
                try {
                    // TODO make this configurable
                    if (!source.next(Duration.ofSeconds(1), input -> {
                        try {
                            sink.accept(invokeSingleItem(context, input));
                        } catch (ExecutionException e) {
                            sink.accept(new Result<R>(Result.Type.FAILED, e));
                        }
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
            callAfter(context);
        }

        if (!source.isActive()) {
            logger.log(Level.INFO, "Source became inactive, flow is terminated.");
        } else {
            logger.log(Level.INFO, "Flow is terminated.");
        }

        sink.close();
    }

    public void invoke(C configuration, Collection<I> inputs, Consumer<Result<R>> consumer) throws ExecutionException {
        var context = new Context<>(configuration);
        try {
            callBefore(context);
            for (I input : inputs) {
                Result<R> result;
                try {
                    result = invokeSingleItem(context, input);
                } catch (ExecutionException e) {
                    result = new Result<R>(Result.Type.FAILED, e);
                }
                consumer.accept(result);
            }
        } finally {
            callAfter(context);
        }
    }


}
