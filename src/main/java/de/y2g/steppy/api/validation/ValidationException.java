package de.y2g.steppy.api.validation;

import java.util.List;

public class ValidationException extends RuntimeException {
    private final List<ValidationError> errors;

    public ValidationException(List<ValidationError> errors) {
        super("Flow verification failed with errors: " + errors.stream().map(ValidationError::getType).toList());
        this.errors = errors;
    }

    public List<ValidationError> getErrors() {
        return errors;
    }
}
