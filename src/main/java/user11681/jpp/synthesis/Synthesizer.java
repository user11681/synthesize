package user11681.jpp.synthesis;

import com.sun.org.apache.bcel.internal.classfile.ClassFormatException;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import java.util.ArrayList;
import java.util.List;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import user11681.jpp.api.AccessType;
import user11681.jpp.api.Declare;
import user11681.jpp.api.Getter;
import user11681.jpp.api.Initializer;
import user11681.jpp.api.Inline;
import user11681.jpp.api.Setter;
import user11681.jpp.asm.ASMUtil;
import user11681.jpp.asm.DelegatingInsnList;

public class Synthesizer {
    private static final Object2ReferenceOpenHashMap<String, ReferenceArrayList<String>> superfaces = new Object2ReferenceOpenHashMap<>();
    private static final Object2ReferenceOpenHashMap<String, ReferenceArrayList<FieldNode>> inheritableFields = new Object2ReferenceOpenHashMap<>();
    private static final Object2ReferenceOpenHashMap<String, ReferenceArrayList<AccessorInfo>> inheritableMethods = new Object2ReferenceOpenHashMap<>();
    private static final Object2ReferenceOpenHashMap<String, ObjectOpenHashSet<String>> fieldInheritors = new Object2ReferenceOpenHashMap<>();
    private static final Object2ReferenceOpenHashMap<String, ObjectOpenHashSet<String>> methodInheritors = new Object2ReferenceOpenHashMap<>();
    private static final Object2ReferenceOpenHashMap<String, MethodNode[]> initializerHolders = new Object2ReferenceOpenHashMap<>();
    private static final Object2ReferenceOpenHashMap<String, MethodNode> inlineMethods = new Object2ReferenceOpenHashMap<>();

    private static final ClassLoader LOADER = Thread.currentThread().getContextClassLoader();

