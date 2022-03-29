package de.y2g.processor.example;

import de.y2g.steppy.api.Context;
import de.y2g.steppy.api.Step;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

@Component(IntegerProducerStep.name)
public class IntegerProducerStep implements Step<Void, Void, Integer> {
    public static final String name = "integer-producer";
    private static final Logger logger = Logger.getLogger(IntegerProducerStep.class.getName());

    @Override
    public Integer invoke(Context<Void> context, Void input) {
        return 100;
    }
}
