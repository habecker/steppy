package de.y2g.processor;

import de.y2g.steppy.api.streaming.Source;

import java.time.Duration;
import java.util.Arrays;
import java.util.Deque;
import java.util.NoSuchElementException;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.function.Consumer;

public class IntegerSource implements Source<Integer> {
    Deque<Integer> data;

    public IntegerSource(Integer... numbers) {
        data = new LinkedBlockingDeque<Integer>(Arrays.asList(numbers));
    }

    @Override
    public boolean isActive() {
        return !data.isEmpty();
    }

    @Override
    public boolean next(Duration timeout, Consumer<Integer> consumer) throws InterruptedException {
        try {
            var input = data.pop();
            consumer.accept(input);
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    @Override
    public void close() {
        data.clear();
    }
}