    public static void transform(final ClassNode klass) {
        AnnotationNode[] annotations;
        AnnotationNode annotation;
        int annotationCount;
        int i;
        int j;

        if (klass.invisibleAnnotations != null) {
            annotations = klass.invisibleAnnotations.toArray(new AnnotationNode[0]);
            annotationCount = annotations.length;

            for (i = 0; i < annotationCount; i++) {
                annotation = annotations[i];

                if (Type.getDescriptor(Declare.Fields.class).equals(annotation.desc)) {
                    final AnnotationNode[] declarations = ((ArrayList<AnnotationNode>) ASMUtil.getAnnotationValue(annotation, "value")).toArray(new AnnotationNode[0]);
                    final int declarationCount = declarations.length;

                    if ((klass.access & Opcodes.ACC_INTERFACE) == 0) {
                        for (j = 0; j < declarationCount; j++) {
                            klass.fields.add(getField(klass, declarations[j]));
                        }
                    } else {
                        final FieldNode[] fieldNodes = new FieldNode[declarationCount];

                        for (j = 0; j < declarationCount; j++) {
                            fieldNodes[j] = getField(klass, declarations[j]);
                        }

                        superfaces.put(klass.name, ReferenceArrayList.wrap(new String[]{klass.name}));
                        inheritableFields.put(klass.name, ReferenceArrayList.wrap(fieldNodes));
                        fieldInheritors.put(klass.name, new ObjectOpenHashSet<>());
                    }

                    break;
                } else if (Type.getDescriptor(Declare.class).equals(annotation.desc)) {
                    if ((klass.access & Opcodes.ACC_INTERFACE) == 0) {
                        klass.fields.add(getField(klass, annotation));
                    } else {
                        superfaces.put(klass.name, ReferenceArrayList.wrap(new String[]{klass.name}));
                        inheritableFields.put(klass.name, ReferenceArrayList.wrap(new FieldNode[]{getField(klass, annotation)}));
                        fieldInheritors.put(klass.name, new ObjectOpenHashSet<>());
                    }

                    break;
                }
            }
        }

        final MethodNode[] methods = klass.methods.toArray(new MethodNode[0]);
        final int methodCount = methods.length;
        MethodNode method;

        for (i = 0; i < methodCount; i++) {
            method = methods[i];

            if (method.invisibleAnnotations != null) {
                annotations = method.invisibleAnnotations.toArray(new AnnotationNode[0]);
                annotationCount = annotations.length;

                for (j = 0; j < annotationCount; j++) {
                    annotation = annotations[j];

                    if (Type.getDescriptor(Getter.class).equals(annotation.desc)) {
                        getter(klass, method, annotation);

                        if ((klass.access & Opcodes.ACC_INTERFACE) != 0 && !superfaces.containsKey(klass.name)) {
                            superfaces.put(klass.name, ReferenceArrayList.wrap(new String[]{klass.name}));
                            methodInheritors.put(klass.name, new ObjectOpenHashSet<>());
                        }
                    }

                    if (Type.getDescriptor(Setter.class).equals(annotation.desc)) {
                        setter(klass, method, annotation);

                        if ((klass.access & Opcodes.ACC_INTERFACE) != 0 && !superfaces.containsKey(klass.name)) {
                            superfaces.put(klass.name, ReferenceArrayList.wrap(new String[]{klass.name}));
                            methodInheritors.put(klass.name, new ObjectOpenHashSet<>());
                        }
                    }

                    if (Type.getDescriptor(Inline.class).equals(annotation.desc)) {
                        inlineMethods.put(klass.name + method.name + method.desc, method);
//                    } else if (Type.getDescriptor(Initializer.class).equals(annotation.desc)) {
//                        if (ASMUtil.getAnnotationValue(annotation, "type", Initializer.Type.CONSTRUCTOR) == Initializer.Type.CONSTRUCTOR) {
//                            constructors.add(method);
//                        } else {
//                            initializers.add(method);
//                        }
                    }
                }
            }
        }

        try {
            Class.forName(ASMUtil.toBinaryName(klass.superName), false, LOADER);
        } catch (final ClassNotFoundException exception) {
            throw new RuntimeException(exception);
        }

        final List<String> supertypeList = klass.interfaces;
        final int supertypeCount = supertypeList.size() + 1;
        final String[] supertypes = supertypeList.toArray(new String[supertypeCount]);
        supertypes[supertypeCount - 1] = klass.superName;
        String supertype;
        FieldNode[] fields;
        FieldNode field;
        String[] superfaceArray;
        AccessorInfo[] accessors;
        AccessorInfo accessor;
        int fieldCount;
        int superfaceCount;
        int accessorCount;
        int k;
        ReferenceArrayList<AccessorInfo> superaccessorList;
        ReferenceArrayList<AccessorInfo> accessorList;
        ReferenceArrayList<String> superfaceList;
        ReferenceArrayList<String> supersuperfaceList;
        Object2ReferenceOpenHashMap<String, MethodNode> methodIdentifiers = null;
        ObjectOpenHashSet<String> currentImplementations;


        for (i = 0; i < supertypeCount; i++) {
            supertype = supertypes[i];

            try {
                Class.forName(ASMUtil.toBinaryName(supertype), false, LOADER);
            } catch (final ClassNotFoundException exception) {
                throw new RuntimeException(exception);
            }

            if (inheritableFields.containsKey(supertype)) {
                if ((klass.access & Opcodes.ACC_INTERFACE) == 0) {
                    if (!fieldInheritors.get(supertype).contains(klass.superName)) {
                        fields = inheritableFields.get(supertype).elements();
                        fieldCount = fields.length;

                        for (j = 0; j < fieldCount; j++) {
                            field = fields[j];

                            if (!klass.fields.contains(field)) {
                                klass.fields.add(field);
                            }
                        }

                        superfaceArray = superfaces.get(supertype).elements();
                        superfaceCount = superfaceArray.length;

                        for (k = 0; k < superfaceCount; k++) {
                            fieldInheritors.get(superfaceArray[k]).add(klass.name);
                        }
                    }
                } else {
                    if (fieldInheritors.containsKey(klass.name)) {
                        fieldInheritors.get(klass.name).addAll(fieldInheritors.get(supertype));
                        inheritableFields.get(klass.name).addAll(inheritableFields.get(supertype));
                    } else {
                        fieldInheritors.put(klass.name, new ObjectOpenHashSet<>(fieldInheritors.get(supertype)));
                        inheritableFields.put(klass.name, inheritableFields.get(supertype));
                    }

                    superfaceList = superfaces.get(klass.name);

                    if (superfaceList == null) {
                        superfaceList = ReferenceArrayList.wrap(new String[]{klass.name});
                        superfaces.put(klass.name, superfaceList);
                    }

                    superfaceList.addAll(superfaces.get(supertype));
                }
            }

            if (inheritableMethods.containsKey(supertype)) {
                superaccessorList = inheritableMethods.get(supertype);
                accessors = superaccessorList.elements();
                accessorCount = accessors.length;

                if ((klass.access & Opcodes.ACC_INTERFACE) == 0) {
                    if (methodIdentifiers == null) {
                        methodIdentifiers = new Object2ReferenceOpenHashMap<>();

                        for (j = 0; j < methodCount; j++) {
                            methodIdentifiers.put((method = methods[j]).name + method.desc, method);
                        }
                    }

                    for (j = 0; j < accessorCount; j++) {
                        accessor = accessors[j];

                        if (methodIdentifiers.containsKey(accessor.name + accessor.descriptor)) {
                            accessor.accept(methodIdentifiers.get(accessor.name + accessor.descriptor), klass.name);
                        } else {
                            klass.methods.add(accessor.toNode(klass.name));
                        }
                    }

                    currentImplementations = methodInheritors.get(klass.name);

                    if (currentImplementations == null) {
                        methodInheritors.put(klass.name, new ObjectOpenHashSet<>());
                    } else {
                        methodInheritors.get(supertype).addAll(currentImplementations);
                    }
                } else {
                    accessorList = inheritableMethods.get(klass.name);

                    if (accessorList ==  null) {
                        inheritableMethods.put(klass.name, ReferenceArrayList.wrap(superaccessorList.elements().clone()));
                    } else {
                        accessorList.addAll(superaccessorList);
                    }

                    superfaceList = superfaces.get(klass.name);

                    if (superfaceList == null) {
                        superfaceList = ReferenceArrayList.wrap(new String[]{klass.name});
                        superfaces.put(klass.name, superfaceList);
                    }

                    supersuperfaceList = superfaces.get(supertype);

                    if (supersuperfaceList != null) {
                        superfaceList.addAll(supersuperfaceList);
                    }
                }
            }
        }
    }

