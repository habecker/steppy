package de.y2g.steppy.core;

import de.y2g.steppy.api.Context;
import de.y2g.steppy.api.Result;
import de.y2g.steppy.api.exception.ExecutionException;
import de.y2g.steppy.api.streaming.Source;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import static de.y2g.steppy.api.Result.Type.SUCCEEDED;

public class NestedSerialFlow<C, I, R> extends FlowProxy<C, I, R> implements StepProxy<C, Object, Void> {
    private final Typing<C, I, Void> stepTyping;

    public NestedSerialFlow(Typing<C, I, R> typing, @Nonnull List<StepProxy> steps) {
        super(typing, steps);
        stepTyping = new Typing<>(typing.getConfigType(), typing.getInputType(), Void.class);
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
    public Void execute(Context<C> context, Object input) throws ExecutionException {
        Logger logger = Logger.getLogger(String.format("nested-flow-%s-%s-%s", getTyping().getConfigType().getSimpleName(), getTyping().getInputType().getSimpleName(), getTyping().getReturnType().getSimpleName()));

        if (input instanceof Source)
        {
            var source = (Source<I>) input;
            while (source.isActive()) {
                try {
                    // TODO make this configurable
                    if(!source.next(Duration.ofSeconds(1), in -> {
                        try
                        {
                            invokeSingleItem(context, in);
                        }
                        catch( ExecutionException e )
                        {
                            logger.log(Level.SEVERE, "Error occured during flow-streaming: " + e.getMessage(), e);
                            source.close();
                        }
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
        } else {
            Result<R> data = invokeSingleItem(context, (I)input);

            switch (data.getType())
            {
                case SUCCEEDED:
                    return null;
                case ABORTED:
                    context.abort();
                case FAILED:
                    var throwable = data.getException();
                    if (throwable instanceof RuntimeException)
                        throw (RuntimeException)throwable;
                    else if (throwable instanceof ExecutionException)
                        throw (ExecutionException) throwable;
                    else
                        throw new ExecutionException("Nested flow failed", throwable);
            }
        }

        return null;
    }

    @Override
    public StepIdentifier getIdentifier() {
        return new StepIdentifier(UUID.randomUUID().toString() + "-nested-flow");
    }

    @Override
    public List<String> getDependsOn() {
        return Collections.emptyList();
    }
}
