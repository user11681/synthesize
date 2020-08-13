package user11681.jpp.synthesis;

import org.objectweb.asm.tree.MethodNode;

public abstract class AccessorInfo {
    public final String name;
    public final String descriptor;
    public final String signature;
    public final String[] exceptions;

    public int access;
    public String fieldName;
    public String fieldDescriptor;

    public AccessorInfo(final MethodNode method) {
        this.name = method.name;
        this.descriptor = method.desc;
        this.signature = method.signature;
        this.exceptions = method.exceptions.toArray(new String[0]);
    }

    public abstract MethodNode toNode(String owner);

    public abstract void accept(MethodNode method, String owner);
}
