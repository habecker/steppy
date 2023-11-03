package de.y2g.steppy.api.streaming;

import de.y2g.steppy.api.Result;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

public class SimpleSink<T> implements Sink<T> {
    final Semaphore semaphore = new Semaphore(0);

    private final List<T> result = new ArrayList<>();

    private boolean closed;

    @Override
    public void close() {
        semaphore.release();
        closed = true;
    }

    @Override
    public void accept(Result<T> t) {
        assert t.getType() == Result.Type.SUCCEEDED;

        result.add(t.getResult());
    }

    public List<T> getResult() {
        return result;
    }

    public boolean isClosed() {
        return closed;
    }
}
