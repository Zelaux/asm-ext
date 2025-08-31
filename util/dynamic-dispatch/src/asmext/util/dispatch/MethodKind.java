package asmext.util.dispatch;

import lombok.AllArgsConstructor;
import org.objectweb.asm.Opcodes;

@AllArgsConstructor
public enum MethodKind {
    Virtual(Opcodes.INVOKEVIRTUAL),
    Static(Opcodes.INVOKESTATIC),
    Interface(Opcodes.INVOKEINTERFACE);
    public final int instruction;

    public boolean isStatic() {
        return this == Static;
    }

    public boolean isInterface() {
        return this == Interface;
    }
}
