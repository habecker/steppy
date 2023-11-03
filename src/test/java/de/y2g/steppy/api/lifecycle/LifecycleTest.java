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

import static org.assertj.core.api.Assertions.assertThat;

class LifecycleTest {
    @BeforeAll
    static void setup() {
        StaticFlowBuilderFactory.initialize(Executors.newSingleThreadExecutor());
    }

    @Test
    void testSequential() throws ExecutionException, ValidationException {
        AtomicReference<String> result = new AtomicReference<>();
        StaticStepRepository.register("lifecycle", new LifecycleStep(result::set));
        var flow = StaticFlowBuilderFactory.builder(None.class, None.class, None.class).append("lifecycle").append("lifecycle").build();
        flow.invoke(None.value(), None.value());

        assertThat(result.get()).isEqualTo("before-flow,before-flow,before-step,after-step,before-step,after-step,after-flow,after-flow");
    }

    @Test
    void testConcurrent() throws ExecutionException, ValidationException {
        AtomicReference<String> result = new AtomicReference<>();
        StaticStepRepository.register("lifecycle", new LifecycleStep(result::set));
        var flow = StaticFlowBuilderFactory.builder(None.class, None.class, None.class).append("lifecycle").append("lifecycle").concurrent()
            .build();
        flow.invoke(None.value(), None.value());

        assertThat(result.get()).isEqualTo("before-flow,before-flow,before-step,after-step,before-step,after-step,after-flow,after-flow");
    }

    @Test
    void testBeforeWithoutContext() throws ExecutionException, ValidationException {
        class LifecycleWithoutContext implements Step<None, None, None> {
            static boolean beforeFlowCalled = false;

            @Before(Scope.STEP)
            void beforeStep() throws IOException {
                beforeFlowCalled = true;
            }

            @Override
            public None invoke(Context<None> context, None input) throws ExecutionException {
                return None.value();
            }
        }

        StaticStepRepository.register("lifecycle-no-context", new LifecycleWithoutContext());
        var flow = StaticFlowBuilderFactory.builder(None.class, None.class, None.class).append("lifecycle-no-context").build();
        flow.invoke(None.value(), None.value());
        assertThat(LifecycleWithoutContext.beforeFlowCalled).isTrue();
    }

    static class LifecycleStep implements Step<None, None, None> {
        private final Consumer<String> lifecycleConsumer;

        @State(scope = Scope.FLOW)
        private Variable<String> state;

        LifecycleStep(Consumer<String> lifecycleConsumer) {
            this.lifecycleConsumer = lifecycleConsumer;
        }

        @Before(Scope.FLOW)
        void beforeFlow(Context<None> context) {
            var value = state.get(context);
            if (value == null) {
                state.set(context, "before-flow");
            } else {
                state.set(context, value + ",before-flow");
            }
        }

        @After(Scope.FLOW)
        void afterFlow(Context<None> context) {
            state.set(context, state.get(context) + ",after-flow");
            lifecycleCallback(context);
        }

        @Before(Scope.STEP)
        void beforeStep(Context<None> context) {
            state.set(context, state.get(context) + ",before-step");
        }

        @After(Scope.STEP)
        void afterStep(Context<None> context) {
            state.set(context, state.get(context) + ",after-step");
        }

        @Override
        public None invoke(Context<None> context, None input) throws ExecutionException {
            return None.value();
        }

        void lifecycleCallback(Context<None> context) {
            lifecycleConsumer.accept(state.get(context));
        }
    }
}