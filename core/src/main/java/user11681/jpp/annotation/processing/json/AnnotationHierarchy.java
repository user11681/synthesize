package user11681.jpp.annotation.processing.json;

import com.google.gson.Gson;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import java.net.URL;
import java.util.Enumeration;
import javax.lang.model.element.ElementKind;
import net.fabricmc.loader.launch.knot.UnsafeKnotClassLoader;
import net.gudenau.lib.unsafe.Unsafe;
import org.apache.commons.io.IOUtils;
import user11681.jpp.annotation.processing.AnnotationContext;

public class AnnotationHierarchy extends Object2ReferenceOpenHashMap<String, JsonAnnotationType> {
    public Object2ReferenceOpenHashMap<String, ReferenceArrayList<AnnotationContext>> annotationTypes;

    public AnnotationHierarchy process() {
        this.annotationTypes = new Object2ReferenceOpenHashMap<>();

        for (final Entry<String, JsonAnnotationType> annotationType : this.object2ReferenceEntrySet()) {
            final ReferenceArrayList<AnnotationContext> annotations = new ReferenceArrayList<>();

            this.annotationTypes.put(annotationType.getKey(), annotations);

            for (final Entry<String, JsonElementAnnotations> element : annotationType.getValue().object2ReferenceEntrySet()) {
                final String elementName = element.getKey();
                final JsonElementAnnotations elementAnnotations = element.getValue();
                final ElementKind kind = elementAnnotations.kind;

                for (final JsonAnnotation annotation : elementAnnotations.annotations) {
                    annotations.add(new AnnotationContext(elementName, kind, annotation));
                }
            }
        }

        return this;
    }

    public static AnnotationHierarchy read() {
        try {
            final Gson gson = new Gson();
            final AnnotationHierarchy annotations = new AnnotationHierarchy();
            final Enumeration<URL> annotationFiles = UnsafeKnotClassLoader.instance.getResources("jpp-annotations.json");

            while (annotationFiles.hasMoreElements()) {
                final AnnotationHierarchy deserialized = gson.fromJson(new String(IOUtils.toByteArray(annotationFiles.nextElement())), AnnotationHierarchy.class);

                for (final Entry<String, JsonAnnotationType> entry : deserialized.object2ReferenceEntrySet()) {
                    annotations.computeIfAbsent(entry.getKey(), (final String key) -> new JsonAnnotationType()).putAll(entry.getValue());
                }
            }

            return annotations;
        } catch (final Throwable exception) {
            throw Unsafe.throwException(exception);
        }
    }
}
