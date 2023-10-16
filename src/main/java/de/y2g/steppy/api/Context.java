package de.y2g.steppy.api;

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

    protected <T> T getState(String name, Scope scope) {
        var requestedScopeId = switch (scope) {
            case FLOW -> GLOBAL_SCOPE;
            default -> scopeId;
        };
        var identifier = requestedScopeId + "." + name;

        return state.getState(requestedScopeId, name);
    }

    protected <T> void setState(String name, Scope scope, T value) {
        var requestedScopeId = switch (scope) {
            case FLOW -> GLOBAL_SCOPE;
            default -> scopeId;
        };
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
