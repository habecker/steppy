package de.y2g.steppy.core;

import de.y2g.steppy.api.validation.ValidationError;
import de.y2g.steppy.api.validation.ValidationException;

import java.util.ArrayList;
import java.util.List;

public interface Verifiable {
    static void verifyAll(List<? extends Verifiable> verifiables, List<Dependency> providedDependencies) throws ValidationException {
        List<ValidationError> errors = new ArrayList<>();

        for (var verifiable: verifiables) {
            try {
                verifiable.verify(providedDependencies);
            } catch (ValidationException e) {
                errors.addAll(e.getErrors());
            }
        }
        if (!errors.isEmpty())
            throw new ValidationException(errors);
    }

    void verify(List<Dependency> providedDependencies) throws ValidationException;
}
