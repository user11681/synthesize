package user11681.jpp.annotation;

/**
 * This enum contains the chaining options for use with {@linkplain Setter Setter}.
 */
public enum ChainType {
    /**
     * Never attempt to load {@code this} before returning.
     */
    NONE(false),

    /**
     * Load {@code this} only when the return type is not assignable from the field's type<br>
     * and when the target method is not defined.
     */
    AUTOMATIC(true),

    /**
     * Load {@code this} immediately before every return point.
     */
    FORCED(true);

    public final boolean enabled;

    ChainType(final boolean enabled) {
        this.enabled = enabled;
    }
}
