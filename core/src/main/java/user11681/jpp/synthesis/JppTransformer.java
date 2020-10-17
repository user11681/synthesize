package user11681.jpp.synthesis;

import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import java.lang.annotation.Annotation;
import java.util.List;
import net.devtech.grossfabrichacks.entrypoints.PrePrePreLaunch;
import net.devtech.grossfabrichacks.transformer.TransformerApi;
import net.devtech.grossfabrichacks.transformer.asm.AsmClassTransformer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.launch.knot.UnsafeKnotClassLoader;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;
import user11681.dynamicentry.DynamicEntry;
import user11681.jpp.annotation.AccessType;
import user11681.jpp.annotation.ChainType;
import user11681.jpp.annotation.Entrypoint;
import user11681.jpp.annotation.Getter;
import user11681.jpp.annotation.ModInterface;
import user11681.jpp.annotation.Setter;
import user11681.jpp.annotation.Var;
import user11681.jpp.annotation.processing.AnnotationContext;
import user11681.jpp.annotation.processing.json.AnnotationHierarchy;
import user11681.reflect.Classes;
import user11681.shortcode.Shortcode;
import user11681.shortcode.instruction.DelegatingInsnList;

public class JppTransformer extends Shortcode implements PrePrePreLaunch, AsmClassTransformer {
    public static final FabricLoader fabric = FabricLoader.getInstance();

    private static final Object2ReferenceOpenHashMap<String, ReferenceArrayList<String>> superfaces = new Object2ReferenceOpenHashMap<>();
    private static final Object2ReferenceOpenHashMap<String, ReferenceArrayList<FieldNode>> inheritableFields = new Object2ReferenceOpenHashMap<>();
    private static final Object2ReferenceOpenHashMap<String, ReferenceArrayList<AccessorInfo>> inheritableMethods = new Object2ReferenceOpenHashMap<>();
    //    private static final Object2ReferenceOpenHashMap<String, ObjectOpenHashSet<String>> fieldInheritors = new Object2ReferenceOpenHashMap<>();
//    private static final Object2ReferenceOpenHashMap<String, ObjectOpenHashSet<String>> methodInheritors = new Object2ReferenceOpenHashMap<>();
    private static final Object2ReferenceOpenHashMap<String, ObjectOpenHashSet<String>> inheritors = new Object2ReferenceOpenHashMap<>();
    private static final AnnotationHierarchy annotations = AnnotationHierarchy.read().process();

    @Override
    public void onPrePrePreLaunch() {
        DynamicEntry.maybeExecute("jpp:preinit", Runnable.class, Runnable::run);

        for (final AnnotationContext annotation : annotations.annotationTypes.get(Entrypoint.class.getName())) {
            final String modID = annotation.get("id");
            final String languageAdapter = annotation.get("adapter");

            for (final String entrypoint : (List<String>) annotation.get("value")) {
                MetadataGenerator.addEntrypoint(modID, entrypoint, languageAdapter, annotation.element.replaceFirst("\\(.*", ""));
            }
        }

        TransformerApi.registerPostMixinAsmClassTransformer(this);
        DynamicEntry.maybeExecute("jpp:postinit", Runnable.class, Runnable::run);
    }

    @Override
    public void transform(final ClassNode klass) {
        processFieldDeclarations(klass);
        processMethods(klass);
        inherit(klass);
        applyModInterfaces(klass);
    }

    private static void processFieldDeclarations(final ClassNode klass) {
        if (klass.invisibleAnnotations != null) {
            final List<AnnotationNode> annotations = getRepeatableAnnotations(klass.invisibleAnnotations, Var.class, Var.Vars.class);
            final int annotationCount = annotations.size();

            if (hasFlag(klass.access, ACC_INTERFACE)) {
                final FieldNode[] fieldNodes = new FieldNode[annotationCount];

                for (int j = 0; j < annotationCount; j++) {
                    fieldNodes[j] = getField(klass, annotations.get(j));
                }

                superfaces.put(klass.name, ReferenceArrayList.wrap(new String[]{klass.name}));
                inheritableFields.put(klass.name, ReferenceArrayList.wrap(fieldNodes));
//                fieldInheritors.put(klass.name, new ObjectOpenHashSet<>());
                initializeInheritorSet(klass.name);
            } else {
                for (int j = 0; j < annotationCount; j++) {
                    klass.fields.add(getField(klass, annotations.get(j)));
                }
            }
        }
    }

