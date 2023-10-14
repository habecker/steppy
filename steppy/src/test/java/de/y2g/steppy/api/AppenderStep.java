package de.y2g.steppy.api;

import de.y2g.steppy.api.exception.ExecutionException;

public class AppenderStep implements Step<None, String, String> {
    private final String append;

    public AppenderStep(String append) {
        this.append = append;
    }

    @Override
    public String invoke(Context<None> context, String input) throws ExecutionException {
        return input + append;
    }
}
