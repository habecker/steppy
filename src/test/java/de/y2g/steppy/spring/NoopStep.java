package de.y2g.steppy.spring;

import de.y2g.steppy.api.Context;
import de.y2g.steppy.api.None;
import de.y2g.steppy.api.Step;
import de.y2g.steppy.api.exception.ExecutionException;
import org.springframework.stereotype.Component;

@Component("noop")
public class NoopStep implements Step<Object, None, None> {
    @Override
    public None invoke(Context<Object> context, None input) throws ExecutionException {
        return input;
    }
}