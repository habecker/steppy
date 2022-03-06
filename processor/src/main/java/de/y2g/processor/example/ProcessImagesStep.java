package de.y2g.processor.example;

import de.y2g.steppy.api.Context;
import de.y2g.steppy.api.State;
import de.y2g.steppy.api.Variable;
import de.y2g.steppy.api.Step;
import de.y2g.steppy.api.exception.ExecutionException;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

@Component(ProcessImagesStep.name)
public class ProcessImagesStep implements Step<Void, Integer, Integer> {
    public static final String name = "process-image";
    private static final Logger logger = Logger.getLogger(ProcessImagesStep.class.getName());

    @State
    Variable<AtomicInteger> counter;

    @State(global = true, name = "global-counter")
    Variable<AtomicInteger> globalCounter;

    @Override
    public Integer invoke(Context<Void> context, Integer input) throws ExecutionException {
        if (counter.get(context) == null)
            counter.set(context, new AtomicInteger(0));

        if (globalCounter.get(context) == null)
            globalCounter.set(context, new AtomicInteger(0));

        var result = String.format("process-images-%d Counter: %d-%d", input, counter.get(context).incrementAndGet(), globalCounter.get(context).incrementAndGet());
        logger.info(result);
        //logger.info(String.format("Processed: %d", input));

        /*if (input > 10)
            context.abort();*/

        if (input == 16)
            throw new ExecutionException("blah");
        return input + 10;
    }
}
