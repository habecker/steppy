package de.y2g.steppy.api.streaming;

import de.y2g.steppy.api.None;
import de.y2g.steppy.api.Context;
import de.y2g.steppy.api.Step;
import de.y2g.steppy.api.exception.ExecutionException;

import java.sql.Array;
import java.util.ArrayList;
import java.util.List;

public class SimpleStringSinkStep implements Step<None, Sink<String>, None> {
    private final List<String> result = new ArrayList<>();


    @Override
    public None invoke(Context<None> context, Sink<String> input) throws ExecutionException {

        return None.value();
    }
}
