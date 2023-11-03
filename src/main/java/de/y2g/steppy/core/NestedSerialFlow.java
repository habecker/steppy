package de.y2g.steppy.core;

import de.y2g.steppy.api.Context;
import de.y2g.steppy.api.Result;
import de.y2g.steppy.api.exception.ExecutionException;
import de.y2g.steppy.api.streaming.Source;
import jakarta.validation.constraints.NotNull;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NestedSerialFlow<C, I, R> extends FlowProxy<C, I, R> implements StepProxy<C, I, R> {
    private final Typing<C, I, R> stepTyping;

    private final UUID uuid = UUID.randomUUID();

    public NestedSerialFlow(Typing<C, I, R> typing, @NotNull List<StepProxy> steps) {
        super(typing, steps);
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
        Logger logger = Logger.getLogger(
            String.format("nested-flow-%s-%s-%s", getTyping().getConfigType().getSimpleName(), getTyping().getInputType().getSimpleName(),
                getTyping().getReturnType().getSimpleName()));

        if (input instanceof Source) {
            var source = (Source<I>)input;
            while (source.isActive()) {
                try {
                    // TODO make this configurable
                    if (!source.next(Duration.ofSeconds(1), in -> {
                        try {
                            invokeSingleItem(context, in);
                        } catch (ExecutionException e) {
                            logger.log(Level.SEVERE, "Error occurred during flow-streaming: " + e.getMessage(), e);
                            source.onFailure(input, e);
                        }
                    })) {
                        logger.log(Level.FINE, "Source became inactive while waiting.");
                        break;
                    }

                } catch (InterruptedException e) {
                    logger.log(Level.SEVERE, "Error occurred during flow-streaming: " + e.getMessage(), e);
                    source.close();
                    break;
                }
            }
        } else {
            Result<R> data;
            try {
                data = invokeSingleItem(context, input);
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
                throw new ExecutionException("Nested flow failed", data.getException());
            }
        }

        return null;
    }

    @Override
    public StepIdentifier getIdentifier() {
        return new StepIdentifier(uuid + "-nested-flow");
    }
}
