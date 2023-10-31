package de.y2g.steppy.pojo;

import de.y2g.steppy.api.Context;
import de.y2g.steppy.api.None;
import de.y2g.steppy.api.Step;
import de.y2g.steppy.api.StepRepository;
import de.y2g.steppy.api.exception.ExecutionException;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.*;

class StaticFlowBuilderFactoryTest {
    @Test
    void testMustInitializeFactory() {
        StaticFlowBuilderFactory.reset();
        assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> StaticFlowBuilderFactory.builder(None.class, None.class, None.class)).withMessageContaining("was not initialized");
    }

    @Test
    void testinputTypeMissingStep() {
        StaticFlowBuilderFactory.initialize(Executors.newSingleThreadExecutor());
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() ->
                StaticFlowBuilderFactory.builder(None.class, None.class, None.class).append("does-not-exist").build()).withMessageContaining("Step with name does-not-exist was not found in the repository");
    }

    protected static class UninstantiableStep implements Step<None, None, None> {

        public UninstantiableStep() {
            throw new IllegalArgumentException("Nope");
        }

        @Override
        public None invoke(Context<None> context, None input) throws ExecutionException {
            return null;
        }
    }

    @Test
    void testCouldNotInstantiateStep() {
        StaticFlowBuilderFactory.initialize(Executors.newSingleThreadExecutor());

        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() ->
               StaticStepRepository.register(UninstantiableStep.class)).withMessageContaining("could not be instantiated.");
    }

}