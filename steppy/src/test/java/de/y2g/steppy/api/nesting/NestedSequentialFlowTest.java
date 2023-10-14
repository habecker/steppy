package de.y2g.steppy.api.nesting;

import de.y2g.steppy.api.None;
import de.y2g.steppy.api.AppendAStep;
import de.y2g.steppy.api.AppendBStep;
import de.y2g.steppy.api.IncrementerStep;
import de.y2g.steppy.api.exception.ExecutionException;
import de.y2g.steppy.api.validation.ValidationEception;
import de.y2g.steppy.pojo.StaticFlowBuilderFactory;
import de.y2g.steppy.pojo.StaticStepRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

class NestedSequentialFlowTest {

    @BeforeAll
    static void setup() {
        StaticStepRepository.register(AppendAStep.class);
        StaticStepRepository.register(AppendBStep.class);
        StaticStepRepository.register(IncrementerStep.class);
        StaticFlowBuilderFactory.initialize(Executors.newSingleThreadExecutor());
    }

    @Test
    void testSequential() throws ExecutionException, ValidationEception {
        var flow = StaticFlowBuilderFactory
            .builder(None.class, String.class, String.class)
            .append(AppendAStep.class)
            .nest(String.class, b ->
                b.append(AppendBStep.class)
                    .append(AppendAStep.class)
            )
            .build();
        assertThat(flow.invoke(None.value(), "").getResult()).isEqualTo("ABA");
    }

    @Test
    void testConcurrent() throws ExecutionException, ValidationEception {
        var flow = StaticFlowBuilderFactory
            .builder(None.class, String.class, String.class)
            .append(AppendAStep.class)
            .nest(String.class, b ->
                b
                    .append(AppendBStep.class)
                    .append(AppendAStep.class)
                    .concurrent()
            )
            .build();
        assertThat(flow.invoke(None.value(), "").getResult()).isEqualTo("ABA");
    }
}