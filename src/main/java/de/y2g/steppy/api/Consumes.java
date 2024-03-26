package de.y2g.steppy.api;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@State(scope = Scope.FLOW, readOnly = true)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Consumes {
    @AliasFor(annotation = State.class, attribute = "name") String name() default "";
}
