package de.y2g.steppy.api.nesting;

import java.time.Duration;
import java.util.function.Consumer;

//FIXME This interface needs to be renamed. It should only be used for data producing steps
public interface Source<I> extends AutoCloseable {

    boolean next(Duration timeout, Consumer<I> consumer) throws InterruptedException;

    void close();
}
