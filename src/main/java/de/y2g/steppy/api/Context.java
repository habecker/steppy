package de.y2g.steppy.api;

import static de.y2g.steppy.api.Scope.FLOW;

public final class Context<C> {
    private static final String GLOBAL_SCOPE = "global";

    private final Configurations configurations;

    private final String scopeId;

    private final ExecutionState state;

    private final Class<C> configurationType;

    private boolean abort = false;

    public Context(Configurations configurations, Class<C> configurationType) {
        this(GLOBAL_SCOPE, configurations, new ExecutionState(), configurationType);
    }

    private Context(String scopeId, Context<?> superior, Class<C> configurationType) {
        this(scopeId, superior.configurations, superior.state, configurationType);
    }

    Context(String scopeId, Configurations configurations, ExecutionState state, Class<C> configurationType) {
        this.scopeId = scopeId;
        this.configurations = configurations;
        this.state = state;
        this.configurationType = configurationType;
    }

    public C getConfiguration() {
        if (Configurations.class.isAssignableFrom(configurationType)) {
            return (C)configurations;
        } else if (configurationType.isAssignableFrom(Object.class) || None.class.isAssignableFrom(configurationType)) {
            return (C)None.value();
        }

        return configurations.get(configurationType);
    }

    <T> T getState(String name, Scope scope) {
        var requestedScopeId = (scope == FLOW) ? GLOBAL_SCOPE : scopeId;
        return state.getState(requestedScopeId, name);
    }

    <T> void setState(String name, Scope scope, T value) {
        var requestedScopeId = (scope == FLOW) ? GLOBAL_SCOPE : scopeId;
        state.setState(requestedScopeId, name, value);
    }

    public void abort() {
        abort = true;
    }

    public boolean isAborted() {
        return abort;
    }

    public <D> Context<D> sub(String scope, Class<D> configurationType) {
        return new Context<>(scope, this, configurationType);
    }
}
