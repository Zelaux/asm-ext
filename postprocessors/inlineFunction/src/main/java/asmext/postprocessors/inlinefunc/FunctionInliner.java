package asmext.postprocessors.inlinefunc;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.HashMap;

public class FunctionInliner implements Opcodes {
    public static InsnList inline(MethodDescriptor descriptor, InsnList __old__body, HashMap<String, MethodToInline> inlineMethods) {
        var array = __old__body.toArray();
        InsnList newBody = new InsnList();

        int newMaxLocal = 0;
        int newMaxStack = 0;
        var globalLabels = new HashMap<LabelNode, LabelNode>();
        for (var rawNode : array) {
            if (rawNode instanceof LabelNode labelNode) {
                globalLabels.putIfAbsent(labelNode,new LabelNode());
            }
            newBody.add(rawNode.clone(globalLabels));
            if (rawNode.getOpcode() != INVOKESTATIC) continue;
            var node = (MethodInsnNode) rawNode;
            var methodToInline = inlineMethods.get(descriptor(node));
            if (methodToInline == null) continue;
            {
                newBody.remove(newBody.getLast());
                var arguments = methodToInline.params();
                for (int i = 0; i < arguments.length; i++) {
                    Type argument = arguments[i];
                    newBody.add(new VarInsnNode(argument.getOpcode(Opcodes.ISTORE), i + descriptor.maxLocal));
                }
                HashMap<LabelNode, LabelNode> localClonedLabels = new HashMap<>();
                for (LabelNode label : methodToInline.labels()) {
                    localClonedLabels.put(label, new LabelNode());
                }
                LabelNode returnLabel=new LabelNode();
                for (AbstractInsnNode insertNode : methodToInline.body()) {
                    int opcode = insertNode.getOpcode();
                    if (insertNode instanceof VarInsnNode varInsnNode) {
                        newBody.add(new VarInsnNode(opcode, varInsnNode.var + descriptor.maxLocal));
                    } else {

                        if(Opcodes.IRETURN <= opcode && opcode<=Opcodes.ARETURN){
                            if (!methodToInline.multiReturn()) {
                                break;
                            }
                            newBody.add(new JumpInsnNode(GOTO,returnLabel));
                            break;
                        }
                        newBody.add(insertNode.clone(localClonedLabels));
                    }
                }
            }
        }
        return newBody;
    }

    private static @NotNull String descriptor(MethodInsnNode node) {
        return node.owner + "#" + node.name + node.desc;
    }

    ;

    public record MethodDescriptor(Type owner, String methodName, Type[] params, Type returnType, int maxLocal,
                                   int maxStack) {
        public MethodDescriptor(Type ownerType, MethodNode node) {
            this(
                    ownerType,
                    node.name,
                    Type.getArgumentTypes(node.desc),
                    Type.getReturnType(node.desc),
                    node.maxLocals,
                    node.maxStack
            );
        }
    }
}
