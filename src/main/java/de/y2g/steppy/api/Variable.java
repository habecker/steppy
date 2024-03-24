package de.y2g.steppy.api;

import java.util.function.Consumer;
import java.util.function.Supplier;

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

    public T ifPresent(Context<?> context, Consumer<T> then) {
        T value = get(context);
        if (value != null) {
            then.accept(value);
        }
        return value;
    }

    public void set(Context<?> context, Supplier<T> supplier) {
        context.setState(name, scope, supplier.get());
    }

    public void set(Context<?> context, T value) {
        context.setState(name, scope, value);
    }
}
