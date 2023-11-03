package de.y2g.steppy.api.sequential;

import de.y2g.steppy.api.AbortStep;
import de.y2g.steppy.api.AppendAStep;
import de.y2g.steppy.api.AppendBStep;
import de.y2g.steppy.api.FailStep;
import de.y2g.steppy.api.IncrementerStep;
import de.y2g.steppy.api.None;
import de.y2g.steppy.api.Result;
import de.y2g.steppy.api.RuntimeErrorStep;
import de.y2g.steppy.api.exception.ExecutionException;
import de.y2g.steppy.api.validation.ValidationException;
import de.y2g.steppy.pojo.StaticFlowBuilderFactory;
import de.y2g.steppy.pojo.StaticStepRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatRuntimeException;

class SequentialFlowTest {

    @BeforeAll
    static void setup() {
        StaticStepRepository.register(AppendAStep.class, AppendBStep.class, IncrementerStep.class, AbortStep.class, FailStep.class,
            RuntimeErrorStep.class, AbortBeforeStep.class);
        StaticFlowBuilderFactory.initialize(Executors.newSingleThreadExecutor());
    }

    @Test
    void testSequential() throws ExecutionException, ValidationException {
        var flow = StaticFlowBuilderFactory.builder(None.class, String.class, String.class).append(AppendAStep.class)
            .append(AppendBStep.class).append(AppendAStep.class).append(AppendBStep.class).build();
        assertThat(flow.invoke(None.value(), "").getResult()).isEqualTo("ABAB");
    }

    @Test
    void testFailure() throws ExecutionException, ValidationException {
        var flow = StaticFlowBuilderFactory.builder(None.class, String.class, String.class).append(AppendAStep.class).append(FailStep.class)
            .build();
        var result = flow.invoke(None.value(), List.of(""));

        assertThat(result).hasSize(1);
        var singleResult = result.stream().toList().get(0);
        assertThat(singleResult.getType()).isEqualTo(Result.Type.FAILED);
        assertThat(singleResult.getException()).hasMessage("Fail");
    }

    @Test
    void testRuntimeError() throws ValidationException {
        var flow = StaticFlowBuilderFactory.builder(None.class, String.class, String.class).append(AppendAStep.class)
            .append(RuntimeErrorStep.class).build();
        assertThatRuntimeException().isThrownBy(() -> {
            flow.invoke(None.value(), List.of(""));
        }).withMessage("Oh no");
    }

    @Test
    void testAbort() throws ValidationException, ExecutionException {
        var flow = StaticFlowBuilderFactory.builder(None.class, String.class, String.class).append(AppendAStep.class)
            .append(AbortStep.class).build();
        assertThat(flow.invoke(None.value(), "").getType()).isEqualTo(Result.Type.ABORTED);
    }

    @Test
    void testAbortBefore() throws ValidationException, ExecutionException {
        var flow = StaticFlowBuilderFactory.builder(None.class, String.class, String.class).append(AppendAStep.class)
            .append(AbortBeforeStep.class).build();
        assertThat(flow.invoke(None.value(), "").getType()).isEqualTo(Result.Type.ABORTED);
    }
}