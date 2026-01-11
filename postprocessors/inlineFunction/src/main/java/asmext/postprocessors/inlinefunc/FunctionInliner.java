package asmext.postprocessors.inlinefunc;

import asmext.postprocessors.inlinefunc.struct.IntSeq;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.Frame;
import org.objectweb.asm.tree.analysis.SourceInterpreter;
import org.objectweb.asm.tree.analysis.SourceValue;

import java.util.Collection;
import java.util.HashMap;

public class FunctionInliner implements Opcodes {
    private static final InsnList MOCK_LIST = new InsnList();
    private static boolean debug = true;

    @SneakyThrows
    public static InsnList inline(MethodDescriptor descriptor, HashMap<String, MethodToInline> inlineMethods) {
        var array = descriptor.instructions().toArray();
        InsnList newBody = new InsnList();

        int newMaxLocal = 0;
        int newMaxStack = 0;
        var globalLabels = new HashMap<LabelNode, LabelNode>();
        var frames = new Analyzer<>(new SourceInterpreter()).analyze(descriptor.owner.getInternalName(), descriptor.node);
        LineNumberNode prevLineNumber = null;
        for (var insnIndex = 0; insnIndex < frames.length; insnIndex++) {
            var rawNode = array[insnIndex];
            if (rawNode.getType() == AbstractInsnNode.LINE) prevLineNumber = (LineNumberNode) rawNode;
            if (rawNode instanceof LabelNode labelNode) globalLabels.putIfAbsent(labelNode, new LabelNode());
            newBody.add(rawNode.clone(globalLabels));
            if (rawNode.getOpcode() != INVOKESTATIC) continue;
            var node = (MethodInsnNode) rawNode;
            var methodToInline = inlineMethods.get(descriptor(node));

            if (methodToInline == null) continue;
            var frame = frames[insnIndex];
            {
                newBody.remove(newBody.getLast());
                var size = newBody.size();
                if (debug) {
                    newBody.add(new LdcInsnNode(methodToInline.name));
                    newBody.add(new InsnNode(POP));
                }
                var arguments = methodToInline.params();
                var lastStack = frame.getStackSize() - 1;
                int[] argumentMapper = new int[arguments.length];
                int savedArgumentCounter = 0;
                for (int i = 0; i < arguments.length; i++) {
                    if (methodToInline.localUsage[i].store.size == 0) {
                        var local = frame.getStack(lastStack - i);
                        if (local.insns.size() == 1) {
                            var first = local.insns.iterator().next();
                            if (first.getType() == AbstractInsnNode.VAR_INSN) {
                                var index1 = MOCK_LIST.indexOf(first);

                                newBody.remove(newBody.get(size - insnIndex + index1));
                                argumentMapper[i] = ((VarInsnNode) first).var;
                                continue;
                            }
                        }
                    }
                    Type argument = arguments[i];
                    newBody.add(new VarInsnNode(argument.getOpcode(Opcodes.ISTORE), argumentMapper[i] = (savedArgumentCounter++) + descriptor.maxLocal));
                }
                HashMap<LabelNode, LabelNode> localClonedLabels = new HashMap<>();
                for (LabelNode label : methodToInline.labels()) {
                    localClonedLabels.put(label, new LabelNode());
                }
                LabelNode returnLabel = new LabelNode();

                for (AbstractInsnNode insertNode : methodToInline.body) {
                    int opcode = insertNode.getOpcode();
                    if (insertNode instanceof VarInsnNode varInsnNode) {
                        newBody.add(new VarInsnNode(opcode, argumentMapper[varInsnNode.var]));
                    } else if(insertNode instanceof IincInsnNode inc){
                        newBody.add(new IincInsnNode(argumentMapper[inc.var],inc.incr));
                    }else {

                        if (Opcodes.IRETURN <= opcode && opcode <= Opcodes.ARETURN) {
                            if (!methodToInline.multiReturn()) {
                                break;
                            }
                            newBody.add(new JumpInsnNode(GOTO, returnLabel));
                            break;
                        }
                        newBody.add(insertNode.clone(localClonedLabels));
                    }
                }
                newBody.add(returnLabel);
                if (prevLineNumber != null) newBody.add(new LineNumberNode(prevLineNumber.line, returnLabel));

            }
        }
        return newBody;
    }

    private static @NotNull String descriptor(MethodInsnNode node) {
        return node.owner + "#" + node.name + node.desc;
    }

    public static MethodDescriptor makeDescriptor(Type ownerType, MethodNode node) {
        return new MethodDescriptor(ownerType, node);
    }

    ;

    public record MethodDescriptor(Type owner, String methodName, Type[] params, Type returnType, int maxLocal,
                                   int maxStack, MethodNode node) {
        public MethodDescriptor(Type ownerType, MethodNode node) {
            this(
                    ownerType,
                    node.name,
                    Type.getArgumentTypes(node.desc),
                    Type.getReturnType(node.desc),
                    node.maxLocals,
                    node.maxStack,
                    node
            );
        }

        public InsnList instructions() {return node.instructions;}
    }
}
