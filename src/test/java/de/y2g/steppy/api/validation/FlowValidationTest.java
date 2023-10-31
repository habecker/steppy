package de.y2g.steppy.api.validation;

import de.y2g.steppy.api.None;
import de.y2g.steppy.api.AppendAStep;
import de.y2g.steppy.api.AppendBStep;
import de.y2g.steppy.api.IncrementerStep;
import de.y2g.steppy.pojo.StaticFlowBuilderFactory;
import de.y2g.steppy.pojo.StaticStepRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class FlowValidationTest {

    @BeforeAll
    static void setup() {
        StaticStepRepository.register(AppendAStep.class);
        StaticStepRepository.register(AppendBStep.class);
        StaticStepRepository.register(IncrementerStep.class);
        StaticFlowBuilderFactory.initialize(Executors.newSingleThreadExecutor());
    }

    @Test
    void incompatibleIntermediate() {
        assertThatExceptionOfType(ValidationException.class).isThrownBy(
                () -> StaticFlowBuilderFactory.builder(None.class, String.class, Integer.class).append(AppendAStep.class)
                    .append(IncrementerStep.class).build()).matches(e -> e.getErrors().size() == 1, "Must have exactly one error")
            .matches(e -> e.getErrors().get(0).getType() == ValidationErrorType.STEP_INPUT_TYPE_INCOMPATIBLE,
                "Must be of type STEP_INPUT_TYPE_INCOMPATIBLE").matches(e -> e.getErrors().get(0).getSteps()
                .equals(List.of(AppendAStep.class.getCanonicalName(), IncrementerStep.class.getCanonicalName())));
    }

    @Test
    void incompatibleReturnType() {
        assertThatExceptionOfType(ValidationException.class).isThrownBy(
                () -> StaticFlowBuilderFactory.builder(None.class, String.class, Integer.class).append(AppendAStep.class).build())
            .matches(e -> e.getErrors().size() == 1, "Must have exactly one error")
            .matches(e -> e.getErrors().get(0).getType() == ValidationErrorType.FLOW_RETURN_TYPE_INCOMPATIBLE,
                "Must be of type STEP_INPUT_TYPE_INCOMPATIBLE").matches(e -> e.getErrors().get(0).getSteps()
                .equals(List.of(AppendAStep.class.getCanonicalName())));
    }

    @Test
    void incompatibleConfigType() {
        assertThatExceptionOfType(ValidationException.class).isThrownBy(
                () -> StaticFlowBuilderFactory.builder(String.class, String.class, String.class).append(AppendAStep.class).build())
            .matches(e -> e.getErrors().size() == 1, "Must have exactly one error")
            .matches(e -> e.getErrors().get(0).getType() == ValidationErrorType.CONFIGURATION_TYPE_INCOMPATIBLE,
                "Must be of type STEP_INPUT_TYPE_INCOMPATIBLE").matches(e -> e.getErrors().get(0).getSteps()
                .equals(List.of(AppendAStep.class.getCanonicalName())));
    }

    @Test
    void incompatibleFlowInputType() {
        assertThatExceptionOfType(ValidationException.class).isThrownBy(
                () -> StaticFlowBuilderFactory.builder(None.class, Integer.class, String.class).append(AppendAStep.class).build())
            .matches(e -> e.getErrors().size() == 1, "Must have exactly one error")
            .matches(e -> e.getErrors().get(0).getType() == ValidationErrorType.FLOW_INPUT_TYPE_INCOMPATIBLE,
                "Must be of type STEP_INPUT_TYPE_INCOMPATIBLE").matches(e -> e.getErrors().get(0).getSteps()
                .equals(List.of(AppendAStep.class.getCanonicalName())));
    }

    @Test
    void nestedFlow() {
        assertThatExceptionOfType(ValidationException.class).isThrownBy(
                        () -> StaticFlowBuilderFactory.builder(None.class, Integer.class, String.class).nest(String.class, b -> b.append(AppendAStep.class)).build())
                .matches(e -> e.getErrors().size() == 1, "Must have exactly one error")
                .matches(e -> e.getErrors().get(0).getType() == ValidationErrorType.FLOW_INPUT_TYPE_INCOMPATIBLE,
                        "Must be of type STEP_INPUT_TYPE_INCOMPATIBLE").matches(e -> e.getErrors().get(0).getSteps()
                        .equals(List.of(AppendAStep.class.getCanonicalName())));
    }
}