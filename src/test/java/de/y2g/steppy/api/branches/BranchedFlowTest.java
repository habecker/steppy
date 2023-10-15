package de.y2g.steppy.api.branches;

import de.y2g.steppy.api.None;
import de.y2g.steppy.api.IncrementerStep;
import de.y2g.steppy.api.exception.ExecutionException;
import de.y2g.steppy.api.validation.ValidationException;
import de.y2g.steppy.pojo.StaticFlowBuilderFactory;
import de.y2g.steppy.pojo.StaticStepRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Executors;

import static de.y2g.steppy.api.Result.Type.FAILED;
import static de.y2g.steppy.pojo.StaticFlowBuilderFactory.builder;
import static org.assertj.core.api.Assertions.assertThat;

class BranchedFlowTest {

    @BeforeAll
    static void setup() {
        StaticStepRepository.register(IncrementerStep.class);
        StaticFlowBuilderFactory.initialize(Executors.newSingleThreadExecutor());
    }

    @Test
    void testBranchedWithPreceedingStep() throws ExecutionException, ValidationException {
        var flow = builder(None.class, Integer.class, Integer.class)
            .append(IncrementerStep.class)
            .branch(
            Integer.class,
            Integer.class,
            builder ->
                builder.when(
                    (any, i) -> i < 10,
                    (b) -> b.append(IncrementerStep.class)
                ).otherwise(
                    b -> b.append(IncrementerStep.class)
                        .append(IncrementerStep.class)
                )
        ).build();
        assertThat(flow.invoke(None.value(), 0).getResult()).isEqualTo(2);
        assertThat(flow.invoke(None.value(), 10).getResult()).isEqualTo(13);
    }


    @Test
    void testBranched() throws ExecutionException, ValidationException {
        var flow = builder(None.class, Integer.class, Integer.class).branch(
            Integer.class,
            Integer.class,
            builder ->
                builder.when(
                    (any, i) -> i < 10,
                    (b) -> b.append(IncrementerStep.class)
                ).otherwise(
                    b -> b.append(IncrementerStep.class)
                        .append(IncrementerStep.class)
                )
        ).build();
        assertThat(flow.invoke(None.value(), 0).getResult()).isEqualTo(1);
        assertThat(flow.invoke(None.value(), 10).getResult()).isEqualTo(12);
    }

    @Test
    void testOtherwiseContinue() throws ExecutionException, ValidationException {
        var flow = builder(None.class, Integer.class, Integer.class).branch(
            Integer.class,
            Integer.class,
            builder ->
                builder.when(
                    (any, i) -> i < 10,
                    (b) -> b.append(IncrementerStep.class)
                ).otherwiseContinue()
        ).build();
        assertThat(flow.invoke(None.value(), 0).getResult()).isEqualTo(1);
        assertThat(flow.invoke(None.value(), 10).getResult()).isEqualTo(10);
    }

    @Test
    void testMissingBranch() throws ExecutionException, ValidationException {
        var flow = builder(None.class, Integer.class, Integer.class).branch(
            Integer.class,
            Integer.class,
            builder ->
                builder.when(
                    (any, i) -> i < 10,
                    (b) -> b.append(IncrementerStep.class)
                )
        ).build();

        var result = flow.invoke(None.value(), 10);
        assertThat(result.getType()).isEqualTo(FAILED);
        assertThat(result.getException()).isInstanceOf(ExecutionException.class);
        assertThat(result.getException()).hasMessageContaining("No branch found");
    }
}