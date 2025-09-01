package asmext.tools.graph.layout;

import asmext.analytics.controlflow.ControlFlowAnalyzer;
import asmext.analytics.controlflow.ControlFlowBlock;
import asmext.analytics.controlflow.ControlFlowNode;
import asmext.tools.graph.ui.UIContext;
import asmext.tools.graph.ui.elem.Group;
import asmext.tools.graph.ui.opcode.OpcodePane;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.util.Textifier;

import java.util.ArrayList;

import static asmext.tools.graph.ui.opcode.OpcodeEntry.opcodeEntry;

public class OldLayoutBuilder {
    @SneakyThrows
    public static void buildLayout(Group mainGroup, ClassNode classNode, UIContext context) {
        MethodNode methodNode = classNode.methods.get(0);
        buildLayout(mainGroup, classNode, methodNode, context);
        mainGroup.layout(context);
    }

    public static void buildLayout(Group mainGroup, ClassNode classNode, MethodNode methodNode, UIContext context) throws AnalyzerException {


        var analyzer = new ControlFlowAnalyzer();
        var nodes = analyzer.analyze(classNode.name, methodNode);
        context.maxIndex = nodes.length;
        ArrayList<ControlFlowBlock> groups = analyzer.collectGroups();

        int y = 0;
        Textifier textifier = new Textifier();
        for (ControlFlowBlock group : groups) {
            var pane = paneForGroup(group, textifier);
            pane.y = y;

            pane.layout(context);


            y += pane.height + 10;
            mainGroup.add(pane);
        }
//        makeGroup(mainGroup, 0, nodes, y, textifier);
//        for (AbstractInsnNode node : methodNode.instructions) {
//            group.add(opcodeEntry(node, textifier).fillX(true));
//        }
    }

    static @NotNull OpcodePane paneForGroup(ControlFlowBlock group, Textifier textifier) {
        var pane = new OpcodePane(0, 0);
        for (ControlFlowNode insnNode : group.nodes) {
            pane.add(opcodeEntry(insnNode, textifier));
        }
        return pane;
    }

}
