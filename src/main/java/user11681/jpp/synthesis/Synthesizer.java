package user11681.jpp.synthesis;

import com.sun.org.apache.bcel.internal.classfile.ClassFormatException;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
import user11681.jpp.asm.ASMUtil;
import user11681.jpp.asm.DelegatingInsnList;

public class Synthesizer {
    private static final Logger LOGGER = LogManager.getLogger("jpp/Synthesizer");
    private static final Object2ReferenceOpenHashMap<String, ReferenceArrayList<String>> superfaces = new Object2ReferenceOpenHashMap<>();
    private static final Object2ReferenceOpenHashMap<String, ReferenceArrayList<FieldNode>> fields = new Object2ReferenceOpenHashMap<>();
    private static final Object2ReferenceOpenHashMap<String, MethodNode[]> initializerHolders = new Object2ReferenceOpenHashMap<>();
    private static final Object2ReferenceOpenHashMap<String, MethodNode> inlineMethods = new Object2ReferenceOpenHashMap<>();
    private static final Object2ReferenceOpenHashMap<String, ObjectOpenHashSet<String>> implementations = new Object2ReferenceOpenHashMap<>();

    private static final ClassLoader LOADER = Thread.currentThread().getContextClassLoader();

    public static void transformNew(final ClassNode klass) {
        AnnotationNode[] annotations;
        AnnotationNode annotation;
        MethodNode[] methods;
        MethodNode method;
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
                        fields.put(klass.name, ReferenceArrayList.wrap(fieldNodes));
                        implementations.put(klass.name, new ObjectOpenHashSet<>());
                    }

