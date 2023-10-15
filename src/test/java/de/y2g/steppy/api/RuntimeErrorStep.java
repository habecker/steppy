package de.y2g.steppy.api;

import de.y2g.steppy.api.exception.ExecutionException;

public class RuntimeErrorStep implements Step<None, String, String> {
    @Override
    public String invoke(Context<None> context, String input) throws ExecutionException {
        throw new RuntimeException("Oh no");
    }
}
