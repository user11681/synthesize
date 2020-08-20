package user11681.jpp.processor;

import com.google.common.collect.ImmutableSet;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Completion;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import user11681.jpp.api.Getter;
import user11681.jpp.api.Setter;

public class JppProcessor extends AbstractProcessor {
    private final File file = new File(JppProcessor.class.getProtectionDomain().getCodeSource().getLocation().getFile(), "test.ptxt");

    @Override
    public Set<String> getSupportedOptions() {
        return ImmutableSet.of("value");
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return ImmutableSet.of(Getter.class.getName(), Setter.class.getName());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public void init(final ProcessingEnvironment processingEnv) {
        try {
            new FileOutputStream(this.file).write("a test".getBytes());
        } catch (final IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    @Override
    public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
        return false;
    }

    @Override
    public Iterable<? extends Completion> getCompletions(final Element element, final AnnotationMirror annotation, final ExecutableElement member, final String userText) {
        return null;
    }
}
