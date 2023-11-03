package de.y2g.steppy.api.sequential;

import de.y2g.steppy.api.Context;
import de.y2g.steppy.api.Step;
import de.y2g.steppy.api.exception.ExecutionException;
import de.y2g.steppy.api.validation.ValidationException;
import de.y2g.steppy.pojo.StaticFlowBuilderFactory;
import de.y2g.steppy.pojo.StaticStepRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class SimpleStep implements Step<Optional<Integer>, Optional<Integer>, Optional<Integer>> {
    @Override
    public Optional<Integer> invoke(Context<Optional<Integer>> context, Optional<Integer> input) throws ExecutionException {
        if (input.or(() -> context.getConfiguration()).isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(input.orElse(0) + context.getConfiguration().orElse(0));
    }
}

public class ParameterizedTypeFlowTest {

    @BeforeAll
    static void setup() {
        StaticStepRepository.register(SimpleStep.class);
        StaticFlowBuilderFactory.initialize(Runnable::run);
    }

    @Test
    void testSequential() throws ExecutionException, ValidationException {
        var flow = StaticFlowBuilderFactory.builder(Optional.class, Optional.class, Optional.class).append(SimpleStep.class).build();
        var result = flow.invoke(Optional.of(1), Optional.of(2));
        assertThat((Optional<Integer>)result.getResult()).isEqualTo(Optional.of(3));
    }
}