    private static void processMethods(final ClassNode klass) {
        for (final MethodNode method : klass.methods.toArray(new MethodNode[0])) {
            if (method.invisibleAnnotations != null) {
                for (final AnnotationNode annotation : method.invisibleAnnotations) {
                    if (getDescriptor(Getter.class).equals(annotation.desc)) {
                        getter(klass, method, annotation);

                        if (hasFlag(klass.access, ACC_INTERFACE) && !superfaces.containsKey(klass.name)) {
                            superfaces.put(klass.name, ReferenceArrayList.wrap(new String[]{klass.name}));
//                            methodInheritors.put(klass.name, new ObjectOpenHashSet<>());
                            initializeInheritorSet(klass.name);
                        }
                    }

                    if (getDescriptor(Setter.class).equals(annotation.desc)) {
                        setter(klass, method, annotation);

                        if (hasFlag(klass.access, ACC_INTERFACE) && !superfaces.containsKey(klass.name)) {
                            superfaces.put(klass.name, ReferenceArrayList.wrap(new String[]{klass.name}));
                            initializeInheritorSet(klass.name);
                        }
                    }
                }
            }
        }
    }

    private static void inherit(final ClassNode klass) {
        final boolean isInterface = hasFlag(klass.access, ACC_INTERFACE);

        Classes.load(UnsafeKnotClassLoader.instance, false, toBinaryName(klass.superName));

        for (final String interfase : klass.interfaces) {
            Classes.load(UnsafeKnotClassLoader.instance, false, toBinaryName(interfase));

            if (isInterface) {
                if (inheritableFields.containsKey(interfase)) {
                    final ReferenceArrayList<FieldNode> fields = inheritableFields.get(klass.name);

                    if (fields == null) {
                        inheritableFields.put(klass.name, ReferenceArrayList.wrap(inheritableFields.get(interfase).elements()));
                    } else {
                        fields.addAll(inheritableFields.get(interfase));
                    }
                }

                if (inheritableMethods.containsKey(interfase)) {
                    final ReferenceArrayList<AccessorInfo> accessors = inheritableMethods.get(klass.name);

                    if (accessors == null) {
                        inheritableMethods.put(klass.name, ReferenceArrayList.wrap(inheritableMethods.get(interfase).elements()));
                    } else {
                        accessors.addAll(inheritableMethods.get(interfase));
                    }
                }
            } else {
                if (inheritableFields.containsKey(interfase)) {
                    final ObjectOpenHashSet<String> implementors = inheritors.get(interfase);
                    final ObjectOpenHashSet<String> fieldNames = new ObjectOpenHashSet<>();

                    for (final FieldNode field : klass.fields) {
                        fieldNames.add(field.name);
                    }

                    if (!implementors.contains(klass.superName)) {
                        for (final FieldNode field : inheritableFields.get(interfase).elements()) {
                            if (!fieldNames.contains(field.name)) {
                                klass.fields.add(field);
                            }
                        }
                    }

                    implementors.add(klass.name);
                }

                if (inheritableMethods.containsKey(interfase)) {
                    final ObjectOpenHashSet<String> implementors = inheritors.get(interfase);
                    final Object2ReferenceOpenHashMap<String, MethodNode> methods = new Object2ReferenceOpenHashMap<>();

                    for (final MethodNode method : klass.methods) {
                        methods.put(method.name, method);
                    }

                    if (!implementors.contains(klass.superName)) {
                        for (final AccessorInfo accessor : inheritableMethods.get(interfase)) {
                            final MethodNode method = methods.get(accessor.name);

                            if (method == null) {
                                klass.methods.add(accessor.toNode(klass.name));
                            } else if (hasFlag(method.access, ACC_NATIVE)) {
                                accessor.accept(methods.get(accessor.name), klass.name);
                            }
                        }
                    }

                    implementors.add(klass.name);
                }
            }
        }
    }

    private static void applyModInterfaces(final ClassNode klass) {
        if (klass.invisibleAnnotations != null) {
            final ObjectOpenHashSet<String> additions = new ObjectOpenHashSet<>(1);
            final ObjectOpenHashSet<String> removals = new ObjectOpenHashSet<>(1);
            final List<String> interfaces = klass.interfaces;
            final List<AnnotationNode> annotations = getRepeatableAnnotations(klass.invisibleAnnotations, ModInterface.class, ModInterface.ModInterfaces.class);

            if (!annotations.isEmpty()) {
                annotations:
                for (final AnnotationNode annotation : annotations) {
                    final List<String> ids = getAnnotationValue(annotation, "id");
                    final List<String> types = getAnnotationValue(annotation, "type");

                    for (final String id : ids) {
                        if (!fabric.isModLoaded(id)) {
                            for (final String type : types) {
                                removals.add(toInternalName(type));
                            }

                            continue annotations;
                        }
                    }

                    for (final String type : types) {
                        additions.add(toInternalName(type));
                    }
                }

                for (final String removal : removals) {
                    if (!additions.contains(removal)) {
                        interfaces.remove(removal);
                    }
                }

                for (final String addition : additions) {
                    if (!interfaces.contains(addition)) {
                        interfaces.add(addition);
                    }
                }
            }
        }
    }

