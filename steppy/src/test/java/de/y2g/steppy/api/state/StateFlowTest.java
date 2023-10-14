package de.y2g.steppy.api.state;

import de.y2g.steppy.api.None;
import de.y2g.steppy.api.exception.ExecutionException;
import de.y2g.steppy.api.validation.ValidationEception;
import de.y2g.steppy.pojo.StaticFlowBuilderFactory;
import de.y2g.steppy.pojo.StaticStepRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

class StateFlowTest {

    @BeforeAll
    static void setup() {
        StaticStepRepository.register(StateStep.class);
        StaticStepRepository.register(GlobalStateStepA.class);
        StaticStepRepository.register(GlobalStateStepB.class);
        StaticFlowBuilderFactory.initialize(Executors.newSingleThreadExecutor());
    }

    @Test
    void testLocalState() throws ExecutionException, ValidationEception {
        var flow = StaticFlowBuilderFactory.builder(None.class, Integer.class, Integer.class)
            .append(StateStep.class)
            .append(StateStep.class)
            .append(StateStep.class)
            .append(StateStep.class)
            .build();
        assertThat(flow.invoke(None.value(), 1).getResult()).isEqualTo(5 );
    }

    @Test
    void testGlobalState() throws ExecutionException, ValidationEception {
        var flow = StaticFlowBuilderFactory.builder(None.class, Integer.class, Integer.class)
            .append(GlobalStateStepA.class)
            .append(GlobalStateStepA.class)
            .append(GlobalStateStepB.class)
            .append(GlobalStateStepB.class)
            .build();
        assertThat(flow.invoke(None.value(), 1).getResult()).isEqualTo(5 );
    }
}