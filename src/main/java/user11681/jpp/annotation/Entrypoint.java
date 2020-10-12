package user11681.jpp.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
@Repeatable(Entrypoint.Entrypoints.class)
public @interface Entrypoint {
    String DEFAULT_ADAPTER = "default";

    /**
     * @return the identifier of the mod to which the annotated type or method belongs.
     */
    String id();

    /**
     * @return the identifiers of the entrypoints to receive.
     */
    String[] entrypoints();

    /**
     * @return the language adapter that should be used.
     */
    String adapter() default DEFAULT_ADAPTER;

    @Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
    @interface Entrypoints {
        Entrypoint[] value();
    }
}
