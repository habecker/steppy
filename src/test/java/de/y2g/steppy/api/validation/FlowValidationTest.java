package de.y2g.steppy.api.validation;

import de.y2g.steppy.api.AppendAStep;
import de.y2g.steppy.api.AppendBStep;
import de.y2g.steppy.api.IncrementerStep;
import de.y2g.steppy.pojo.StaticFlowBuilderFactory;
import de.y2g.steppy.pojo.StaticStepRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class FlowValidationTest {

    @BeforeAll
    static void setup() {
        StaticStepRepository.register(AppendAStep.class);
        StaticStepRepository.register(AppendBStep.class);
        StaticStepRepository.register(IncrementerStep.class);
        StaticFlowBuilderFactory.initialize(Executors.newSingleThreadExecutor());
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void missingTypeParams(boolean concurrent) {
        StaticStepRepository.register(StepWithNoTypes.class);
        final var builder = StaticFlowBuilderFactory.builder(String.class, Integer.class);
        if (concurrent) {
            builder.concurrent();
        }
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> builder.append(StepWithNoTypes.class))
            .withMessageContaining("does not implement parameterized step interface.")
            .withMessageContaining(StepWithNoTypes.class.getCanonicalName());
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void incompatibleIntermediate(boolean concurrent) {
        final var builder = StaticFlowBuilderFactory.builder(String.class, Integer.class);
        if (concurrent) {
            builder.concurrent();
        }
        assertThatExceptionOfType(ValidationException.class).isThrownBy(
                () -> builder.append(AppendAStep.class).append(IncrementerStep.class).build())
            .matches(e -> e.getErrors().size() == 1, "Must have exactly one error")
            .matches(e -> e.getErrors().get(0).getType() == ValidationErrorType.STEP_INPUT_TYPE_INCOMPATIBLE,
                "Must be of type STEP_INPUT_TYPE_INCOMPATIBLE").matches(e -> e.getErrors().get(0).getSteps()
                .equals(List.of(AppendAStep.class.getCanonicalName(), IncrementerStep.class.getCanonicalName())));
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void incompatibleReturnType(boolean concurrent) {
        final var builder = StaticFlowBuilderFactory.builder(String.class, Integer.class);
        if (concurrent) {
            builder.concurrent();
        }
        assertThatExceptionOfType(ValidationException.class).isThrownBy(() -> builder.append(AppendAStep.class).build())
            .matches(e -> e.getErrors().size() == 1, "Must have exactly one error")
            .matches(e -> e.getErrors().get(0).getType() == ValidationErrorType.FLOW_RETURN_TYPE_INCOMPATIBLE,
                "Must be of type STEP_INPUT_TYPE_INCOMPATIBLE")
            .matches(e -> e.getErrors().get(0).getSteps().equals(List.of(AppendAStep.class.getCanonicalName())));
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void incompatibleFlowInputType(boolean concurrent) {
        final var builder = StaticFlowBuilderFactory.builder(Integer.class, String.class);
        if (concurrent) {
            builder.concurrent();
        }
        assertThatExceptionOfType(ValidationException.class).isThrownBy(() -> builder.append(AppendAStep.class).build())
            .matches(e -> e.getErrors().size() == 1, "Must have exactly one error")
            .matches(e -> e.getErrors().get(0).getType() == ValidationErrorType.FLOW_INPUT_TYPE_INCOMPATIBLE,
                "Must be of type STEP_INPUT_TYPE_INCOMPATIBLE")
            .matches(e -> e.getErrors().get(0).getSteps().equals(List.of(AppendAStep.class.getCanonicalName())));
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void nestedFlowReturn(boolean concurrent) {
        final var builder = StaticFlowBuilderFactory.builder(String.class, Integer.class);
        if (concurrent) {
            builder.concurrent();
        }
        assertThatExceptionOfType(ValidationException.class).isThrownBy(
                () -> builder.nest(String.class, b -> b.append(AppendAStep.class)).build())
            .matches(e -> e.getErrors().size() == 1, "Must have exactly one error")
            .matches(e -> e.getErrors().get(0).getType() == ValidationErrorType.FLOW_RETURN_TYPE_INCOMPATIBLE,
                "Must be of type FLOW_RETURN_TYPE_INCOMPATIBLE")
            .matches(e -> e.getErrors().get(0).getSteps().get(0).contains("nested-flow"));
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void nestedFlow(boolean concurrent) {
        final var builder = StaticFlowBuilderFactory.builder(Integer.class, String.class);
        if (concurrent) {
            builder.concurrent();
        }
        assertThatExceptionOfType(ValidationException.class).isThrownBy(
                () -> builder.nest(String.class, b -> b.append(AppendAStep.class)).build())
            .matches(e -> e.getErrors().size() == 1, "Must have exactly one error")
            .matches(e -> e.getErrors().get(0).getType() == ValidationErrorType.FLOW_INPUT_TYPE_INCOMPATIBLE,
                "Must be of type STEP_INPUT_TYPE_INCOMPATIBLE")
            .matches(e -> e.getErrors().get(0).getSteps().equals(List.of(AppendAStep.class.getCanonicalName())));
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void branchedFlow(boolean concurrent) {
        final var builder = StaticFlowBuilderFactory.builder(Integer.class, String.class);
        if (concurrent) {
            builder.concurrent();
        }
        assertThatExceptionOfType(ValidationException.class).isThrownBy(
                () -> builder.branch(Integer.class, String.class, b -> b.when((s) -> true, bb -> bb.append(AppendAStep.class))).build())
            .matches(e -> e.getErrors().size() == 1, "Must have exactly one error")
            .matches(e -> e.getErrors().get(0).getType() == ValidationErrorType.FLOW_INPUT_TYPE_INCOMPATIBLE,
                "Must be of type FLOW_INPUT_TYPE_INCOMPATIBLE")
            .matches(e -> e.getErrors().get(0).getSteps().equals(List.of(AppendAStep.class.getCanonicalName())));
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void branchedFlowReturn(boolean concurrent) {
        final var builder = StaticFlowBuilderFactory.builder(String.class, Integer.class);
        if (concurrent) {
            builder.concurrent();
        }
        assertThatExceptionOfType(ValidationException.class).isThrownBy(
                () -> builder.branch(String.class, String.class, b -> b.when((s) -> true, bb -> bb.append(AppendAStep.class))).build())
            .matches(e -> e.getErrors().size() == 1, "Must have exactly one error")
            .matches(e -> e.getErrors().get(0).getType() == ValidationErrorType.FLOW_RETURN_TYPE_INCOMPATIBLE,
                "Must be of type FLOW_RETURN_TYPE_INCOMPATIBLE")
            .matches(e -> e.getErrors().get(0).getSteps().get(0).contains("branched-flow"));
    }
}