    private static void inline(final MethodNode in) {
        AbstractInsnNode instruction = in.instructions.getFirst();
        AbstractInsnNode inlineInstruction;
        MethodInsnNode methodInstruction;
        MethodNode inlineMethod;
        LabelNode end;
        DelegatingInsnList appetizer;
        InsnList instructions;
        String[] parameters;
        int parameterCount;
        int i;
        int locals;
        int opcode;

        while (instruction != null) {
            if (instruction instanceof MethodInsnNode) {
                methodInstruction = (MethodInsnNode) instruction;
                inlineMethod = inlineMethods.get(methodInstruction.owner + methodInstruction.name + methodInstruction.desc);

                if (inlineMethod != null) {
                    inlineInstruction = inlineMethod.instructions.getFirst();
                    end = new LabelNode();
                    appetizer = new DelegatingInsnList();
                    parameters = ASMUtil.getExplicitParameters(inlineMethod).toArray(new String[0]);
                    parameterCount = ASMUtil.countExplicitParameters(inlineMethod);
                    locals = in.maxLocals;

                    for (i = parameterCount; i > 0; --i) {
                        appetizer.add(new VarInsnNode(ASMUtil.getLoadOpcode(parameters[i]), locals++));
                    }

                    instructions = ASMUtil.copyInstructions(inlineMethod.instructions, appetizer);

                    while (inlineInstruction != null) {
                        opcode = inlineInstruction.getOpcode();

                        if (ASMUtil.isLoadOpcode(opcode) || ASMUtil.isStoreOpcode(opcode)) {
                            ((VarInsnNode) inlineInstruction).var += locals;
                        }

                        inlineInstruction = inlineInstruction.getNext();
                    }

                    instructions.add(end);

                    in.instructions.add(instructions);
                    in.maxLocals += inlineMethod.maxLocals;
                }
            }

            instruction = instruction.getNext();
        }
    }

