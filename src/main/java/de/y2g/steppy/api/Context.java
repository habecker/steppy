package de.y2g.steppy.api;

import static de.y2g.steppy.api.Scope.FLOW;

public final class Context<C> {
    private static final String GLOBAL_SCOPE = "global";

    private final C configuration;

    private final String scopeId;

    private final ExecutionState state;

    private boolean abort = false;

    public Context(C configuration) {
        this(GLOBAL_SCOPE, configuration, new ExecutionState());
    }

    private Context(String scopeId, Context<C> superior) {
        this(scopeId, superior.configuration, superior.state);
    }

    Context(String scopeId, C configuration, ExecutionState state) {
        this.scopeId = scopeId;
        this.configuration = configuration;
        this.state = state;
    }

    public C getConfiguration() {
        return configuration;
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

    public boolean isAbort() {
        return abort;
    }

    public Context<C> sub(String scope) {
        return new Context<>(scope, this);
    }
}
