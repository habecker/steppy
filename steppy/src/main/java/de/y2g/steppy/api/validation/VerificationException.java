package de.y2g.steppy.api.validation;

import java.util.List;

public class VerificationException extends Exception {
    private final List<VerificationError> errors;

    public VerificationException(String message, List<VerificationError> errors) {
        super(message);
        this.errors = errors;
    }

    public List<VerificationError> getErrors() {
        return errors;
    }

    @Override
    public String toString() {
        return "VerificationException{" +
                "errors=" + errors +
                '}';
    }
}
