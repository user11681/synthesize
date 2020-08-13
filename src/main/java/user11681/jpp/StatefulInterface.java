package user11681.jpp;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import user11681.jpp.synthesis.Declare;
import user11681.jpp.synthesis.Getter;
import user11681.jpp.synthesis.Initializer;
import user11681.jpp.synthesis.Setter;

@Declare(name = "energy", descriptor = "J")
@Declare(name = "width", descriptor = "I")
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
