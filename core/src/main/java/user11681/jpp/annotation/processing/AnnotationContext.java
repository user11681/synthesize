package user11681.jpp.annotation.processing;

import javax.lang.model.element.ElementKind;
import user11681.jpp.annotation.processing.json.JsonAnnotation;

public class AnnotationContext {
    public final String element;
    public final ElementKind kind;
    public final JsonAnnotation annotation;

    public AnnotationContext(final String element, final ElementKind kind,  final JsonAnnotation annotation) {
        this.element = element;
        this.kind = kind;
        this.annotation = annotation;
    }

    public <T> T get(final String key) {
        return (T) this.annotation.get(key);
    }
}
