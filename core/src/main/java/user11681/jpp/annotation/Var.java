package user11681.jpp.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Target;
import org.objectweb.asm.Opcodes;

/**
 * a repeatable annotation that describes a synthetic field declaration.<br>
 * It is intended to be used with {@link Getter} or {@link Setter}.
 */
@Target(ElementType.TYPE)
@Repeatable(Var.Vars.class)
public @interface Var {
    /**
     * The default access for generated fields is {@code public}.
     */
    int DEFAULT_ACCESS = Opcodes.ACC_PUBLIC;

    /**
     * @return the {@linkplain Opcodes modifiers} of this field,<br>
     * which can be used in order make the field {@code final}, {@code private}, {@code abstract} and so on.
     */
    int access() default DEFAULT_ACCESS;

    /**
     * @return the name of the field to generate.
     */
    String name();

    /**
     * @return the descriptor or type name of the field to generate.
     * <br><br>
     * <h3>descriptors</h3>
     * void: V<br>
     * boolean: Z<br>
     * char: C<br>
     * byte: B<br>
     * short: S<br>
     * int: I<br>
     * long: J<br>
     * float: F<br>
     * double: D<br>
     * any non-array non-primitive type: the fully qualified name of the class in any valid format<br>
     * array: [ + descriptor of component class
     */
    String descriptor();

    @Target(ElementType.TYPE)
    @interface Vars {
        /**
         * @return information about the fields to generate in the annotated type.
         */
        Var[] value();
    }
}
