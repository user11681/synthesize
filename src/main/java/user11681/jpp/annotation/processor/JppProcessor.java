package user11681.jpp.annotation.processor;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Set;
import javax.annotation.processing.Completion;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import net.gudenau.lib.unsafe.Unsafe;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import user11681.jpp.annotation.Entrypoint;
import user11681.jpp.annotation.Getter;
import user11681.jpp.annotation.Setter;
import user11681.jpp.annotation.Var;

public class JppProcessor implements Processor {
    private ProcessingEnvironment environment;

    @Override
    public Set<String> getSupportedOptions() {
        return Collections.EMPTY_SET;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return new ObjectOpenHashSet<>(new String[]{
            Entrypoint.class.getName(),
            Entrypoint.Entrypoints.class.getName(),
            Getter.class.getName(),
            Setter.class.getName(),
            Var.class.getName(),
            Var.Vars.class.getName()
        });
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

    @Override
    public void init(final ProcessingEnvironment environment) {
        this.environment = environment;

        environment.getMessager().printMessage(Diagnostic.Kind.OTHER, "jpp™ annotation processor v0");
        System.out.println("test");

        try {
            final Filer filer = this.environment.getFiler();
            final JavaFileObject file = filer.createClassFile("user11681.jpp.annotation.processor.Jppaptest", (Element[]) null);
            final OutputStream stream = file.openOutputStream();
            final ClassNode klass = new ClassNode();
            final ClassWriter cw = new ClassWriter(2);

            klass.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC + Opcodes.ACC_SUPER, "user11681/jpp/annotation/processor/Jppaptest", null, "java/lang/Object", null);
            klass.accept(cw);

            stream.write(cw.toByteArray());

            throw new Throwable();
        } catch (final Throwable throwable) {
            Unsafe.throwException(throwable);
        }
    }

    @Override
    public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment environment) {
        for (final TypeElement element : annotations) {
            System.out.println(element.asType());
        }

        return false;
    }

    @Override
    public Iterable<? extends Completion> getCompletions(final Element element, final AnnotationMirror annotation, final ExecutableElement member, final String userText) {
        return null;
    }
}
