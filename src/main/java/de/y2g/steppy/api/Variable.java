package de.y2g.steppy.api;

public final class Variable<T> {
    private final String name;

    private final Scope scope;

    public Variable(Scope scope, String name) {
        this.name = name;
        this.scope = scope;
    }

    public T get(Context<?> context) {
        return context.getState(name, scope);
    }

    public void set(Context<?> context, T value) {
        context.setState(name, scope, value);
    }
}
