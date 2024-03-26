package de.y2g.steppy.core;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ReflectionUtils {
    public static <T extends Annotation> List<T> getAnnotationsByType(Annotation annotation, Class<T> annotationType) {
        List<T> annotations = new ArrayList<>();
        for (Annotation a: annotation.annotationType().getAnnotations()) {
            if (annotationType.isInstance(a)) {
                annotations.add(annotationType.cast(a));
            } else if (a.annotationType().getPackageName().startsWith("de.y2g.steppy.api")) {
                annotations.addAll(getAnnotationsByType(a, annotationType));
            }
        }

        return annotations;
    }

    public static <T extends Annotation> List<T> getAnnotationsByType(Field field, Class<T> annotationType) {
        List<T> annotations = new ArrayList<>();
        for (Annotation a: field.getAnnotations()) {
            if (annotationType.isInstance(a)) {
                annotations.add(annotationType.cast(a));
            } else {
                annotations.addAll(getAnnotationsByType(a, annotationType));
            }
        }
        return annotations;
    }

    public static <T extends Annotation> T getAnnotation(Field field, Class<T> annotationType) {
        return getAnnotationsByType(field, annotationType).stream().findFirst().orElse(null);
    }

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