package user11681.jpp.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Target;

/**
 * This annotation type should be used for adding interfaces to or removing interfaces from classes based on mod presence.
 */
@Target(ElementType.TYPE)
@Repeatable(ModInterface.ModInterfaces.class)
public @interface ModInterface {
    /**
     * @return the IDs of the mods whose presence is required for the specified interfaces to be applied or remain present.<br><br>
     *
     * If any of the mods whose ID is specified is not found, then the interface is stripped unless another <b>{@code ModInterface}</b><br>
     * annotation is found and all of the specified mods are found.
     */
    String[] id();

    /**
     * @return the names of the interfaces to apply to the annotated type in any valid format (binary or internal or as a descriptor).
     */
    String[] type();

    @Target(ElementType.TYPE)
    @interface ModInterfaces {
        ModInterface[] value();
    }
}
