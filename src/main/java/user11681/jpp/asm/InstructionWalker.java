package user11681.jpp.asm;

import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.MultiANewArrayInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

public class InstructionWalker implements ASMUtil {
    public final ReferenceArrayList<String> operands = new ReferenceArrayList<>();
    public final ReferenceArrayList<String> variables = new ReferenceArrayList<>();

    public AbstractInsnNode instruction;

    public InstructionWalker(final MethodNode method) {
        this.instruction = method.instructions.getFirst();
    }

    public InstructionWalker(final InsnList instructions) {
        this.instruction = instructions.getFirst();
    }

    public InstructionWalker(final AbstractInsnNode firstInstruction) {
        this.instruction = firstInstruction;
    }

    public AbstractInsnNode next() {
        final AbstractInsnNode instruction = this.instruction.getNext();

        switch (instruction.getOpcode()) {
            case ACONST_NULL:
                this.operands.push("null");
                break;
            case ICONST_M1:
            case ICONST_0:
            case ICONST_1:
            case ICONST_2:
            case ICONST_3:
            case ICONST_4:
            case ICONST_5:
            case ILOAD:
            case BIPUSH:
            case SIPUSH:
                this.operands.push("I");
                break;
            case LCONST_0:
            case LCONST_1:
            case LLOAD:
                this.operands.push("J");
                this.operands.push("J");
                break;
            case FCONST_0:
            case FCONST_1:
            case FCONST_2:
            case FLOAD:
                this.operands.push("F");
                break;
            case DCONST_0:
            case DCONST_1:
            case DLOAD:
                this.operands.push("D");
                this.operands.push("D");
                break;
            case IALOAD:
            case BALOAD:
            case CALOAD:
            case SALOAD:
                this.operands.pop();
                this.operands.pop();
                this.operands.push("I");
                break;
            case LDC:
                this.push(Type.getDescriptor(((LdcInsnNode) instruction).cst.getClass()));
                break;
            case ALOAD:
                this.operands.push("Ljava/lang/Object;");
                break;
            case LALOAD:
                this.operands.pop();
                this.operands.pop();
                this.operands.push("J");
                this.operands.push("J");
                break;
            case FALOAD:
                this.operands.pop();
                this.operands.pop();
                this.operands.push("F");
                break;
            case DALOAD:
                this.operands.pop();
                this.operands.pop();
                this.operands.push("D");
                this.operands.push("D");
                break;
            case AALOAD:
                this.operands.pop();
                this.operands.pop();
                this.operands.push("Ljava/lang/Object;");
                break;
            case ISTORE:
            case FSTORE:
            case ASTORE:
                this.variables.set(((VarInsnNode) instruction).var, this.operands.pop());
                break;
            case LSTORE:
            case DSTORE:
                this.variables.set(((VarInsnNode) instruction).var, this.operands.pop());
                this.variables.set(((VarInsnNode) instruction).var + 1, this.operands.pop());
                break;
            case IASTORE:
            case FASTORE:
            case AASTORE:
            case BASTORE:
            case CASTORE:
            case SASTORE:
                this.operands.pop();
                this.operands.pop();
                this.operands.pop();
                break;
            case LASTORE:
            case DASTORE:
                this.operands.pop();
                this.operands.pop();
                this.operands.pop();
                this.operands.pop();
                break;
            case POP:
            case IADD:
            case FADD:
            case ISUB:
            case FSUB:
            case IMUL:
            case FMUL:
            case IDIV:
            case FDIV:
            case IREM:
            case FREM:
            case INEG:
            case FNEG:
            case ISHL:
            case ISHR:
            case IUSHR:
            case IAND:
            case IOR:
            case IXOR:
            case IFEQ:
            case IFNE:
            case IFLT:
            case IFGE:
            case IFGT:
            case IFLE:
            case TABLESWITCH:
            case LOOKUPSWITCH:
            case IRETURN:
            case FRETURN:
            case ARETURN:
            case MONITORENTER:
            case MONITOREXIT:
            case IFNULL:
            case IFNONNULL:
                this.operands.pop();
                break;
            case POP2:
            case LADD:
            case DADD:
            case LSUB:
            case DSUB:
            case LMUL:
            case DMUL:
            case LDIV:
            case DDIV:
            case LREM:
            case DREM:
            case LNEG:
            case DNEG:
            case LSHL:
            case LSHR:
            case LUSHR:
            case LAND:
            case LOR:
            case LXOR:
            case IF_ICMPEQ:
            case IF_ICMPNE:
            case IF_ICMPLT:
            case IF_ICMPGE:
            case IF_ICMPGT:
            case IF_ICMPLE:
            case IF_ACMPEQ:
            case IF_ACMPNE:
            case LRETURN:
            case DRETURN:
                this.operands.pop();
                this.operands.pop();
                break;
            case DUP:
                this.dup(0);
                break;
            case DUP_X1:
                this.dup(1);
                break;
            case DUP_X2:
                this.dup(2);
                break;
            case DUP2:
                this.dup2(0);
                break;
            case DUP2_X1:
                this.dup2(1);
                break;
            case DUP2_X2:
                this.dup2(2);
                break;
            case SWAP:
                this.swap();
                break;
            case I2L:
                this.operands.set(this.operands.size() - 1, "J");
                this.operands.push("J");
                break;
            case I2F:
                this.operands.set(this.operands.size() - 1, "F");
                break;
            case I2D:
                this.operands.set(this.operands.size() - 1, "D");
                this.operands.push("D");
                break;
            case L2I:
            case D2I:
            case FCMPL:
            case FCMPG:
                this.operands.pop();
                this.operands.set(this.operands.size() - 1, "I");
                break;
            case L2F:
            case D2F:
                this.operands.pop();
                this.operands.set(this.operands.size() - 1, "F");
                break;
            case L2D:
                this.operands.set(this.operands.size() - 2, "D");
                this.operands.set(this.operands.size() - 1, "D");
                break;
            case F2I:
            case I2B:
            case I2C:
            case I2S:
            case ARRAYLENGTH:
            case INSTANCEOF:
                this.operands.set(this.operands.size() - 1, "I");
                break;
            case F2L:
                this.operands.pop();
                this.operands.add("L");
                this.operands.add("L");
                break;
            case F2D:
                this.operands.set(this.operands.size() - 1, "D");
                this.operands.add("D");
                break;
            case D2L:
                this.operands.set(this.operands.size() - 2, "L");
                this.operands.set(this.operands.size() - 1, "L");
                break;
            case LCMP:
            case DCMPL:
            case DCMPG:
                this.operands.pop();
                this.operands.pop();
                this.operands.pop();
                this.operands.set(this.operands.size() - 1, "I");
                break;
            case GETSTATIC:
            case PUTSTATIC:
                this.push(((FieldInsnNode) instruction).desc);
                break;
            case GETFIELD:
            case PUTFIELD:
                this.push(((FieldInsnNode) instruction).desc);
                this.operands.pop();
                break;
            case INVOKEVIRTUAL:
            case INVOKESPECIAL:
            case INVOKEINTERFACE:
            case INVOKESTATIC:
                this.walk((MethodInsnNode) instruction);
                break;
            case INVOKEDYNAMIC:
                this.walk((InvokeDynamicInsnNode) instruction);
                break;
            case NEW:
                this.operands.add(((TypeInsnNode) instruction).desc);
                break;
            case NEWARRAY:
                this.operands.pop();
                this.operands.add(((TypeInsnNode) instruction).desc);
                break;
            case ANEWARRAY:
                this.operands.pop();
                this.operands.add("[" + ((TypeInsnNode) instruction).desc);
                break;
            case ATHROW:
                this.operands.clear();
                this.operands.add("Ljava/lang/Throwable;");
                break;
            case MULTIANEWARRAY:
                this.walk((MultiANewArrayInsnNode) instruction);
                break;
        }

        return instruction;
    }

/*    public AbstractInsnNode previous() {
        switch (this.instruction.getOpcode()) {
            case ACONST_NULL:
            case ICONST_M1:
            case ICONST_0:
            case ICONST_1:
            case ICONST_2:
            case ICONST_3:
            case ICONST_4:
            case ICONST_5:
            case ILOAD:
            case FCONST_0:
            case FCONST_1:
            case FCONST_2:
            case FLOAD:
            case BIPUSH:
            case SIPUSH:
            case LDC:
            case ALOAD:
                this.operands.pop();
                break;
            case LCONST_0:
            case LCONST_1:
            case LLOAD:
            case DCONST_0:
            case DCONST_1:
            case DLOAD:
                this.operands.pop();
                this.operands.pop();
                break;
            case IALOAD:
            case BALOAD:
            case CALOAD:
            case SALOAD:
            case FALOAD:
            case AALOAD:
                this.operands.pop();
                this.operands.push("I");
                this.operands.push("I");
                break;
            case LALOAD:
            case DALOAD:
                this.operands.pop();
                this.operands.pop();
                this.operands.push("I");
                this.operands.push("I");
                break;
            case ISTORE:
                this.operands.push("I");
                break;
            case FSTORE:
                this.operands.push("F");
                break;
            case ASTORE:
                this.operands.push("Ljava/lang/Object;");
                break;
            case LSTORE:
                this.operands.push("J");
                this.operands.push("J");
                break;
            case DSTORE:
                this.operands.push("D");
                this.operands.push("D");
                break;
            case IASTORE:
            case BASTORE:
            case CASTORE:
            case SASTORE:
                this.operands.push("I");
                this.operands.push("I");
                this.operands.push("I");
                break;
            case FASTORE:
                this.operands.push("I");
                this.operands.push("I");
                this.operands.push("F");
                break;
            case LASTORE:
                this.operands.push("I");
                this.operands.push("I");
                this.operands.push("L");
                this.operands.push("L");
                break;
            case DASTORE:
                this.operands.push("I");
                this.operands.push("I");
                this.operands.push("D");
                this.operands.push("D");
                break;
            case AASTORE:
                this.operands.push("I");
                this.operands.push("I");
                this.operands.push("Ljava/lang/Object;");
                break;
            case POP:
            case IADD:
            case FADD:
            case ISUB:
            case FSUB:
            case IMUL:
            case FMUL:
            case IDIV:
            case FDIV:
            case IREM:
            case FREM:
            case INEG:
            case FNEG:
            case ISHL:
            case ISHR:
            case IUSHR:
            case IAND:
            case IOR:
            case IXOR:
            case IFEQ:
            case IFNE:
            case IFLT:
            case IFGE:
            case IFGT:
            case IFLE:
            case TABLESWITCH:
            case LOOKUPSWITCH:
            case IRETURN:
            case FRETURN:
            case ARETURN:
            case MONITORENTER:
            case MONITOREXIT:
            case IFNULL:
            case IFNONNULL:
                this.operands.pop();
                break;
            case POP2:
            case LADD:
            case DADD:
            case LSUB:
            case DSUB:
            case LMUL:
            case DMUL:
            case LDIV:
            case DDIV:
            case LREM:
            case DREM:
            case LNEG:
            case DNEG:
            case LSHL:
            case LSHR:
            case LUSHR:
            case LAND:
            case LOR:
            case LXOR:
            case IF_ICMPEQ:
            case IF_ICMPNE:
            case IF_ICMPLT:
            case IF_ICMPGE:
            case IF_ICMPGT:
            case IF_ICMPLE:
            case IF_ACMPEQ:
            case IF_ACMPNE:
            case LRETURN:
            case DRETURN:
                this.operands.pop();
                this.operands.pop();
                break;
            case DUP:
                this.dup(0);
                break;
            case DUP_X1:
                this.dup(1);
                break;
            case DUP_X2:
                this.dup(2);
                break;
            case DUP2:
                this.dup2(0);
                break;
            case DUP2_X1:
                this.dup2(1);
                break;
            case DUP2_X2:
                this.dup2(2);
                break;
            case SWAP:
                this.swap();
                break;
            case I2L:
                this.operands.set(this.operands.size() - 1, "J");
                this.operands.push("J");
                break;
            case I2F:
                this.operands.set(this.operands.size() - 1, "F");
                break;
            case I2D:
                this.operands.set(this.operands.size() - 1, "D");
                this.operands.push("D");
                break;
            case L2I:
            case D2I:
            case FCMPL:
            case FCMPG:
                this.operands.pop();
                this.operands.set(this.operands.size() - 1, "I");
                break;
            case L2F:
            case D2F:
                this.operands.pop();
                this.operands.set(this.operands.size() - 1, "F");
                break;
            case L2D:
                this.operands.set(this.operands.size() - 2, "D");
                this.operands.set(this.operands.size() - 1, "D");
                break;
            case F2I:
            case I2B:
            case I2C:
            case I2S:
            case ARRAYLENGTH:
            case INSTANCEOF:
                this.operands.set(this.operands.size() - 1, "I");
                break;
            case F2L:
                this.operands.pop();
                this.operands.add("L");
                this.operands.add("L");
                break;
            case F2D:
                this.operands.set(this.operands.size() - 1, "D");
                this.operands.add("D");
                break;
            case D2L:
                this.operands.set(this.operands.size() - 2, "L");
                this.operands.set(this.operands.size() - 1, "L");
                break;
            case LCMP:
            case DCMPL:
            case DCMPG:
                this.operands.pop();
                this.operands.pop();
                this.operands.pop();
                this.operands.set(this.operands.size() - 1, "I");
                break;
            case GETSTATIC:
            case PUTSTATIC:
                this.push(((FieldInsnNode) this.instruction).desc);
                break;
            case GETFIELD:
            case PUTFIELD:
                this.push(((FieldInsnNode) this.instruction).desc);
                this.operands.pop();
                break;
            case INVOKEVIRTUAL:
            case INVOKESPECIAL:
            case INVOKEINTERFACE:
            case INVOKESTATIC:
                this.walk((MethodInsnNode) this.instruction);
                break;
            case INVOKEDYNAMIC:
                this.walk((InvokeDynamicInsnNode) this.instruction);
                break;
            case NEW:
                this.operands.add(((TypeInsnNode) this.instruction).desc);
                break;
            case NEWARRAY:
                this.operands.pop();
                this.operands.add(((TypeInsnNode) this.instruction).desc);
                break;
            case ANEWARRAY:
                this.operands.pop();
                this.operands.add("[" + ((TypeInsnNode) this.instruction).desc);
                break;
            case ATHROW:
                this.operands.clear();
                this.operands.add("Ljava/lang/Throwable;");
                break;
            case MULTIANEWARRAY:
                this.walk((MultiANewArrayInsnNode) this.instruction);
                break;
        }

        return this.instruction =  this.instruction.getPrevious();
    }*/

