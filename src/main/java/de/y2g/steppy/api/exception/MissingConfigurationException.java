package de.y2g.steppy.api.exception;

import java.util.List;

public class MissingConfigurationException extends ExecutionException {
    private final List<Class<?>> missingConfigurations;

    public MissingConfigurationException(String message, List<Class<?>> missingConfigurations) {
        super(message);
        this.missingConfigurations = missingConfigurations;
    }

    @Override
    public String getMessage() {
        return super.getMessage() + ". Missing: " + missingConfigurations;
    }

    public List<Class<?>> getMissingConfigurations() {
        return missingConfigurations;
    }
}