    private static void extend(final ClassNode klass, final MethodNode[] methods) {
        final int supertypeCount = klass.interfaces.size() + 1;
        final String[] supertypes = klass.interfaces.toArray(new String[supertypeCount]);
        supertypes[supertypeCount - 1] = klass.superName;

        final int methodCount = methods.length;
        int j;
        int k;
        String supertype;
        MethodNode[] superconstructors;
        MethodNode superconstructor;
        int superconstructorCount;
        MethodNode method;
        ObjectOpenHashSet<String> remainingSupertypes;
        AbstractInsnNode instruction;

        for (int i = 0; i < supertypeCount; i++) {
            supertype = supertypes[i];

            try {
                Class.forName(ASMUtil.toBinaryName(supertype), false, LOADER);
            } catch (final ClassNotFoundException exception) {
                throw new RuntimeException(exception);
            }

            superconstructors = initializerHolders.get(supertype);

            if (superconstructors != null) {
                superconstructorCount = superconstructors.length;

                for (j = 0; j < methodCount; j++) {
                    method = methods[j];

                    if (isConstructor(method)) {
                        remainingSupertypes = new ObjectOpenHashSet<>(klass.interfaces);

                        for (k = 0; k < superconstructorCount; k++) {
                            instruction = method.instructions.getFirst();

                            while (instruction != null) {
                                if (instruction instanceof MethodInsnNode) {
                                    remainingSupertypes.remove(((MethodInsnNode) instruction).owner);
                                }

                                instruction = instruction.getNext();
                            }

                            if (!remainingSupertypes.isEmpty()) {
                                throw new ClassFormatException(String.format("constructor %s::%s does not call a constructor of its supertype %s", klass.name, method.name + method.desc, supertype));
                            }
                        }
                    }
                }
            }
        }
    }

    private static FieldNode getField(final ClassNode klass, final AnnotationNode annotation) {
        final String name = ASMUtil.getAnnotationValue(annotation, "name");
        final FieldNode[] fields = klass.fields.toArray(new FieldNode[0]);
        final int fieldCount = fields.length;

        for (int i = 0; i < fieldCount; i++) {
            if (fields[i].name.equals(name)) {
                throw new RuntimeException(String.format("field %s already exists in %s.", name, klass.name));
            }
        }

        return new FieldNode(ASMUtil.getAnnotationValue(annotation, "access", Declare.DEFAULT_ACCESS), name, ASMUtil.getAnnotationValue(annotation, "descriptor"), null, null);
    }

