package asmext.tools.graph.layout;

import asmext.tools.graph.ui.UIContext;
import asmext.tools.graph.ui.elem.Element;
import asmext.tools.graph.ui.elem.Group;
import asmext.tools.graph.ui.layout.LayoutDirection;
import asmext.tools.graph.ui.opcode.groups.DoubleBranch;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.util.Textifier;

import java.util.ArrayList;

import static asmext.tools.graph.layout.OldLayoutBuilder.paneForGroup;

public class LayoutBuilder {
    public static void buildLayout(Group mainGroup, ClassNode classNode, MethodNode methodNode, UIContext context) throws AnalyzerException {
        ControlFlowAnalyzer analyzer = new ControlFlowAnalyzer();
        analyzer.analyze(classNode.name, methodNode);
        var nodes = analyzer.getNodes();
        context.maxIndex = nodes.length;
        ArrayList<NodeGroup> groups = OldLayoutBuilder.makeGroups(nodes);
        Group group = new Group(0, 0, 0, 0, LayoutDirection.Vertical);
        mainGroup.add(group);
        Textifier textifier = new Textifier();
        group.containerLayout.childGap = 10;
        for (int[] i = {0}; i[0] < groups.size(); i[0]++) {
            var group1 = buildGroup(groups, i, textifier);
            group.add(group1.centered());
        }

        mainGroup.layout(context);


    }

    private static Element buildGroup(ArrayList<NodeGroup> groups, int[] i, Textifier textifier) {
//        Group out = new Group(LayoutDirection.Vertical);
//        out.containerLayout.childGap = 10;
        for (; i[0] < groups.size(); i[0]++) {
            NodeGroup group = groups.get(i[0]);
            switch (group.kind.nonMerged()) {
                case Simple -> {
                    //Second branch in if stmt
//                    if (!group.kind.isMerge())
//                        throw new IllegalStateException("Unexpected value: " + group.kind);

                    return paneForGroup(group, textifier).centered();
                }
                case IfStmt -> {
                    Group group1 = new Group(LayoutDirection.Vertical);
                    i[0]++;
                    var left = buildGroup(groups, i, textifier);
                    i[0]++;
                    var right = buildGroup(groups, i, textifier);

                    group1.add(paneForGroup(group,textifier).centered());
                    group1.add(new DoubleBranch(left, right).centered());
                    return group1;
                }
                case IfLoop -> {
                    //TODO
                    throw new UnsupportedOperationException();
                }
                case Switch -> {
                    //TODO
                    throw new UnsupportedOperationException();
                }
                case Goto -> {
                    return paneForGroup(group, textifier).centered();
                }
                case GotoLoop -> {
                    return paneForGroup(group, textifier).centered();
                }
                case End -> {
                    return paneForGroup(group, textifier).centered();
                }
            }
        }
        return null;
    }
}
