package de.y2g.processor.example;

import de.y2g.steppy.api.*;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

@DependsOn(ProcessImagesStep.name)
@Component(UploadImageStep.name)
public class UploadImageStep implements Step<Void, Integer, Integer> {
    public static final String name = "upload-image";
    private static final Logger logger = Logger.getLogger(UploadImageStep.class.getName());

    @State(global = true, name = "global-counter")
    Variable<AtomicInteger> counter;

    @State(global = true, name = "global-counter")
    Variable<String> test;

    @Override
    public Integer invoke(Context<Void> context, Integer input) {
        if (counter.get(context) == null)
            counter.set(context, new AtomicInteger(0));
        var result = String.format("upload-images-%d Counter: %d", input, counter.get(context).incrementAndGet());
        //logger.info(result);
        return input;
    }
}
