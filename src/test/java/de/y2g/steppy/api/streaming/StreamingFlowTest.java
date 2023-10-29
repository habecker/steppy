package de.y2g.steppy.api.streaming;

import de.y2g.steppy.api.FailStep;
import de.y2g.steppy.api.None;
import de.y2g.steppy.api.AppendAStep;
import de.y2g.steppy.api.AppendBStep;
import de.y2g.steppy.api.NoopStep;
import de.y2g.steppy.api.exception.ExecutionException;
import de.y2g.steppy.api.validation.ValidationException;
import de.y2g.steppy.pojo.StaticFlowBuilderFactory;
import de.y2g.steppy.pojo.StaticStepRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StreamingFlowTest {

    @BeforeAll
    static void setup() {
        StaticStepRepository.register(AppendAStep.class);
        StaticStepRepository.register(AppendBStep.class);
        StaticStepRepository.register(NoopStep.class);
        StaticStepRepository.register(FailStep.class);
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
    void testSequentialError() throws ExecutionException, ValidationException, InterruptedException {
        var flow = StaticFlowBuilderFactory.builder(None.class, String.class, String.class).append(AppendAStep.class)
            .append(AppendBStep.class).append(FailStep.class).append(AppendAStep.class).append(AppendBStep.class).build();

        var source = new SimpleSource<>(Stream.of(""));
        var sink = new SimpleSink<String>();

        flow.stream(None.value(), source, sink);

        var failures = source.getFailures();
        assertThat(failures).hasSize(1);
        assertThat(failures.get(0)).isInstanceOf(ExecutionException.class);
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
    void testConcurrentError() throws ExecutionException, ValidationException, InterruptedException {
        var flow = StaticFlowBuilderFactory.builder(None.class, String.class, String.class).append(AppendAStep.class)
            .append(AppendBStep.class).append(AppendAStep.class).append(FailStep.class).append(AppendBStep.class).concurrent().build();

        var source = new SimpleSource<>(Stream.of(""));
        var sink = new SimpleSink<String>();

        flow.stream(None.value(), source, sink);
        assertTrue(sink.semaphore.tryAcquire(1, TimeUnit.SECONDS));

        var failures = source.getFailures();
        assertThat(failures).hasSize(1);
        assertThat(failures.get(0)).isInstanceOf(ExecutionException.class);
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
    void testInnerStreamingError() throws ExecutionException, ValidationException, InterruptedException {
        var source = new SimpleSource<>(Stream.of(""));
        var sourceStep = new SimpleStringSourceStep(source);
        var collectStep = new CollectStringStep();

        StaticStepRepository.register("source", sourceStep);
        StaticStepRepository.register("collect", collectStep);

        var flow = StaticFlowBuilderFactory.builder(None.class, None.class, None.class)
            .append("source")
            .nest(None.class, b -> b
                .append(AppendAStep.class)
                .append(AppendBStep.class)
                .append(FailStep.class)
            )
            .build();

        flow.invoke(None.value());

        var failures = source.getFailures();
        assertThat(failures).hasSize(1);
        assertThat(failures.get(0)).isInstanceOf(ExecutionException.class);
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

    @Test
    void testInnerStreamingConcurrentError() throws ExecutionException, ValidationException, InterruptedException {
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
                .append(FailStep.class)
                .concurrent()
            )
            .build();

        flow.invoke(None.value());

        var failures = source.getFailures();
        assertThat(failures).hasSize(1);
        assertThat(failures.get(0)).isInstanceOf(ExecutionException.class);
    }
}