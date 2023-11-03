package de.y2g.steppy.api.validation;

import de.y2g.steppy.api.Context;
import de.y2g.steppy.api.Step;
import de.y2g.steppy.api.exception.ExecutionException;

public class StepWithNoTypes implements Step {
    @Override
    public Object invoke(Context context, Object input) throws ExecutionException {
        context.abort();
        return null;
    }
}