    private static void addEntrypoints(final ClassNode klass) {
        if (klass.invisibleAnnotations != null) {

            for (final AnnotationNode annotation : getRepeatableAnnotations(klass.invisibleAnnotations, Entrypoint.class, Entrypoint.Entrypoints.class)) {
                final String id = getAnnotationValue(annotation, "id", Entrypoint.DEFAULT_ID);
                final String adapter = getAnnotationValue(annotation, "adapter", Entrypoint.DEFAULT_ADAPTER);

                for (final String entrypoint : (List<String>) getAnnotationValue(annotation, "entrypoints")) {
                    MetadataGenerator.addEntrypoint(id, entrypoint, adapter, toBinaryName(klass.name));
                }
            }
        }

        for (final FieldNode field : klass.fields) {
            if (field.invisibleAnnotations != null && hasFlag(field.access, ACC_STATIC)) {
                for (final AnnotationNode annotation : getRepeatableAnnotations(field.invisibleAnnotations, Entrypoint.class, Entrypoint.Entrypoints.class)) {
                    final String id = getAnnotationValue(annotation, "id", Entrypoint.DEFAULT_ID);
                    final String adapter = getAnnotationValue(annotation, "adapter", Entrypoint.DEFAULT_ADAPTER);

                    for (final String entrypoint : (List<String>) getAnnotationValue(annotation, "entrypoints")) {
                        MetadataGenerator.addEntrypoint(id, entrypoint, adapter, toBinaryName(klass.name) + "::" + field.name);
                    }
                }
            }
        }

        for (final MethodNode method : klass.methods) {
            if (method.invisibleAnnotations != null) {
                for (final AnnotationNode annotation : getRepeatableAnnotations(method.invisibleAnnotations, Entrypoint.class, Entrypoint.Entrypoints.class)) {
                    final String id = getAnnotationValue(annotation, "id", Entrypoint.DEFAULT_ID);
                    final String adapter = getAnnotationValue(annotation, "adapter", Entrypoint.DEFAULT_ADAPTER);

                    for (final String entrypoint : (List<String>) getAnnotationValue(annotation, "entrypoints")) {
                        MetadataGenerator.addEntrypoint(id, entrypoint, adapter, toBinaryName(klass.name) + "::" + method.name);
                    }
                }
            }
        }
    }

    private static void initializeInheritorSet(final String klass) {
        if (!inheritors.containsKey(klass)) {
            inheritors.put(klass, new ObjectOpenHashSet<>());
        }
    }

    private static FieldNode getField(final ClassNode klass, final AnnotationNode annotation) {
        final String name = getAnnotationValue(annotation, "name");
        final FieldNode[] fields = klass.fields.toArray(new FieldNode[0]);
        final int fieldCount = fields.length;

        for (int i = 0; i < fieldCount; i++) {
            if (fields[i].name.equals(name)) {
                throw new RuntimeException(String.format("field %s already exists in %s.", name, klass.name));
            }
        }

        return new FieldNode(getAnnotationValue(annotation, "access", Var.DEFAULT_ACCESS), name, toDescriptor(getAnnotationValue(annotation, "descriptor")), null, null);
    }

