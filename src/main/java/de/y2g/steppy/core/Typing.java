package de.y2g.steppy.core;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class Typing<C, I, R> {
    private final Class<C> configType;

    private final Class<I> inputType;

    private final Class<R> returnType;

    public Typing(Class<C> configType, Class<I> inputType, Class<R> returnType) {
        this.configType = configType;
        this.inputType = inputType;
        this.returnType = returnType;
    }

    public static boolean isInputTypeCompatible(Typing current, Typing successor) {
        return successor.inputType.isAssignableFrom(current.returnType);
    }

    public static boolean isInputTypeCompatible(Class<?> inputType, Typing current) {
        return inputType.isAssignableFrom(current.inputType);
    }

    public static boolean isReturnTypeCompatible(Class<?> returnType, Typing current) {
        return returnType.isAssignableFrom(current.returnType);
    }

    public static boolean isConfigTypeCompatible(Class<?> configType, Typing a) {
        return a.getConfigType().isAssignableFrom(configType);
    }

    public Class<C> getConfigType() {
        return configType;
    }

    public Class<I> getInputType() {
        return inputType;
    }

    public Class<R> getReturnType() {
        return returnType;
    }

}
