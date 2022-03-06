package de.y2g.processor.example;

import de.y2g.processor.IntegerSource;
import de.y2g.steppy.api.Context;
import de.y2g.steppy.api.Step;
import de.y2g.steppy.api.streaming.Source;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

@Component(IntegerSourceStep.name)
public class IntegerSourceStep implements Step<Void, Void, Source<Integer>> {
    public static final String name = "integer-source";
    private static final Logger logger = Logger.getLogger(IntegerSourceStep.class.getName());

    @Override
    public Source<Integer> invoke(Context<Void> context, Void input) {
        return new IntegerSource(10,11,12,13);
    }
}
