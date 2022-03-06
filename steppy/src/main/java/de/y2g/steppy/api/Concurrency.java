package de.y2g.steppy.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Concurrency {
    Type value() default Type.ALLOW;

    enum Type {
        LOCK, ALLOW
    }
}
