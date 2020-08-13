package user11681.jpp.synthesis;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;
import user11681.jpp.asm.ASMUtil;
import user11681.jpp.asm.DelegatingInsnList;

public class GetterInfo extends AccessorInfo {
    public GetterInfo(final MethodNode method) {
        super(method);
    }

    @Override
    public MethodNode toNode(final String owner) {
        final MethodNode method = new MethodNode(this.access, this.name, this.descriptor, this.signature, this.exceptions);

        method.visitVarInsn(Opcodes.ALOAD, 0);
        method.visitFieldInsn(Opcodes.GETFIELD, owner, this.fieldName, this.fieldDescriptor);
        method.visitInsn(ASMUtil.getReturnOpcode(this.fieldDescriptor));

        return method;
    }

    @Override
    public void accept(final MethodNode method, final String owner) {
        final DelegatingInsnList instructions = new DelegatingInsnList();

        instructions.addVarInsn(Opcodes.ALOAD, 0);
        instructions.addFieldInsn(Opcodes.GETFIELD, owner, this.fieldName, this.fieldDescriptor);

        ASMUtil.insertBeforeEveryReturn(method, instructions);
    }
}
