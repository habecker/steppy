package de.y2g.steppy.api.validation;

import de.y2g.steppy.core.StepIdentifier;

import java.util.Arrays;
import java.util.List;

public class ValidationError {
    private final ValidationErrorType type;

    private final StepIdentifier[] steps;

    public ValidationError(ValidationErrorType type, StepIdentifier... steps) {
        this.type = type;
        this.steps = steps;
    }

    public ValidationErrorType getType() {
        return type;
    }

    public List<String> getSteps() {
        return Arrays.stream(steps).map(StepIdentifier::name).toList();
    }
}
