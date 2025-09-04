package asmext.analytics;


import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Frame;
import org.objectweb.asm.tree.analysis.Value;

import java.lang.reflect.Method;
import java.util.HashMap;

/**
 * @author Zelaux
 * @since 2025-09
 */
public abstract class AnalyzerPlugin<V extends Value> {
    private static final HashMap<String, PluginUsage> methodUsageMap = requirementMap();
    public final PluginUsage[] requirements;
    public final Class<V> valueType;

    public AnalyzerPlugin(Class<V> valueType) {
        this.valueType = valueType;

        boolean[] usageMap = new boolean[PluginUsage.allAmount];
        int total = 0;
        for (Class<?> myType = getClass(); myType != AnalyzerPlugin.class; myType = myType.getSuperclass()) {
            for (Method method : myType.getDeclaredMethods()) {
                PluginUsage usage = methodUsageMap.get(fullDesc(method));
                if (usage == null) continue;
                if (usageMap[usage.id]) continue;
                total++;
                usageMap[usage.id] = true;
            }
        }
        PluginUsage[] usages = new PluginUsage[total];
        {
            int i = 0;
            PluginUsage[] values = PluginUsage.values();
            for (int id = 0; id < usageMap.length; id++) {
                if (!usageMap[id]) continue;
                usages[i++] = values[id];
            }
        }
        requirements = usages;
    }

    private static HashMap<String, PluginUsage> requirementMap() {
        Method[] methods = AnalyzerPlugin.class.getDeclaredMethods();
        var map = new HashMap<String, PluginUsage>();
        for (Method method : methods) {
            PluginMarker anno = method.getAnnotation(PluginMarker.class);
            if (anno == null) continue;
            map.put(fullDesc(method), anno.value());
        }
        return map;
    }

    private static String fullDesc(Method method) {
        return method.getName() + " " + Type.getType(method).getDescriptor();
    }


    @PluginMarker(PluginUsage.initFrame)
    public void initialFrame(PlugableAnalyzer<V> analyzer, PlugableAnalyzerFrame<V> frame) {}

    @PluginMarker(PluginUsage.frameInitJump)
    public void initJumpTarget(PlugableAnalyzer<V> analyzer, PlugableAnalyzerFrame<V> frame, int opcode, LabelNode target) {}

    @PluginMarker(PluginUsage.frameExecute)
    public void afterExecute(PlugableAnalyzer<V> analyzer, PlugableAnalyzerFrame<V> frame, AbstractInsnNode insn) {}

    @PluginMarker(PluginUsage.frameExecute)
    public void beforeExecute(PlugableAnalyzer<V> analyzer, PlugableAnalyzerFrame<V> frame, AbstractInsnNode insn) {}

    @PluginMarker(PluginUsage.controlFlowEdge)
    public void newControlFlowEdge(PlugableAnalyzer<V> analyzer, int insnIndex, int successorIndex) {}

    @PluginMarker(PluginUsage.frameMerge)
    public void beforeMerge(PlugableAnalyzer<V> analyzer,
                            PlugableAnalyzerFrame<? extends V> thisFrame,
                            PlugableAnalyzerFrame<? extends V> otherFrame) {}

    @PluginMarker(PluginUsage.frameMerge)
    public void afterMerge(PlugableAnalyzer<V> analyzer,
                           PlugableAnalyzerFrame<? extends V> thisFrame,
                           PlugableAnalyzerFrame<? extends V> otherFrame, boolean changed) {}

    @PluginMarker(PluginUsage.beforeAnalyze)
    public void beforeAnalyze(PlugableAnalyzer<V> analyzer, String owner, MethodNode method) {}

    @PluginMarker(PluginUsage.afterAnalyze)
    public void afterAnalyze(PlugableAnalyzer<V> analyzer, String owner, MethodNode method, Frame<V>[] analyzed) {}
}
