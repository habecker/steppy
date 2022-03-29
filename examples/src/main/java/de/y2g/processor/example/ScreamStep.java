package de.y2g.processor.example;

import de.y2g.steppy.api.Context;
import de.y2g.steppy.api.Step;
import de.y2g.steppy.api.exception.ExecutionException;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

@Component(ScreamStep.name)
public class ScreamStep implements Step<Void, Integer, Integer> {
    public static final String name = "scream-image";
    private static final Logger logger = Logger.getLogger(ScreamStep.class.getName());

    @Override
    public Integer invoke(Context<Void> context, Integer input) throws ExecutionException {
        var result = String.format("scream-step-%d", input);
        logger.info(result);
        return input;
    }
}
