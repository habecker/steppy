package de.y2g.steppy.api;

public final class Context<C> {
    private final C configuration;
    private final ExecutionState state;
    private boolean abort = false;

    public Context(C configuration) {
        this(configuration, new ExecutionState());
    }

    private Context(Context<C> superior) {
        this(superior.configuration, superior.state);
    }

    Context(C configuration, ExecutionState state) {
        this.configuration = configuration;
        this.state = state;
    }

    public C getConfiguration() {
        return configuration;
    }

    protected ExecutionState getState() {
        return state;
    }

    public void abort() {
        abort = true;
    }

    public boolean isAbort() {
        return abort;
    }

    public Context<C> sub() {
        return new Context<>(this);
    }
}