                    break;
                } else if (Type.getDescriptor(Declare.class).equals(annotation.desc)) {
                    if ((klass.access & Opcodes.ACC_INTERFACE) == 0) {
                        klass.fields.add(getField(klass, annotation));
                    } else {
                        superfaces.put(klass.name, ReferenceArrayList.wrap(new String[]{klass.name}));
                        fields.put(klass.name, ReferenceArrayList.wrap(new FieldNode[]{getField(klass, annotation)}));
                        implementations.put(klass.name, new ObjectOpenHashSet<>());
                    }

                    break;
                }
            }
        }

        final List<String> interfaceList = klass.interfaces;

        if (!interfaceList.isEmpty()) {
            try {
                Class.forName(ASMUtil.toBinaryName(klass.superName), false, LOADER);
            } catch (final ClassNotFoundException exception) {
                throw new RuntimeException(exception);
            }

            final String[] interfaces = interfaceList.toArray(new String[0]);
            final int interfaceCount = interfaces.length;
            String interfase;
            FieldNode[] fields;
            FieldNode field;
            String[] superfaceArray;
            String[] superface;
            int fieldCount;
            int superfaceCount;
            int k;

            for (i = 0; i < interfaceCount; i++) {
                interfase = interfaces[i];

                try {
                    Class.forName(ASMUtil.toBinaryName(interfase), false, LOADER);
                } catch (final ClassNotFoundException exception) {
                    throw new RuntimeException(exception);
                }

                if (Synthesizer.fields.containsKey(interfase)) {
                    if ((klass.access & Opcodes.ACC_INTERFACE) == 0) {
                        if (!implementations.get(interfase).contains(klass.superName)) {
                            fields = Synthesizer.fields.get(interfase).elements();
                            fieldCount = fields.length;

                            for (j = 0; j < fieldCount; j++) {
                                field = fields[j];

                                if (!klass.fields.contains(field)) {
                                    klass.fields.add(field);
                                }
                            }

                            superfaceArray = superfaces.get(interfase).elements();
                            superfaceCount = superfaceArray.length;

                            for (k = 0; k < superfaceCount; k++) {
                                implementations.get(superfaceArray[k]).add(klass.name);
                            }
                        }
                    } else {
                        if (implementations.containsKey(klass.name)) {
                            implementations.get(klass.name).addAll(implementations.get(interfase));
                            Synthesizer.fields.get(klass.name).addAll(Synthesizer.fields.get(interfase));
                        } else {
                            implementations.put(klass.name, new ObjectOpenHashSet<>(implementations.get(interfase)));
                            Synthesizer.fields.put(klass.name, Synthesizer.fields.get(interfase));
                        }

                        ReferenceArrayList<String> superfaceList = superfaces.get(klass.name);

                        if (superfaceList == null) {
                            superfaceList = ReferenceArrayList.wrap(new String[]{klass.name});
                            superfaces.put(klass.name, superfaceList);
                        }

                        superfaceList.addAll(superfaces.get(interfase));
                    }
                }
            }
        }
    }

    private static void transform(final ClassNode klass) {
        final MethodNode[] methods = klass.methods.toArray(new MethodNode[0]);
        final int methodCount = methods.length;
        MethodNode method;
        AnnotationNode[] annotations;
        AnnotationNode annotation;
        int annotationCount;
        int fieldCount;
        int i;
        int j;

        final ReferenceArrayList<MethodNode> constructors = new ReferenceArrayList<>();
        final ReferenceArrayList<MethodNode> initializers = new ReferenceArrayList<>();
        FieldNode[] fields;
        FieldNode field;

        for (i = 0; i < methodCount; i++) {
            method = methods[i];

            if ("<init>".equals(method.name)) {
                constructors.add(method);

                if (Synthesizer.fields.containsKey(klass.name)) {
                    fields = Synthesizer.fields.get(klass.name).toArray(new FieldNode[0]);
                    fieldCount = fields.length;

                    for (j = 0; j < fieldCount; j++) {
                        field = fields[j];


                    }
                }
            } else if (method.invisibleAnnotations != null) {
                annotations = method.invisibleAnnotations.toArray(new AnnotationNode[0]);
                annotationCount = annotations.length;

                for (j = 0; j < annotationCount; j++) {
                    annotation = annotations[j];

                    if (Type.getDescriptor(Getter.class).equals(annotation.desc)) {
                        get(klass, method, annotation);
                    } else if (Type.getDescriptor(Setter.class).equals(annotation.desc)) {
                        set(klass, method, annotation);
                    } else if (Type.getDescriptor(Inline.class).equals(annotation.desc)) {
                        inlineMethods.put(klass.name + method.name + method.desc, method);
                    } else if (Type.getDescriptor(Initializer.class).equals(annotation.desc)) {
                        if (ASMUtil.getAnnotationValue(annotation, "type", Initializer.Type.CONSTRUCTOR) == Initializer.Type.CONSTRUCTOR) {
                            constructors.add(method);
                        } else {
                            initializers.add(method);
                        }
                    }
                }
            }
        }

        final MethodNode[] constructorArray = constructors.toArray(new MethodNode[0]);
        final MethodNode[] initializerArray = initializers.toArray(new MethodNode[0]);
        MethodNode initializer;

        initializerHolders.put(klass.name, constructorArray);

        for (i = 0; i < initializerArray.length; i++) {
            initializer = initializerArray[i];

            for (j = 0; j < constructorArray.length; j++) {
                ASMUtil.insertBeforeEveryReturn(constructorArray[j], initializer.instructions);
            }

            klass.methods.remove(initializer);
        }

        extend(klass, methods);
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
        final ObjectOpenHashSet<String> supertypeList = new ObjectOpenHashSet<>(klass.interfaces);
        supertypeList.add(klass.superName);

        final String[] supertypes = supertypeList.toArray(new String[0]);
        final int supertypeCount = supertypes.length;
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
                        remainingSupertypes = new ObjectOpenHashSet<>(supertypeList);

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

    private static void get(final ClassNode klass, final MethodNode method, final AnnotationNode annotation) {
        final String fieldDescriptor = Type.getReturnType(method.desc).getDescriptor();
        final String fieldName = ASMUtil.getAnnotationValue(annotation, "value");
        final DelegatingInsnList instructions = new DelegatingInsnList();

        instructions.addVarInsn(Opcodes.ALOAD, 0);
        instructions.addFieldInsn(Opcodes.GETFIELD, klass.name, fieldName, fieldDescriptor);

        if (insertAndSetAccess(method, annotation, instructions)) {
            instructions.addInsn(ASMUtil.getReturnOpcode(fieldDescriptor));

            method.instructions.insert(instructions);
        }
    }

    private static void set(final ClassNode klass, MethodNode method, final AnnotationNode annotation) {
        final String fieldDescriptor = ASMUtil.getExplicitParameters(method).get(0);
        final String fieldName = ASMUtil.getAnnotationValue(annotation, "value");
        final DelegatingInsnList instructions = new DelegatingInsnList();

        instructions.addVarInsn(Opcodes.ALOAD, 0);
        instructions.addVarInsn(ASMUtil.getLoadOpcode(fieldDescriptor), 1);
        instructions.addFieldInsn(Opcodes.PUTFIELD, klass.name, fieldName, fieldDescriptor);

        if (insertAndSetAccess(method, annotation, instructions)) {
            instructions.addInsn(ASMUtil.getReturnOpcode(method));

            method.instructions.insert(instructions);
        }
    }

    private static boolean insertAndSetAccess(final MethodNode method, final AnnotationNode annotation, final InsnList instructions) {
        final int access = ASMUtil.getAnnotationValue(annotation, "access", Setter.DEFAULT_ACCESS);
        final boolean override = ASMUtil.getAnnotationValue(annotation, "overrideAccess", true);

        boolean incomplete = true;

        for (final AbstractInsnNode instruction : method.instructions) {
            if (ASMUtil.isReturnOpcode(instruction.getOpcode())) {
                method.instructions.insertBefore(instruction, instructions);

                incomplete = false;
            }
        }

        if (access == Getter.DEFAULT_ACCESS) {
            if (override && incomplete) {
                method.access &= ~ASMUtil.ABSTRACT_ALL;
            }
        } else {
            if (override) {
                method.access = access;
            } else {
                method.access |= access;
            }
        }

        return incomplete;
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

    static {
        LOGGER.info("Initializing.");
    }
}
