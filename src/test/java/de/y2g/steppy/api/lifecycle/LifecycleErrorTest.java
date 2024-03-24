package de.y2g.steppy.api.lifecycle;

import de.y2g.steppy.api.After;
import de.y2g.steppy.api.Before;
import de.y2g.steppy.api.Context;
import de.y2g.steppy.api.None;
import de.y2g.steppy.api.NoopStep;
import de.y2g.steppy.api.Scope;
import de.y2g.steppy.api.Step;
import de.y2g.steppy.api.exception.ExecutionException;
import de.y2g.steppy.api.streaming.SimpleSink;
import de.y2g.steppy.api.streaming.SimpleSource;
import de.y2g.steppy.api.validation.ValidationException;
import de.y2g.steppy.pojo.StaticFlowBuilderFactory;
import de.y2g.steppy.pojo.StaticStepRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.stream.Stream;

import static de.y2g.steppy.api.Result.Type.FAILED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class LifecycleErrorTest {

    private final Executor executor = Runnable::run;

    private static Stream<Arguments> testParamsFlowScoped() {
        return Stream.of(Arguments.of(true, LifecycleBeforeFlowErrorStep.class), Arguments.of(true, LifecycleAfterFlowErrorStep.class),
            Arguments.of(false, LifecycleBeforeFlowErrorStep.class), Arguments.of(false, LifecycleAfterFlowErrorStep.class));
    }

    private static Stream<Arguments> testParamsStepScoped() {
        return Stream.of(Arguments.of(true, LifecycleBeforeStepErrorStep.class), Arguments.of(true, LifecycleAfterStepErrorStep.class),
            Arguments.of(false, LifecycleBeforeStepErrorStep.class), Arguments.of(false, LifecycleAfterStepErrorStep.class));
    }

    @BeforeEach
    void setup() {
        StaticFlowBuilderFactory.initialize(executor);
    }

    @ParameterizedTest
    @MethodSource("testParamsStepScoped")
    void testExceptionDuringLifecycleStepStreamed(boolean concurrent, Class<? extends Step> stepType)
        throws ExecutionException, ValidationException, InterruptedException {
        StaticStepRepository.register(stepType);

        final var builder = StaticFlowBuilderFactory.builder(None.class, None.class);
        if (concurrent) {
            builder.concurrent();
        }

        var flow = builder.append(stepType).build();
        var source = new SimpleSource<>(Stream.of(None.value()));

        flow.stream(source, new SimpleSink<>());

        assertThat(source.getFailures()).hasSize(1);
        var failures = source.getFailures();
        assertThat(failures).hasSize(1);
        var throwable = failures.get(0);
        assertThat(throwable).hasMessageContaining("An unknown error").hasCauseInstanceOf(IOException.class);
    }

    @ParameterizedTest
    @MethodSource("testParamsStepScoped")
    void testExceptionDuringLifecycleStep(boolean concurrent, Class<? extends Step> stepType)
        throws ExecutionException, ValidationException {
        StaticStepRepository.register(stepType);

        final var builder = StaticFlowBuilderFactory.builder(None.class, None.class);
        if (concurrent) {
            builder.concurrent();
        }

        var flow = builder.append(stepType).build();
        var result = flow.invoke(None.value());
        assertThat(result.getType()).isEqualTo(FAILED);
        assertThat(result.getException()).hasMessageContaining("An unknown error");
        assertThat(result.getException()).hasCauseInstanceOf(IOException.class);
    }

    @ParameterizedTest
    @ValueSource(classes = { LifecycleBeforeFlowErrorStep.class, LifecycleAfterFlowErrorStep.class })
    void testExceptionDuringLifecycleFlowStreamedConcurrent(Class<? extends Step> stepType) throws ExecutionException {
        StaticStepRepository.register(stepType);

        final var builder = StaticFlowBuilderFactory.builder(None.class, None.class);
        builder.concurrent();

        var flow = builder.append(stepType).build();
        var source = new SimpleSource<>(Stream.of(None.value()));

        flow.stream(source, new SimpleSink<>());

        // REMARK: currently we don't have a handle to check if the exception is correctly logged. We expect the code to run through though
    }

    @ParameterizedTest
    @ValueSource(classes = { LifecycleBeforeFlowErrorStep.class, LifecycleAfterFlowErrorStep.class, })
    void testExceptionDuringLifecycleFlowStreamed(Class<? extends Step> stepType)
        throws ExecutionException, ValidationException, InterruptedException {
        StaticStepRepository.register(stepType);

        final var builder = StaticFlowBuilderFactory.builder(None.class, None.class);

        var flow = builder.append(stepType).build();
        var source = new SimpleSource<>(Stream.of(None.value()));

        assertThatExceptionOfType(ExecutionException.class).isThrownBy(() -> flow.stream(source, new SimpleSink<>()))
            .withMessageContaining("An unknown error").withCauseInstanceOf(IOException.class);
    }

    @ParameterizedTest
    @MethodSource("testParamsFlowScoped")
    void testExceptionDuringLifecycleFlow(boolean concurrent, Class<? extends Step> stepType)
        throws ExecutionException, ValidationException {
        StaticStepRepository.register(stepType);

        final var builder = StaticFlowBuilderFactory.builder(None.class, None.class);
        if (concurrent) {
            builder.concurrent();
        }

        var flow = builder.append(stepType).build();
        assertThatExceptionOfType(ExecutionException.class).isThrownBy(() -> flow.invoke(None.value()))
            .withMessageContaining("An unknown error").withCauseInstanceOf(IOException.class);
    }

    static class LifecycleBeforeStepErrorStep extends NoopStep {
        @Before(Scope.STEP)
        void beforeStep(Context<None> context) throws IOException {
            throw new IOException();
        }
    }

    static class LifecycleAfterStepErrorStep extends NoopStep {
        @After(Scope.STEP)
        void afterStep(Context<None> context) throws IOException {
            throw new IOException();
        }
    }

    static class LifecycleBeforeFlowErrorStep extends NoopStep {
        @Before(Scope.FLOW)
        void beforeStep(Context<None> context) throws IOException {
            throw new IOException();
        }
    }

    static class LifecycleAfterFlowErrorStep extends NoopStep {
        @After(Scope.FLOW)
        void afterStep(Context<None> context) throws IOException {
            throw new IOException();
        }
    }

}