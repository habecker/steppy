package de.y2g.steppy.api;

public final class Variable<T> {
    private final String scopeId;
    private final String name;

    public Variable(String scopeId, String name) {
        this.scopeId = scopeId;
        this.name = name;
    }

    public T get(Context<?> context) {
        return context.getState().getState(scopeId, name);
    }

    public void set(Context<?> context, T value) {
        context.getState().setState(scopeId, name, value);
    }
}
