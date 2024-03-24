package de.y2g.steppy.cdi;

import de.y2g.steppy.api.Context;
import de.y2g.steppy.api.None;
import de.y2g.steppy.api.Step;
import de.y2g.steppy.api.exception.ExecutionException;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Named;

@Named("noop")
@Dependent
public class NoopStep implements Step<Object, None, None> {
    @Override
    public None invoke(Context<Object> context, None input) throws ExecutionException {
        return input;
    }
}