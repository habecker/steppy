package de.y2g.steppy.api.validation;

import de.y2g.steppy.core.StepIdentifier;

import java.util.Arrays;
import java.util.List;

public class DataFlowValidationError implements ValidationError {
    private final ValidationErrorType type;

    private final StepIdentifier[] steps;

    public DataFlowValidationError(ValidationErrorType type, StepIdentifier... steps) {
        this.type = type;
        this.steps = steps;
    }

    @Override
    public ValidationErrorType getType() {
        return type;
    }

    @Override
    public List<String> getSteps() {
        return Arrays.stream(steps).map(StepIdentifier::name).toList();
    }

    @Override
    public String getMessage() {
        return "Validation failed for steps: " + String.join(", ", getSteps());
    }
}
