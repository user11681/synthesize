package user11681.jpp.annotation.processing;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.Object2ReferenceMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import java.lang.annotation.Annotation;
import java.lang.annotation.Repeatable;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import javax.annotation.processing.Completion;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.StandardLocation;
import net.gudenau.lib.unsafe.Unsafe;
import org.spongepowered.tools.obfuscation.mirror.TypeUtils;
import user11681.jpp.annotation.Entrypoint;
import user11681.jpp.annotation.Var;
import user11681.jpp.annotation.processing.json.AnnotationHierarchy;
import user11681.jpp.annotation.processing.json.JsonAnnotation;
import user11681.jpp.annotation.processing.json.JsonAnnotationType;
import user11681.jpp.annotation.processing.json.JsonElementAnnotations;
import user11681.reflect.Classes;

public class JppProcessor implements Processor {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final AnnotationHierarchy annotations = new AnnotationHierarchy();
    private static final ObjectOpenHashSet<String> processedTypes = new ObjectOpenHashSet<>();
    private static final ReferenceArrayList<Class<Annotation>> supportedAnnotationTypes = Classes.cast(ReferenceArrayList.wrap(new Class<?>[]{
        Entrypoint.class,
        Entrypoint.Entrypoints.class,
        Var.class,
        Var.Vars.class
    }));
    private static final ObjectOpenHashSet<String> supportedAnnotationTypeNames = new ObjectOpenHashSet<>();
    private static final Reference2ReferenceOpenHashMap<Class<Annotation>, String> atomicTypeNames = new Reference2ReferenceOpenHashMap<>();

    static {
        for (final Class<Annotation> type : supportedAnnotationTypes) {
            supportedAnnotationTypeNames.add(type.getName().replace('$', '.'));

            try {
                final Method valueMethod = type.getDeclaredMethod("value");
                final Class<?> componentType = valueMethod.getReturnType().getComponentType();

                if (componentType != null && Annotation.class.isAssignableFrom(componentType)) {
                    final Repeatable repeatable = componentType.getAnnotation(Repeatable.class);

                    if (repeatable != null && repeatable.value() == type) {
                        atomicTypeNames.put(type, componentType.getName());

                        continue;
                    }
                }
            } catch (final NoSuchMethodException ignored) {}

            annotations.put(type.getName(), new JsonAnnotationType());
        }
    }

    private Messager messager;
    private Filer filer;
    private Types types;
    private Elements elements;

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return supportedAnnotationTypeNames;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

    @Override
    public Set<String> getSupportedOptions() {
        return Collections.EMPTY_SET;
    }

    @Override
    public Iterable<? extends Completion> getCompletions(final Element element, final AnnotationMirror annotation, final ExecutableElement member, final String userText) {
        return null;
    }

    @Override
    public void init(final ProcessingEnvironment environment) {
        this.messager = environment.getMessager();
        this.types = environment.getTypeUtils();
        this.elements = environment.getElementUtils();
        this.filer = environment.getFiler();

        this.printf("jpp™ annotation processor v0");
    }

    @Override
    public boolean process(final Set<? extends TypeElement> annotationTypes, final RoundEnvironment environment) {
        if (environment.processingOver()) {
            try {
                final JsonObject serialized = new JsonObject();

                for (final Object2ReferenceMap.Entry<String, JsonAnnotationType> entry : annotations.object2ReferenceEntrySet()) {
                    serialized.add(entry.getKey(), gson.toJsonTree(entry.getValue()));
                }

                this.filer.createResource(StandardLocation.CLASS_OUTPUT, "", "jpp-annotations.json").openOutputStream().write(gson.toJson(serialized).getBytes());
            } catch (final Throwable exception) {
                throw Unsafe.throwException(exception);
            }
        } else {
            for (final Class<Annotation> annotationType : supportedAnnotationTypes) {
                final String annotationTypeName = annotationType.getName();

                this.printf("Processing %s annotations.", annotationTypeName);

                for (final Element element : environment.getElementsAnnotatedWith(annotationType)) {
                    if (processedTypes.add(String.format("%s@%s", element.toString(), annotationTypeName))) {
                        final ElementKind kind = element.getKind();
                        final String name;

                        if (kind.isClass() || kind.isInterface()) {
                            name = TypeUtils.getTypeName(element.asType());
                        } else if (kind.isField()) {
                            name = String.format("%s::%s", TypeUtils.getTypeName(element.getEnclosingElement().asType()), element.toString());
                        } else {
                            name = String.format("%s::%s%s", TypeUtils.getTypeName(element.getEnclosingElement().asType()), element.getSimpleName().toString(), TypeUtils.getDescriptor(element));
                        }

                        this.printf("\t%s", name);

                        final String repeatableType = atomicTypeNames.get(annotationType);

                        annotations.get(repeatableType == null ? annotationTypeName : repeatableType).computeIfAbsent(name, (final String typeName) -> new JsonElementAnnotations(kind)).annotations.addAll(this.flatSerialize(element.getAnnotation(annotationType)));
                    }
                }
            }

            for (final Element element : environment.getElementsAnnotatedWith(Entrypoint.class)) {
                if (processedTypes.add(String.format("%s@%s", element.toString(), Entrypoint.class.getName()))) {

                }
            }
        }

        return false;
    }

    private ReferenceArrayList<JsonAnnotation> flatSerialize(final Annotation annotation) {
        final Class<? extends Annotation> annotationType = annotation.annotationType();

        try {
            if (atomicTypeNames.containsKey(annotationType)) {
                final ReferenceArrayList<JsonAnnotation> annotations = ReferenceArrayList.wrap(new JsonAnnotation[2], 0);

                for (final Annotation contained : (Annotation[]) annotationType.getDeclaredMethod("value").invoke(annotation)) {
                    annotations.add(this.serialize(contained));
                }

                return annotations;
            }

            return ReferenceArrayList.wrap(new JsonAnnotation[]{this.serialize(annotation)});
        } catch (final Throwable throwable) {
            throw Unsafe.throwException(throwable);
        }
    }

    private JsonAnnotation serialize(final Annotation annotation) {
        try {
            final JsonAnnotation jsonAnnotation = new JsonAnnotation();

            for (final Method method : annotation.annotationType().getDeclaredMethods()) {
                jsonAnnotation.put(method.getName(), method.invoke(annotation));
            }

            return jsonAnnotation;
        } catch (final Throwable throwable) {
            throw Unsafe.throwException(throwable);
        }
    }

    private void printf(final String format, final Object... arguments) {
        this.print(String.format(format, arguments));
    }

    private void print(final Object... objects) {
        final String string = Arrays.toString(objects);

        this.print(string.substring(1, string.length() - 1).replace(",", ""));
    }

    private void print(final String text) {
        this.messager.printMessage(Diagnostic.Kind.OTHER, text);
    }
}
