package de.y2g.steppy.api.state;

import de.y2g.steppy.api.exception.ExecutionException;
import de.y2g.steppy.api.validation.ValidationException;
import de.y2g.steppy.pojo.StaticFlowBuilderFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Executors;

import static de.y2g.steppy.pojo.StaticFlowBuilderFactory.initialize;
import static de.y2g.steppy.pojo.StaticStepRepository.register;
import static org.assertj.core.api.Assertions.assertThat;

class StateFlowTest {

    @BeforeAll
    static void setup() {
        register(StateStep.class);
        register(GlobalStateStepA.class);
        register(GlobalStateStepB.class);
        register(LifecycleStateStep.class);
        initialize(Executors.newSingleThreadExecutor());
    }

    @Test
    void testLocalState() throws ExecutionException, ValidationException {
        var flow = StaticFlowBuilderFactory.builder(Integer.class, Integer.class).append(StateStep.class).append(StateStep.class)
            .append(StateStep.class).append(StateStep.class).build();
        assertThat(flow.invoke(1).getResult()).isEqualTo(1);
    }

    @Test
    void testGlobalState() throws ExecutionException, ValidationException {
        var flow = StaticFlowBuilderFactory.builder(Integer.class, Integer.class).append(GlobalStateStepA.class)
            .append(GlobalStateStepA.class).append(GlobalStateStepB.class).append(GlobalStateStepB.class).build();
        assertThat(flow.invoke(1).getResult()).isEqualTo(5);
    }

    @DisplayName("Test access to variable in different lifecycle states")
    @Test
    void testLifecycleState() throws ExecutionException, ValidationException {
        var flow = StaticFlowBuilderFactory.builder(Integer.class, Integer.class).append(LifecycleStateStep.class).build();
        assertThat(flow.invoke(1).getResult()).isEqualTo(100);
        assertThat(LifecycleStateStep.values).containsExactly(100, 100, 100, 100, 100);
    }
}