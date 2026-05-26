package asmext.analytics;

import asmext.analytics.controlflow.ControlFlowPlugin;
import asmlib.util.NodeUtil;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.SourceInterpreter;
import org.objectweb.asm.tree.analysis.SourceValue;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BooleanComparisonSearchPluginTest implements Opcodes {
    @Test
    void testFinding() throws Exception {
        var node = NodeUtil.classNode(TargetClass.class, ASM9);
        MethodNode methodNode = node.methods.get(0);
        var controlFlowPlugin = new ControlFlowPlugin();
        var plugin = BooleanComparisonSearchPlugin.sourceValue(controlFlowPlugin);
        plugin.instructions = methodNode.instructions.toArray();
        PlugableAnalyzer<SourceValue> plugin1 = new PlugableAnalyzer<>(new SourceInterpreter())
                .plugins(plugin)
                .plugins(controlFlowPlugin);
        var analyze = plugin1
                .analyze(node.name, methodNode);
        var booleanComparisons = plugin.allComparison();
        assertEquals(8, booleanComparisons.size());
        ;
    }

    static class TargetClass {
        TargetClass(int a, int b, Object c) {
            boolean x = a == b;
            System.out.println(x);
            System.out.println(a != b);
            System.out.println(a > b);
            System.out.println(a >= b);
            System.out.println(a < b);
            System.out.println(a <= b);
            System.out.println(c == null);
            System.out.println(c != null);
        }
    }
}