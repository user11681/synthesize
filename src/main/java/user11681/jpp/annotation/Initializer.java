package user11681.jpp.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

import static user11681.jpp.annotation.Initializer.Type.CONSTRUCTOR;

/**
 * This annotation interface marks a method as an initializer.<br>
 * {@code static} methods become static initializers and non-{@code static} method become instance initializers.
 */
@Target(ElementType.METHOD)
public @interface Initializer {
    /**
     * @return the type of this initializer. See <b>{@link Type}</b> for more information.
     */
    Type type() default CONSTRUCTOR;

    /**
     * This enum defines the type of an initializer.
     */
    enum Type {
        /**
         * an instance initializer that is inlined into the end of every constructor.<br>
         * If a constructor is not found, then it is generated.
         */
        INITIALIZER,

        /**
         * a constructor.
         */
        CONSTRUCTOR
    }
}
