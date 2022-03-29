package de.y2g.processor.cdi;

import de.y2g.steppy.api.Context;
import de.y2g.steppy.api.State;
import de.y2g.steppy.api.Step;
import de.y2g.steppy.api.Variable;

import javax.enterprise.context.Dependent;
import javax.inject.Named;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

@Named(ProcessImagesStep.name)
@Dependent
public class ProcessImagesStep implements Step<Void, Integer, Integer> {
    public static final String name = "process-cdi-images";
    private static final Logger logger = Logger.getLogger(ProcessImagesStep.class.getName());

    @State
    Variable<AtomicInteger> counter;

    @Override
    public Integer invoke(Context<Void> context, Integer input) {
        if (counter.get(context) == null)
            counter.set(context, new AtomicInteger(0));
        var result = String.format("process-images-%d", counter.get(context).incrementAndGet());
        try {
            Thread.sleep((16L - input.longValue()) * 100L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //logger.info(result);
        logger.info(String.format("Processed: %d", input));
        return input;
    }
}
