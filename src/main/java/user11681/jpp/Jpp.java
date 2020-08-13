package user11681.jpp;

import net.devtech.grossfabrichacks.transformer.TransformerApi;
import net.fabricmc.loader.entrypoint.minecraft.hooks.EntrypointUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.tree.ClassNode;
import user11681.jpp.synthesis.Synthesizer;

public class Jpp {
    private static  final Logger LOGGER = LogManager.getLogger("jpp");

    public static void init() throws ClassNotFoundException {
        EntrypointUtils.invoke("jpp:pre", Runnable.class, Runnable::run);

        Class.forName("user11681.jpp.synthesis.Synthesizer");
        Class.forName("user11681.jpp.asm.ASMUtil");

        LOGGER.info("Initializing.");
        TransformerApi.registerPostMixinAsmClassTransformer((final String name, final ClassNode klass) -> Synthesizer.transform(klass));
        LOGGER.info("Done.");

        EntrypointUtils.invoke("jpp:post", Runnable.class, Runnable::run);
    }
}
