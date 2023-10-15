package de.y2g.steppy.api.streaming;

import de.y2g.steppy.api.Context;
import de.y2g.steppy.api.None;
import de.y2g.steppy.api.AppendAStep;
import de.y2g.steppy.api.AppendBStep;
import de.y2g.steppy.api.NoopStep;
import de.y2g.steppy.api.Result;
import de.y2g.steppy.api.Step;
import de.y2g.steppy.api.exception.ExecutionException;
import de.y2g.steppy.api.validation.ValidationException;
import de.y2g.steppy.pojo.StaticFlowBuilderFactory;
import de.y2g.steppy.pojo.StaticStepRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static java.util.Collections.emptyIterator;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StreamingFlowTest {

    static class SimpleSource<T> implements Source<T> {
        private Iterator<T> source;

        public SimpleSource(Stream<T> source) {
            this.source = source.iterator();
        }

        @Override
        public boolean isActive() {
            return source.hasNext();
        }

        @Override
        public boolean next(Duration timeout, Consumer<T> consumer) throws InterruptedException {
            if (source.hasNext()) {
                consumer.accept(source.next());
                return source.hasNext();
            }
            return false;
        }

        @Override
        public void close() {
            source = emptyIterator();
        }
    }

    static class SimpleSink<T> implements Sink<T> {
        private final List<T> result = new ArrayList<>();
        private final Semaphore semaphore = new Semaphore(0);


        @Override
        public void close() {
            semaphore.release();
        }

        @Override
        public void accept(Result<T> t) {
            assert t.getType() == Result.Type.SUCCEEDED;

            result.add(t.getResult());
        }

        public List<T> getResult() {
            return result;
        }
    }

    @BeforeAll
    static void setup() {
        StaticStepRepository.register(AppendAStep.class);
        StaticStepRepository.register(AppendBStep.class);
        StaticStepRepository.register(NoopStep.class);
        StaticFlowBuilderFactory.initialize(Executors.newSingleThreadExecutor());
    }

    @Test
    void testSequential() throws ExecutionException, ValidationException, InterruptedException {
        var flow = StaticFlowBuilderFactory.builder(None.class, String.class, String.class).append(AppendAStep.class)
            .append(AppendBStep.class).append(AppendAStep.class).append(AppendBStep.class).build();

        var source = new SimpleSource<>(Stream.of("", "C"));
        var sink = new SimpleSink<String>();

        flow.stream(None.value(), source, sink);

        assertThat(sink.getResult()).isEqualTo(List.of("ABAB", "CABAB"));
    }

    @Test
    void testConcurrent() throws ExecutionException, ValidationException, InterruptedException {
        var flow = StaticFlowBuilderFactory.builder(None.class, String.class, String.class).append(AppendAStep.class)
            .append(AppendBStep.class).append(AppendAStep.class).append(AppendBStep.class).concurrent().build();

        var source = new SimpleSource<>(Stream.of("", "C"));
        var sink = new SimpleSink<String>();

        flow.stream(None.value(), source, sink);
        assertTrue(sink.semaphore.tryAcquire(1, TimeUnit.SECONDS));

        assertThat(sink.getResult()).isEqualTo(List.of("ABAB", "CABAB"));
    }

    @Test
    void testInnerStreaming() throws ExecutionException, ValidationException, InterruptedException {
        var source = new SimpleSource<>(Stream.of("", "C"));
        var sourceStep = new SimpleStringSourceStep(source);
        var collectStep = new CollectStringStep();

        StaticStepRepository.register("source", sourceStep);
        StaticStepRepository.register("collect", collectStep);

        var flow = StaticFlowBuilderFactory.builder(None.class, None.class, None.class)
            .append("source")
            .nest(None.class, b -> b
                .append(AppendAStep.class)
                .append(AppendBStep.class)
                .append(AppendAStep.class)
                .append(AppendBStep.class)
                .append("collect")
            )
            .build();

        flow.invoke(None.value());

        assertThat(collectStep.result).isEqualTo(List.of("ABAB", "CABAB"));
    }

    @Test
    void testInnerStreamingConcurrent() throws ExecutionException, ValidationException, InterruptedException {
        var source = new SimpleSource<>(Stream.of("", "C"));
        var sourceStep = new SimpleStringSourceStep(source);
        var collectStep = new CollectStringStep();

        StaticStepRepository.register("source", sourceStep);
        StaticStepRepository.register("collect", collectStep);

        var flow = StaticFlowBuilderFactory.builder(None.class, None.class, None.class)
            .append("source")
            .nest(None.class, b -> b
                .append(AppendAStep.class)
                .append(AppendBStep.class)
                .append(AppendAStep.class)
                .append(AppendBStep.class)
                .append("collect")
                .concurrent()
            )
            .build();

        flow.invoke(None.value());

        assertTrue(collectStep.semaphore.tryAcquire(2, 1, TimeUnit.SECONDS));
        assertThat(new HashSet<>(collectStep.result)).isEqualTo(Set.of("ABAB", "CABAB"));
    }

    private class CollectStringStep implements Step<None, String, None> {
        private final List<String> result = new ArrayList<>();
        private final Semaphore semaphore = new Semaphore(0);

        @Override
        public None invoke(Context<None> context, String input) throws ExecutionException {
            result.add(input);
            semaphore.release();
            return None.value();
        }
    }
}