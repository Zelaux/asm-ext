package asmext.tools.graph.layout;

import asmext.tools.graph.ui.*;
import asmext.tools.graph.ui.elem.Group;
import asmext.tools.graph.ui.opcode.OpcodePane;
import lombok.SneakyThrows;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.util.Textifier;

import java.util.ArrayList;

import static asmext.tools.graph.ui.opcode.OpcodeEntry.opcodeEntry;

public class LayoutBuilder {
    @SneakyThrows
    public static void buildLayout(Group mainGroup, ClassNode classNode, UIContext context) {
        MethodNode methodNode = classNode.methods.get(0);
        buildLayout(mainGroup, classNode, methodNode,context);
        mainGroup.layout(context);
    }

    public static void buildLayout(Group mainGroup, ClassNode classNode, MethodNode methodNode, UIContext context) throws AnalyzerException {


        ControlFlowAnalyzer analyzer = new ControlFlowAnalyzer();
        analyzer.analyze(classNode.name, methodNode);
        var nodes = analyzer.getNodes();
        context.maxIndex=nodes.length;
        ArrayList<NodeGroup> groups = new ArrayList<>();
        {
            int prevStart = 0;
            for (int i = 0; i < nodes.length; i++) {
                ControlFlowNode node = nodes[i];
                NodeKind kind = node.kind();
                if (kind == NodeKind.Simple) continue;
                int endIndex = !kind.isMerge() ? i + 1 : i;

                ControlFlowNode[] groupNodes = new ControlFlowNode[endIndex - prevStart];
                for (int j = prevStart, groupI = 0; j < endIndex; j++, groupI++) {
                    groupNodes[groupI] = nodes[j];
                }
                groups.add(new NodeGroup(
                        nodes[endIndex - 1].simpleNext,
                        kind,
                        groupNodes,
                        nodes[endIndex - 1].gotoNext.copy(),
                        nodes[prevStart].previous.copy()
                ));
                prevStart=endIndex;
                i = endIndex;
            }
        }

        int y = 0;
        Textifier textifier = new Textifier();
        for (NodeGroup group : groups) {
            var pane = new OpcodePane(0, y);
            for (ControlFlowNode insnNode : group.nodes) {
                pane.add(opcodeEntry(insnNode, textifier));
            }

            pane.layout(context);


            y += pane.height + 10;
            mainGroup.add(pane);
        }
//        makeGroup(mainGroup, 0, nodes, y, textifier);
//        for (AbstractInsnNode node : methodNode.instructions) {
//            group.add(opcodeEntry(node, textifier).fillX(true));
//        }
    }

}
