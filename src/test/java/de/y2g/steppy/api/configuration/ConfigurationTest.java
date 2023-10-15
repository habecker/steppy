package de.y2g.steppy.api.configuration;

import de.y2g.steppy.api.None;
import de.y2g.steppy.api.Context;
import de.y2g.steppy.api.Step;
import de.y2g.steppy.api.exception.ExecutionException;
import de.y2g.steppy.api.validation.ValidationException;
import de.y2g.steppy.pojo.StaticFlowBuilderFactory;
import de.y2g.steppy.pojo.StaticStepRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Executors;

import static de.y2g.steppy.pojo.StaticFlowBuilderFactory.builder;
import static org.assertj.core.api.Assertions.assertThat;

class ConfigurationTest {

    private static class ReturnConfigStep implements Step<Integer, None, Integer> {
        @Override
        public Integer invoke(Context<Integer> context, None input) throws ExecutionException {
            return context.getConfiguration();
        }
    }

    @BeforeAll
    static void setup() {
        StaticStepRepository.register(ReturnConfigStep.class);
        StaticFlowBuilderFactory.initialize(Executors.newSingleThreadExecutor());
    }

    @Test
    void testAccessConfiguration() throws ExecutionException, ValidationException {
        var flow = builder(Integer.class, None.class, Integer.class)
            .append(ReturnConfigStep.class)
            .build();
        assertThat(flow.invoke(10).getResult()).isEqualTo(10);
    }
}