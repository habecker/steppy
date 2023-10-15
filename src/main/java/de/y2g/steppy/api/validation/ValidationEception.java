package de.y2g.steppy.api.validation;

import java.util.List;

public class ValidationEception extends Exception {
    private final List<ValidationError> errors;

    public ValidationEception(String message, List<ValidationError> errors) {
        super(message);
        this.errors = errors;
    }

    public List<ValidationError> getErrors() {
        return errors;
    }
}
