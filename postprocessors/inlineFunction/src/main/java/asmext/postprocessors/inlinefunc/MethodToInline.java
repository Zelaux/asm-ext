package asmext.postprocessors.inlinefunc;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.intellij.lang.annotations.MagicConstant;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@ToString
@FieldDefaults(level = AccessLevel.PUBLIC, makeFinal = true)
@Getter
@AllArgsConstructor
public final class MethodToInline implements Opcodes {
    String name;
    int maxLocals, maxStack;
    InsnList body;
    Type[] params;
    Type returnType;
    List<LocalVariableNode> localVariables;
    VarUsage[] localUsage;
    List<LabelNode> labels;
    int[] returnStatements;

    public static MethodToInline make(MethodNode node) {
        var labels = new ArrayList<LabelNode>();
        int returnCounter = 0;
        var instructions = node.instructions.toArray();
        var returnStatements = new int[instructions.length];

        var localUsage=new VarUsage[node.maxLocals];
        for (int i = 0; i < localUsage.length; i++) localUsage[i] = new VarUsage(i);

        for (int instructionIndex = 0; instructionIndex < instructions.length; instructionIndex++) {
            AbstractInsnNode insnNode = instructions[instructionIndex];
            //noinspection MagicConstant
            @MagicConstant(valuesFromClass = AbstractInsnNode.class)
            int type = insnNode.getType();
            int opcode = insnNode.getOpcode();
            switch (type) {
                case AbstractInsnNode.LINE, AbstractInsnNode.FRAME,
                     AbstractInsnNode.MULTIANEWARRAY_INSN, AbstractInsnNode.LOOKUPSWITCH_INSN,
                     AbstractInsnNode.TABLESWITCH_INSN, AbstractInsnNode.LDC_INSN,
                     AbstractInsnNode.JUMP_INSN, AbstractInsnNode.INVOKE_DYNAMIC_INSN, AbstractInsnNode.METHOD_INSN,
                     AbstractInsnNode.FIELD_INSN, AbstractInsnNode.TYPE_INSN,
                     AbstractInsnNode.INT_INSN -> {}
                case AbstractInsnNode.IINC_INSN -> {
                    var varInsn = (IincInsnNode) insnNode;
                    localUsage[varInsn.var].store.add(instructionIndex);
                }
                case AbstractInsnNode.VAR_INSN -> {
                    var varInsn = (VarInsnNode) insnNode;
                     if(OpcodeClassifier.isLoad(opcode)){
                         localUsage[varInsn.var].load.add(instructionIndex);
                     }else{
                         localUsage[varInsn.var].store.add(instructionIndex);
                     }
                }
                case AbstractInsnNode.LABEL -> labels.add((LabelNode) insnNode);
                case AbstractInsnNode.INSN -> {

                    if (Opcodes.IRETURN <= opcode && opcode <= Opcodes.RETURN) {
                        returnStatements[returnCounter++] = instructionIndex;
                    }
                }
            }
        }
        return new MethodToInline(
                node.name,
                node.maxLocals,
                node.maxStack,
                node.instructions,
                Type.getArgumentTypes(node.desc),
                Type.getReturnType(node.desc),
                node.localVariables,
                localUsage,
                labels,
                Arrays.copyOf(returnStatements, returnCounter)
        );
    }


    public Type[] params() {return params;}


    public List<LabelNode> labels() {return labels;}

    public boolean multiReturn() {return returnStatements.length > 1;}


}
