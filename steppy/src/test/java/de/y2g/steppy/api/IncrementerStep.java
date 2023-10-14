package de.y2g.steppy.api;

import de.y2g.steppy.api.exception.ExecutionException;

public class IncrementerStep implements Step<None, Integer, Integer> {
    @Override
    public Integer invoke(Context<None> context, Integer input) throws ExecutionException {
        return input + 1;
    }
}
