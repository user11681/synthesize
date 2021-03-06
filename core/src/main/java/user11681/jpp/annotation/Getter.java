package user11681.jpp.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import org.objectweb.asm.Opcodes;

/**
 * This annotation marks a method as a getter for a field with the name specified by {@link Getter#value} <br>
 * and of the same type as the return type of the said method.
 * <br><br>
 * The target's body is not deleted; rather, the field's value is loaded <br>
 * immediately before every return point.<br>
 * If a return point does not exist, then it is generated at the end of the target's body.
 * <br><br>
 * <b>If the target field does not exist, then it should be specified in a {@link Var} annotation.</b>
 */
@Target(ElementType.METHOD)
public @interface Getter {
    int DEFAULT_ACCESS = Integer.MIN_VALUE;

    /**
     * @return the desired access flags to add to or override for the target,<br>
     * which must be one or a combination of the modifiers in {@link Opcodes}.
     * <br><br>
     * This method exists mainly in order to allow {@code default} methods to be non-public.
     */
    int access() default DEFAULT_ACCESS;

    /**
     * @return whether the access flags returned by {@link #access} should override<br>
     * those in the target's declaration or be added to the existing flags.
     * <br><br>
     * <h3>{@code native} methods</h3>
     * If this method returns {@link AccessType#OVERRIDE} and {@link #access} returns {@link #DEFAULT_ACCESS},<br>
     * then its target's {@code native} modifier is stripped.
     */
    AccessType accessType() default AccessType.OVERRIDE;

    /**
     * @return the name of the field to get. If it does not exist,<br>
     * then it is generated and added to the owning type.
     */
    String value();
}
