package user11681.jpp.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Target;

/**
 * Mark the annotated element as an entrypoint.
 */
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
@Repeatable(Entrypoint.Entrypoints.class)
public @interface Entrypoint {
    String DEFAULT_ID = "jpp";
    String DEFAULT_ADAPTER = "default";

    /**
     * @return the identifier of the mod to which the annotated type or method belongs.
     */
    String id() default DEFAULT_ID;

    /**
     * @return the identifiers of the entrypoints to receive.
     */
    String[] value();

    /**
     * @return the language adapter that should be used.
     */
    String adapter() default DEFAULT_ADAPTER;

    @Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
    @interface Entrypoints {
        Entrypoint[] value();
    }
}