    private static void getter(final ClassNode klass, final MethodNode method, final AnnotationNode annotation) {
        final int access = setAndGetAccess(method, annotation);
        final String fieldName = getAnnotationValue(annotation, "value");
        final String fieldDescriptor = getReturnType(method.desc);

        if (hasFlag(method.access, ACC_NATIVE)) {
            method.visitVarInsn(ALOAD, 0);
            method.visitFieldInsn(GETFIELD, klass.name, fieldName, fieldDescriptor);
            method.visitInsn(getReturnOpcode(fieldDescriptor));

            method.access = access;
        } else if (!hasFlag(klass.access, ACC_INTERFACE)) {
            final DelegatingInsnList instructions = new DelegatingInsnList();

            instructions.addVarInsn(ALOAD, 0);
            instructions.addFieldInsn(GETFIELD, klass.name, fieldName, fieldDescriptor);
            instructions.addInsn(getReturnOpcode(fieldDescriptor));

            insertBeforeEveryReturn(method, instructions);

            method.access = access;
        } else {
            final ReferenceArrayList<AccessorInfo> accessors = inheritableMethods.get(klass.name);
            final GetterInfo info = new GetterInfo(method);
            info.access = access;
            info.fieldName = fieldName;
            info.fieldDescriptor = fieldDescriptor;

            if (accessors == null) {
                inheritableMethods.put(klass.name, ReferenceArrayList.wrap(new AccessorInfo[]{info}));
            } else {
                accessors.add(info);
            }
        }
    }

    private static void setter(final ClassNode klass, MethodNode method, final AnnotationNode annotation) {
        final ReferenceArrayList<String> descriptor = parseDescriptor(method);
        final String fieldName = getAnnotationValue(annotation, "value");
        final String fieldDescriptor = descriptor.get(0);
        final int access = setAndGetAccess(method, annotation);
        final String returnType = descriptor.top();
        final int returnOpcode = getReturnOpcode(returnType);
        final ChainType chainType = getChainType(klass, annotation, returnType, fieldDescriptor);

        if (hasFlag(method.access, ACC_NATIVE)) {
            method.visitVarInsn(ALOAD, 0);
            method.visitVarInsn(getLoadOpcode(fieldDescriptor), 1);
            method.visitFieldInsn(PUTFIELD, klass.name, fieldName, fieldDescriptor);

            if (chainType.enabled) {
                method.visitVarInsn(ALOAD, 0);
            }

            method.visitInsn(returnOpcode);

            method.access = access;
        } else if (!hasFlag(klass.access, ACC_INTERFACE)) {
            final DelegatingInsnList instructions = new DelegatingInsnList();

            instructions.addVarInsn(ALOAD, 0);
            instructions.addVarInsn(getLoadOpcode(fieldDescriptor), 1);
            instructions.addFieldInsn(PUTFIELD, klass.name, fieldName, fieldDescriptor);

            if (chainType == ChainType.FORCED) {
                instructions.addVarInsn(ALOAD, 0);
            }

            insertBeforeEveryReturn(method, instructions);

            method.access = access;
        } else {
            final ReferenceArrayList<AccessorInfo> accessors = inheritableMethods.get(klass.name);
            final SetterInfo info = new SetterInfo(method, chainType);
            info.access = access;
            info.fieldName = fieldName;
            info.fieldDescriptor = fieldDescriptor;

            if (accessors == null) {
                inheritableMethods.put(klass.name, ReferenceArrayList.wrap(new AccessorInfo[]{info}));
            } else {
                accessors.add(info);
            }
        }
    }

    private static int setAndGetAccess(final MethodNode method, final AnnotationNode annotation) {
        new Var.Vars() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return null;
            }

            @Override
            public Var[] value() {
                return new Var[0];
            }
        };

        final int access = getAnnotationValue(annotation, "access", Setter.DEFAULT_ACCESS);
        final int newAccess;

        if (access == Getter.DEFAULT_ACCESS) {
            newAccess = method.access & ~ACC_NATIVE;
        } else if (getAnnotationValue(annotation, "accessType", AccessType.OVERRIDE) == AccessType.OVERRIDE) {
            newAccess = access;
        } else {
            newAccess = method.access | access;
        }

        return newAccess;
    }

    private static ChainType getChainType(final ClassNode klass, final AnnotationNode setterAnnotation, final String returnType, final String fieldDescriptor) {
        try {
            final String returnTypeName = toInternalName(returnType);
            final ChainType chainType = getAnnotationValue(setterAnnotation, "chainType", ChainType.AUTOMATIC);
            boolean chainable = false;
            Class<?> superclass = Class.forName(toBinaryName(klass.superName));
            String superclassName;

            while (superclass != null) {
                superclassName = toInternalName(superclass.getName());

                if (fieldDescriptor.equals(superclassName)) {
                    return ChainType.NONE;
                }

                if (returnTypeName.equals(superclassName)) {
                    chainable = true;
                }

                superclass = superclass.getSuperclass();
            }

            return chainable || chainType == ChainType.FORCED ? chainType : ChainType.NONE;
        } catch (final ClassNotFoundException exception) {
            throw new RuntimeException(exception);
        }
    }
}
