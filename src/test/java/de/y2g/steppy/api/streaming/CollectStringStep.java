package de.y2g.steppy.api.streaming;

import de.y2g.steppy.api.Context;
import de.y2g.steppy.api.None;
import de.y2g.steppy.api.Step;
import de.y2g.steppy.api.exception.ExecutionException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

class CollectStringStep implements Step<None, String, None> {
    final List<String> result = new ArrayList<>();

    final Semaphore semaphore = new Semaphore(0);

    @Override
    public None invoke(Context<None> context, String input) throws ExecutionException {
        result.add(input);
        semaphore.release();
        return None.value();
    }
}
