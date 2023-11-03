package de.y2g.steppy.api.streaming;

import de.y2g.steppy.api.Context;
import de.y2g.steppy.api.None;
import de.y2g.steppy.api.Step;
import de.y2g.steppy.api.exception.ExecutionException;

public class SimpleStringSourceStep implements Step<None, None, Source<String>> {
    private final Source<String> returnValue;

    public SimpleStringSourceStep(Source<String> returnValue) {
        this.returnValue = returnValue;
    }

    @Override
    public Source<String> invoke(Context<None> context, None input) throws ExecutionException {
        return returnValue;
    }
}
