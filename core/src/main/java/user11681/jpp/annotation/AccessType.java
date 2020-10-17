package user11681.jpp.annotation;

/**
 * an enum for determining the mode of resolution of specified {@link Getter#access} and {@link Setter#access}.
 */
public enum AccessType {
    /**
     * merge the access flags specified in the annotation and already present in the method.
     */
    ADDITION,

    /**
     * override the access flags of the method with the specified access flags.
     */
    OVERRIDE
}
