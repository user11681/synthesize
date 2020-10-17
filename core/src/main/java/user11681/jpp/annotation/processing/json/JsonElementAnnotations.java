package user11681.jpp.annotation.processing.json;

import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import javax.lang.model.element.ElementKind;

public class JsonElementAnnotations {
    public final ElementKind kind;
    public final ReferenceArrayList<JsonAnnotation> annotations = ReferenceArrayList.wrap(new JsonAnnotation[1], 0);

    public JsonElementAnnotations(final ElementKind kind) {
        this.kind = kind;
    }
}
