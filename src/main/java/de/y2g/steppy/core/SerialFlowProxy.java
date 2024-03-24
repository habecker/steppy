package de.y2g.steppy.core;

import de.y2g.steppy.api.Configurations;
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
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class SerialFlowProxy<I, R> extends FlowProxy<I, R> implements Flow<I, R> {

    public SerialFlowProxy(Typing<Configurations, I, R> typing, @NotNull List<StepProxy> steps) {
        super(typing, steps);
    }

    @Override
    public Collection<Result<R>> invoke(Configurations configurations, Collection<I> inputs) throws ExecutionException {
        preprocessConfiguration(configurations);

        var context = new Context<>(configurations, Configurations.class);
        var result = new ArrayList<Result<R>>(inputs.size());
        try {
            callBefore(context);

            for (I input: inputs) {
                try {
                    Result<R> data = invokeSingleItem(context, input);
                    result.add(data);
                } catch (ExecutionException e) {
                    result.add(new Result<>(Result.Type.FAILED, e));
                }
            }
        } finally {
            callAfter(context);
        }

        return result;
    }

    @Override
    public void stream(Configurations configurations, Source<I> source, Sink<R> sink) throws ExecutionException {
        preprocessConfiguration(configurations);

        Logger logger = Logger.getLogger(
            String.format("flow-%s-%s-%s", getTyping().getConfigType().getSimpleName(), getTyping().getInputType().getSimpleName(),
                getTyping().getReturnType().getSimpleName()));
        var context = new Context<>(configurations, Configurations.class);
        try {
            callBefore(context);

            while (source.isActive()) {
                try {
                    // TODO make this configurable
                    if (!source.next(Duration.ofSeconds(1), input -> {
                        try {
                            sink.accept(invokeSingleItem(context, input));
                        } catch (ExecutionException e) {
                            source.onFailure(input, e);
                        }
                    })) {
                        logger.log(Level.FINE, "Source became inactive while waiting.");
                        break;
                    }

                } catch (InterruptedException e) {
                    logger.log(Level.SEVERE, "Error occurred during flow-streaming: " + e.getMessage(), e);
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
}
