package de.y2g.steppy.api.lifecycle;

import de.y2g.steppy.api.After;
import de.y2g.steppy.api.Before;
import de.y2g.steppy.api.Context;
import de.y2g.steppy.api.None;
import de.y2g.steppy.api.Scope;
import de.y2g.steppy.api.State;
import de.y2g.steppy.api.Step;
import de.y2g.steppy.api.Variable;
import de.y2g.steppy.api.exception.ExecutionException;
import de.y2g.steppy.api.validation.ValidationException;
import de.y2g.steppy.pojo.StaticFlowBuilderFactory;
import de.y2g.steppy.pojo.StaticStepRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static de.y2g.steppy.api.Result.Type.FAILED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class LifecycleErrorTest {
    static class LifecycleErrorStep implements Step<None, None, None> {
        @Before(Scope.STEP)
        void beforeStep(Context<None> context) throws IOException {
            throw new IOException();
        }

        @Override
        public None invoke(Context<None> context, None input) throws ExecutionException {
            return None.value();
        }
    }

    @BeforeAll
    static void setup() {
        StaticFlowBuilderFactory.initialize(Executors.newSingleThreadExecutor());
    }


    @Test
    void testExceptionDuringLifecycle() throws ExecutionException, ValidationException {
        StaticStepRepository.register(LifecycleErrorStep.class);
        var flow = StaticFlowBuilderFactory.builder(None.class, None.class, None.class)
            .append(LifecycleErrorStep.class)
            .build();
        var result = flow.invoke(None.value(), None.value());
        assertThat(result.getType()).isEqualTo(FAILED);
        assertThat(result.getException()).hasMessageContaining("An unknown error");
        assertThat(result.getException()).hasCauseInstanceOf(IOException.class);
    }
}