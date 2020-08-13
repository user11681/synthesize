package user11681.jpp;

import org.objectweb.asm.tree.ClassNode;
import user11681.jpp.synthesis.Synthesizer;

public class Jpp {
    public static void init() {
        TransformerApi.registerPostMixinAsmClassTransformer((final String name, final ClassNode klass) -> Synthesizer.transformNew(klass));
    }
}
