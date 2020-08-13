package user11681.jpp.synthesis;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;
import user11681.jpp.asm.ASMUtil;
import user11681.jpp.asm.DelegatingInsnList;

public class SetterInfo extends AccessorInfo {
    public SetterInfo(final MethodNode method) {
        super(method);
    }

    @Override
    public MethodNode toNode(final String owner) {
        final MethodNode method = new MethodNode(this.access, this.name, this.descriptor, this.signature, this.exceptions);

        method.visitVarInsn(Opcodes.ALOAD, 0);
        method.visitVarInsn(ASMUtil.getLoadOpcode(this.fieldDescriptor), 1);
        method.visitFieldInsn(Opcodes.PUTFIELD, owner, this.fieldName, this.fieldDescriptor);
        method.visitInsn(ASMUtil.getReturnOpcode(method));

        return method;
    }

    @Override
    public void accept(final MethodNode method, final String owner) {
        final DelegatingInsnList instructions = new DelegatingInsnList();

        instructions.addVarInsn(Opcodes.ALOAD, 0);
        instructions.addVarInsn(ASMUtil.getLoadOpcode(this.fieldDescriptor), 1);
        instructions.addFieldInsn(Opcodes.PUTFIELD, owner, this.fieldName, this.fieldDescriptor);

        ASMUtil.insertBeforeEveryReturn(method, instructions);
    }
}