    private static void getter(final ClassNode klass, final MethodNode method, final AnnotationNode annotation) {
        final int access = setAndGetAccess(method, annotation);
        final String fieldName = ASMUtil.getAnnotationValue(annotation, "value");
        final String fieldDescriptor = ASMUtil.getReturnType(method.desc);

        if ((method.access & Opcodes.ACC_NATIVE) != 0) {
            method.visitVarInsn(Opcodes.ALOAD, 0);
            method.visitFieldInsn(Opcodes.GETFIELD, klass.name, fieldName, fieldDescriptor);
            method.visitInsn(ASMUtil.getReturnOpcode(fieldDescriptor));

            method.access = access;
        } else if ((klass.access & Opcodes.ACC_INTERFACE) == 0) {
            final DelegatingInsnList instructions = new DelegatingInsnList();

            instructions.addVarInsn(Opcodes.ALOAD, 0);
            instructions.addFieldInsn(Opcodes.GETFIELD, klass.name, fieldName, fieldDescriptor);
            instructions.addInsn(ASMUtil.getReturnOpcode(fieldDescriptor));

            ASMUtil.insertBeforeEveryReturn(method, instructions);

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
        final ReferenceArrayList<String> descriptor = ASMUtil.parseDescriptor(method);
        final String fieldName = ASMUtil.getAnnotationValue(annotation, "value");
        final String fieldDescriptor = descriptor.get(0);
        final int access = setAndGetAccess(method, annotation);

        if ((method.access & Opcodes.ACC_NATIVE) != 0) {
            method.visitVarInsn(Opcodes.ALOAD, 0);
            method.visitVarInsn(ASMUtil.getLoadOpcode(fieldDescriptor), 1);
            method.visitFieldInsn(Opcodes.PUTFIELD, klass.name, fieldName, fieldDescriptor);
            method.visitInsn(ASMUtil.getReturnOpcode(descriptor.top()));

            method.access = access;
        } else if ((klass.access & Opcodes.ACC_INTERFACE) == 0) {
            final DelegatingInsnList instructions = new DelegatingInsnList();

            instructions.addVarInsn(Opcodes.ALOAD, 0);
            instructions.addVarInsn(ASMUtil.getLoadOpcode(fieldDescriptor), 1);
            instructions.addFieldInsn(Opcodes.PUTFIELD, klass.name, fieldName, fieldDescriptor);

            ASMUtil.insertBeforeEveryReturn(method, instructions);

            method.access = access;
        } else {
            final ReferenceArrayList<AccessorInfo> accessors = inheritableMethods.get(klass.name);
            final SetterInfo info = new SetterInfo(method);
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
        final int access = ASMUtil.getAnnotationValue(annotation, "access", Setter.DEFAULT_ACCESS);
        final int newAccess;

        if (access == Getter.DEFAULT_ACCESS) {
            newAccess = method.access & ~ASMUtil.ACC_NATIVE;
        } else if (ASMUtil.getAnnotationValue(annotation, "accessType", AccessType.OVERRIDE) == AccessType.OVERRIDE) {
            newAccess = access;
        } else {
            newAccess = method.access | access;
        }

        return newAccess;
    }

    private static boolean isConstructor(final MethodNode method) {
        return "<init>".equals(method.name) || method.invisibleAnnotations != null
                && ASMUtil.getAnnotationValue(method.invisibleAnnotations, Initializer.class, "type", Initializer.Type.CONSTRUCTOR) == Initializer.Type.CONSTRUCTOR;
    }

/*    public static void retransformMethodNode(final ClassNode klass) {
        final MethodNode[] methods = klass.methods.toArray(new MethodNode[0]);
        final int methodCount = methods.length;
        MethodNode method;
        AbstractInsnNode instruction;

        for (int i = 0; i < methodCount; i++) {
            method = methods[i];

            if ("visitMethodInsn".equals(method.name)) {
                instruction = method.instructions.getLast();

                while (instruction != null) {
                    if (instruction instanceof MethodInsnNode) {
                        while (true) {
                            method.instructions.remove(instruction);

                            if (instruction instanceof VarInsnNode && ((VarInsnNode) instruction).var == 0) {
                                break;
                            }

                            instruction = instruction.getPrevious();
                        }

                        break;
                    }

                    instruction = instruction.getPrevious();
                }
            }
        }
    }*/
}
