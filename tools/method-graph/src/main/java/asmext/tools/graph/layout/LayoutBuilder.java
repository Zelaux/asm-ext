package asmext.tools.graph.layout;

import asmext.analytics.controlflow.ControlFlowAnalyzer;
import asmext.analytics.controlflow.ControlFlowBlock;
import asmext.tools.graph.ui.UIContext;
import asmext.tools.graph.ui.elem.Element;
import asmext.tools.graph.ui.elem.Group;
import asmext.tools.graph.ui.layout.LayoutPlacement;
import asmext.tools.graph.ui.opcode.OpcodeEntry;
import asmext.tools.graph.ui.opcode.groups.DoubleBranch;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.util.Textifier;

import java.util.ArrayList;

import static asmext.tools.graph.layout.OldLayoutBuilder.paneForGroup;

public class LayoutBuilder {
    public static void buildLayout(Group mainGroup, ClassNode classNode, MethodNode methodNode, UIContext context) throws AnalyzerException {
        var analyzer = new ControlFlowAnalyzer();
        var nodes = analyzer.analyze(classNode.name, methodNode);

        context.maxIndex = nodes.length;
        context.everyEntry = new OpcodeEntry[nodes.length];

        ArrayList<ControlFlowBlock> groups = analyzer.collectGroups();
        Group group = newVerticalGroup();
        mainGroup.add(group);
        Textifier textifier = new Textifier();
        AlreadyBuiltEntry[] alreadyBuilt = new AlreadyBuiltEntry[groups.size()];
        final boolean centered = context.useHorizontalMode;
        for (int[] i = {0}; i[0] < groups.size(); i[0]++) {
            var group1 = buildGroup(groups, i, textifier, alreadyBuilt, centered);

            group.add(group1.centered(centered));
        }

        mainGroup.layout(context);


    }

    private static Element buildGroup(ArrayList<ControlFlowBlock> groups, int[] i, Textifier textifier, AlreadyBuiltEntry[] alreadyBuilt, boolean centered) {
//        Group out = new Group(LayoutPlacement.Vertical);
//        out.containerLayout.childGap = 10;
        int index = i[0];
        if (index >= groups.size()) return null;
        ControlFlowBlock nodeGroup = groups.get(index);
        Element element = switch (nodeGroup.kind.outputType()) {
            case Simple -> paneForGroup(nodeGroup, textifier).centered(centered);
            case IfStmt -> {
                i[0]++;
                if (alreadyBuilt[i[0]] != null) {
                    Element element1 = paneForGroup(nodeGroup, textifier);
                    yield element1.centered(centered);
                }

                Group group = newVerticalGroup();
                Element element2 = paneForGroup(nodeGroup, textifier);
                group.add(element2.centered(centered));

                var left = buildGroup(groups, i, textifier, alreadyBuilt, centered);
                i[0]++;
                if (alreadyBuilt[i[0]] != null) {
                    group.add(left);
                    yield group;
                }
                var right = buildGroup(groups, i, textifier, alreadyBuilt, centered);

                group.add(new DoubleBranch(left, right).centered(centered));
                yield group;
            }
            case IfLoop -> throw new UnsupportedOperationException();
            case Switch -> throw new UnsupportedOperationException();
            case Goto -> paneForGroup(nodeGroup, textifier).centered(centered);
            case GotoLoop -> paneForGroup(nodeGroup, textifier).centered(centered);
            case End -> paneForGroup(nodeGroup, textifier).centered(centered);

        };
        alreadyBuilt[index] = new AlreadyBuiltEntry(element, i[0] - index);
        return element;

    }

    private static @NotNull Group newVerticalGroup() {
        Group group = new Group(LayoutPlacement.Vertical);
        group.containerLayout.childGap = 10;
        return group;
    }

    record AlreadyBuiltEntry(Element element, int indexOffset) {}
}
