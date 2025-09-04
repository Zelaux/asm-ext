package main.java.asmext;

import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.TypePath;
import org.objectweb.asm.util.Printer;
import org.objectweb.asm.util.Textifier;


/**
 * @author Zelaux
 * @since 2025-09
 */
public class PrintInsnTextifier extends Textifier {
    {
        tab3 = tab;
        ltab = tab2 = "";
        tab = null;
    }

    public String getString(){
        return stringBuilder.toString();
    }
    @Override
    public void visitInsn(int opcode) {
        super.visitInsn(opcode);
        text.clear();
    }

    @Override
    public void visitIntInsn(int opcode, int operand) {
        super.visitIntInsn(opcode, operand);
        text.clear();
    }

    @Override
    public void visitVarInsn(int opcode, int varIndex) {
        super.visitVarInsn(opcode, varIndex);
        text.clear();
    }

    @Override
    public void visitTypeInsn(int opcode, String type) {
        super.visitTypeInsn(opcode, type);
        text.clear();
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
        super.visitFieldInsn(opcode, owner, name, descriptor);
        text.clear();
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
        text.clear();
    }

    @Override
    public void visitInvokeDynamicInsn(String name, String descriptor, Handle bootstrapMethodHandle, Object... bootstrapMethodArguments) {
        super.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments);
        text.clear();
    }

    @Override
    public void visitJumpInsn(int opcode, Label label) {
        super.visitJumpInsn(opcode, label);
        text.clear();
    }

    @Override
    public void visitLabel(Label label) {
        super.visitLabel(label);
        text.clear();
    }

    @Override
    public void visitLdcInsn(Object value) {
        super.visitLdcInsn(value);
        text.clear();
    }

    @Override
    public void visitIincInsn(int varIndex, int increment) {
        super.visitIincInsn(varIndex, increment);
        text.clear();
    }

    @Override
    public void visitTableSwitchInsn(int min, int max, Label dflt, Label... labels) {
        super.visitTableSwitchInsn(min, max, dflt, labels);
        text.clear();
    }

    @Override
    public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
        super.visitLookupSwitchInsn(dflt, keys, labels);
        text.clear();
    }

    @Override
    public void visitMultiANewArrayInsn(String descriptor, int numDimensions) {
        super.visitMultiANewArrayInsn(descriptor, numDimensions);
        text.clear();
    }

    @Override
    public Printer visitInsnAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
        Printer printer = super.visitInsnAnnotation(typeRef, typePath, descriptor, visible);
        text.clear();
        return printer;
    }
}
