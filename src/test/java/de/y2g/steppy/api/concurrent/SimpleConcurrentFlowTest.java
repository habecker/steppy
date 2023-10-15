package de.y2g.steppy.api.concurrent;

import de.y2g.steppy.api.None;
import de.y2g.steppy.api.AppendAStep;
import de.y2g.steppy.api.AppendBStep;
import de.y2g.steppy.api.FailStep;
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

class SimpleConcurrentFlowTest {

    @BeforeAll
    static void setup() {
        StaticStepRepository.register(AppendAStep.class);
        StaticStepRepository.register(AppendBStep.class);
        StaticStepRepository.register(FailStep.class);
        StaticStepRepository.register(RuntimeErrorStep.class);
        StaticFlowBuilderFactory.initialize(Executors.newSingleThreadExecutor());
    }

    @Test
    void testSimple() throws ExecutionException, ValidationException {
        var flow = StaticFlowBuilderFactory.builder(None.class, String.class, String.class).append(AppendAStep.class)
            .append(AppendBStep.class).append(AppendAStep.class).append(AppendBStep.class).concurrent().build();
        assertThat(flow.invoke(None.value(), List.of("", "")).stream().map(Result::getResult)).isEqualTo(List.of("ABAB", "ABAB"));
    }

    @Test
    void testFailure() throws ExecutionException, ValidationException {
        var flow = StaticFlowBuilderFactory.builder(None.class, String.class, String.class).append(AppendAStep.class).append(FailStep.class).concurrent().build();
        var result = flow.invoke(None.value(), List.of(""));

        assertThat(result).hasSize(1);
        var singleResult = result.stream().toList().get(0);
        assertThat(singleResult.getType()).isEqualTo(Result.Type.FAILED);
        assertThat(singleResult.getException()).hasMessage("Fail");
    }

    @Test
    void testRuntimeError() throws ExecutionException, ValidationException {
        var flow = StaticFlowBuilderFactory.builder(None.class, String.class, String.class).append(AppendAStep.class)
            .append(RuntimeErrorStep.class).concurrent().build();
        var result = flow.invoke(None.value(), List.of(""));

        assertThat(result).hasSize(1);
        var singleResult = result.stream().toList().get(0);
        assertThat(singleResult.getType()).isEqualTo(Result.Type.FAILED);
        assertThat(singleResult.getException()).hasMessage("Oh no");
    }
}