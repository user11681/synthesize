package test;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import user11681.jpp.annotation.Var;
import user11681.jpp.annotation.Getter;
import user11681.jpp.annotation.Initializer;
import user11681.jpp.annotation.Setter;

@Var(name = "energy", descriptor = "J")
@Var(name = "width", descriptor = "I")
public interface StatefulInterface {
    Logger LOGGER = LogManager.getLogger("StatefulInterface");

    int thing = 0;

    @Initializer
    default boolean noParamCtor0() {
        LOGGER.info("Now you can name your constructors.");
        LOGGER.info("descriptor: ()Z");
        LOGGER.info("Yes, they can return values too.");

        return true;
    }

    @Initializer
    default boolean noParamCtor1() {
        LOGGER.info("2nd ()Z constructor");

        return true;
    }

    @Getter("energy")
    default long energy() {
        return 123;
    }

    @Setter("energy")
    default void set(final long energy) {}

    @Getter("width")
    default int width() {
        return 123;
    }

    @Setter("width")
    default void setWidth(final int width) {}
}
