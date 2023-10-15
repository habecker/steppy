package de.y2g.steppy.api;

public class Result<R> {
    R result;
    Type type;
    Throwable throwable;

    public Result(R result, Type type) {
        this.result = result;
        this.type = type;
    }

    public Result(Type type) {
        this(null, type);
    }

    public Result(Type type, Throwable throwable) {
        this(null, type);
        this.throwable = throwable;
    }

    public Throwable getException() {
        return throwable;
    }

    public R getResult() {
        return result;
    }

    public Type getType() {
        return type;
    }

    public enum Type {
        SUCCEEDED,
        ABORTED,
        FAILED
    }
}
