package de.y2g.steppy.api.validation;

import de.y2g.steppy.core.Dependency;
import de.y2g.steppy.core.StepIdentifier;

import java.util.Collection;
import java.util.List;

public class DependencyError implements ValidationError {
    private final ValidationErrorType type;

    private final StepIdentifier step;

    private final Collection<Dependency> missingDependencies;

    public DependencyError(ValidationErrorType type, StepIdentifier step, Collection<Dependency> missingDependencies) {
        this.missingDependencies = missingDependencies;
        this.type = ValidationErrorType.MISSING_DEPENDENCIES;
        this.step = step;
    }

    @Override
    public ValidationErrorType getType() {
        return type;
    }

    @Override
    public List<String> getSteps() {
        return List.of(step.toString());
    }

    @Override
    public String getMessage() {
        return "Missing dependencies for step " + step.name() + ":" + String.join(", ",
            missingDependencies.stream().map(Dependency::toString).toList());
    }
}