    private void walk(final MultiANewArrayInsnNode instruction) {
        for (int i = 0; i < instruction.dims + 1; i++) {
            this.operands.pop();
        }

        this.operands.push(instruction.desc);
    }

    private void walk(final MethodInsnNode instruction) {
        for (final String parameter : ASMUtil.getExplicitParameters(instruction)) {
            this.pop(parameter);
        }

        if (instruction.getOpcode() != INVOKESTATIC) {
            this.operands.pop();
        }

        final String returnType = ASMUtil.getReturnType(instruction);

        if (!"V".equals(returnType)) {
            this.push(returnType);
        }
    }

    private void walk(final InvokeDynamicInsnNode instruction) {
        for (final String parameter : ASMUtil.getExplicitParameters(instruction)) {
            this.pop(parameter);
        }

        this.push(ASMUtil.getReturnType(instruction));
    }

    private void dup(final int x) {
        this.operands.add(this.operands.size() - 1 - x, this.operands.top());
    }

    private void dup2(final int x) {
        final int end = this.operands.size() - 1;

        this.operands.add(end - 1 - x, this.operands.get(end - 1));
        this.operands.add(end - x, this.operands.get(end));
    }

    private void swap() {
        final int end = this.operands.size() - 1;

        this.operands.set(end, this.operands.set(end -1, this.operands.top()));
    }

    private void push(final String descriptor) {
        this.operands.push(descriptor);

        if ("J".equals(descriptor) || "D".equals(descriptor)) {
            this.operands.push(descriptor);
        }
    }

    private void pop() {
        this.pop(this.operands.top());
    }

    private void pop(final String descriptor) {
        this.operands.pop();

        if ("J".equals(descriptor) || "D".equals(descriptor)) {
            this.operands.pop();
        }
    }
}
