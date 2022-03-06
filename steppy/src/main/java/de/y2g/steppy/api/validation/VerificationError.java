package de.y2g.steppy.api.validation;

import de.y2g.steppy.core.StepIdentifier;

import java.util.Arrays;

public class VerificationError {
    VerificationErrorType type;
    StepIdentifier[] steps;

    public VerificationError(VerificationErrorType type, StepIdentifier... steps) {
        this.type = type;
        this.steps = steps;
    }

    @Override
    public String toString() {
        return "VerificationError{" +
                "type=" + type +
                ", steps=" + Arrays.toString(steps) +
                '}';
    }
}
