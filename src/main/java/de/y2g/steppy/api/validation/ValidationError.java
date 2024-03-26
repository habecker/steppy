package de.y2g.steppy.api.validation;

import java.util.List;

public interface ValidationError {
    ValidationErrorType getType();

    List<String> getSteps();

    String getMessage();
}
