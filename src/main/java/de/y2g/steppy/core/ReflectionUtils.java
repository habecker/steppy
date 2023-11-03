package de.y2g.steppy.core;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ReflectionUtils {

    public static <A extends Annotation> List<Method> findMethodsByAnnotation(Class<?> type, Class<A> annotationType, Predicate<A> filter) {
        return Arrays.stream(type.getDeclaredMethods()).filter(method -> {
            Class<?>[] params = method.getParameterTypes();
            if (params.length > 0)
                return false;
            return Arrays.stream(method.getAnnotationsByType(annotationType)).anyMatch(filter);
        }).collect(Collectors.toList());
    }

    public static <A extends Annotation> List<Method> findMethodsByAnnotation(Class<?> type, Class<A> annotationType, Predicate<A> filter,
        Class<?> parameterType) {
        return Arrays.stream(type.getDeclaredMethods()).filter(method -> {
            Class<?>[] params = method.getParameterTypes();
            if (params.length != 1)
                return false;
            return params[0].equals(parameterType) && Arrays.stream(method.getAnnotationsByType(annotationType)).anyMatch(filter);
        }).collect(Collectors.toList());
    }
}