package asmext.postprocessors.inlinefunc;

import org.intellij.lang.annotations.MagicConstant;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;
import java.util.List;

public record MethodToInline(
        InsnList body,
        int maxLocals,
        int maxStack,
        Type[] params,
        Type returnType,
        List<LocalVariableNode> localVariables,
        List<LabelNode> labels,
        boolean multiReturn
) {
    public static MethodToInline make(MethodNode node) {
        var labels = new ArrayList<LabelNode>();
        int returnCounter = 0;
        for (AbstractInsnNode insnNode : node.instructions) {
            //noinspection MagicConstant
            @MagicConstant(valuesFromClass = AbstractInsnNode.class)
            int type = insnNode.getType();
            switch (type) {
                case AbstractInsnNode.LINE, AbstractInsnNode.FRAME,
                     AbstractInsnNode.MULTIANEWARRAY_INSN, AbstractInsnNode.LOOKUPSWITCH_INSN,
                     AbstractInsnNode.TABLESWITCH_INSN, AbstractInsnNode.IINC_INSN, AbstractInsnNode.LDC_INSN,
                     AbstractInsnNode.JUMP_INSN, AbstractInsnNode.INVOKE_DYNAMIC_INSN, AbstractInsnNode.METHOD_INSN,
                     AbstractInsnNode.FIELD_INSN, AbstractInsnNode.TYPE_INSN, AbstractInsnNode.VAR_INSN,
                     AbstractInsnNode.INT_INSN -> {
                }
                case AbstractInsnNode.LABEL -> labels.add((LabelNode) insnNode);
                case AbstractInsnNode.INSN -> {
                    int opcode = insnNode.getOpcode();
                    if (Opcodes.IRETURN <= opcode && opcode <= Opcodes.RETURN) {
                        returnCounter++;
                    }
                }
            }
        }
        return new MethodToInline(
                node.instructions,
                node.maxLocals,
                node.maxStack,
                Type.getArgumentTypes(node.desc),
                Type.getReturnType(node.desc),
                node.localVariables,
                labels,
                returnCounter > 1
        );
    }
}
