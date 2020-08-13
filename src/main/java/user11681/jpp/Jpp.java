package user11681.jpp;

import net.devtech.grossfabrichacks.transformer.TransformerApi;
import net.fabricmc.loader.entrypoint.minecraft.hooks.EntrypointUtils;
import org.objectweb.asm.tree.ClassNode;
import user11681.jpp.synthesis.Synthesizer;

public class Jpp {
    public static void init() throws ClassNotFoundException {
        Class.forName("user11681.jpp.synthesis.Synthesizer");
        Class.forName("user11681.jpp.asm.ASMUtil");

        TransformerApi.registerPostMixinAsmClassTransformer((final String name, final ClassNode klass) -> Synthesizer.transformNew(klass));

        EntrypointUtils.invoke("jpp", Runnable.class, Runnable::run);
    }
}
