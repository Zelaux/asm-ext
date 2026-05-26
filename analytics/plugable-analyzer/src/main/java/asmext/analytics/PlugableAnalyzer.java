package asmext.analytics;

import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.*;

import java.util.ArrayList;

/**
 * {@inheritDoc}
 *
 * @author Zelaux
 * @since 2025-09
 */
public class PlugableAnalyzer<V extends Value> extends Analyzer<V> {
    public final Interpreter<V> originalInterpreter;
    public final ArrayList<AnalyzerPlugin<V>> allPlugins = new ArrayList<>();
    protected final ArrayList<AnalyzerPlugin<V>>[] sortedPlugins = initPlugins();
protected AbstractInsnNode[] currentInstructions;
    /**
     * Constructs a new {@link Analyzer}.
     *
     * @param interpreter the interpreter to use to symbolically interpret the bytecode instructions.
     */
    public PlugableAnalyzer(Interpreter<V> interpreter) {
        super(interpreter);
        this.originalInterpreter = interpreter;
    }

    private static <V extends Value> ArrayList<AnalyzerPlugin<V>> @NotNull [] initPlugins() {
        //noinspection unchecked
        ArrayList<AnalyzerPlugin<V>>[] arrayLists = new ArrayList[PluginUsage.allAmount];
        for (int i = 0; i < arrayLists.length; i++) {
            arrayLists[i] = new ArrayList<>(0);
        }
        return arrayLists;
    }

    @Override
    public Frame<V>[] analyze(String owner, MethodNode method) throws AnalyzerException {
        for (AnalyzerPlugin<V> plugin : pluginsFor(PluginUsage.beforeAnalyze))
            plugin.beforeAnalyze(this, owner, method);
        currentInstructions=method.instructions.toArray();
        Frame<V>[] analyzedFrames = super.analyze(owner, method);
        for (AnalyzerPlugin<V> plugin : pluginsFor(PluginUsage.afterAnalyze))
            plugin.afterAnalyze(this, owner, method, analyzedFrames);
        return analyzedFrames;
    }

    public <Type extends Interpreter<V>> Type originalInterpreter() {
        //noinspection unchecked
        return (Type) originalInterpreter;
    }

    public PlugableAnalyzer<V> plugins(@NonNull AnalyzerPlugin<? super V>... plugins) {
        for (AnalyzerPlugin<? super V> plugin0 : plugins) {
            //noinspection unchecked,rawtypes
            AnalyzerPlugin<V> plugin = ((AnalyzerPlugin) plugin0);
            allPlugins.add(plugin);
            for (PluginUsage usage : plugin.requirements) {
                sortedPlugins[usage.id].add(plugin);
            }
        }
        return this;
    }

    @Override
    protected PlugableAnalyzerFrame<V> newFrame(int numLocals, int numStack) {
        PlugableAnalyzerFrame<V> frame = new PlugableAnalyzerFrame<>(numLocals, numStack, this);
        for (AnalyzerPlugin<V> plugin : allPlugins) plugin.initialFrame(this, frame);
        return frame;
    }

    @Override
    protected PlugableAnalyzerFrame<V> newFrame(Frame<? extends V> frame) {
        return new PlugableAnalyzerFrame<>(frame, this);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    protected void newControlFlowEdge(int insnIndex, int successorIndex) {
        for (AnalyzerPlugin<V> plugin : pluginsFor(PluginUsage.controlFlowEdge)) {
            plugin.newControlFlowEdge(this, insnIndex, successorIndex);
        }
        Frame<V>[] frames = getFrames();
        castFrame(frames[successorIndex]).setIndex(successorIndex);
        castFrame(frames[insnIndex]).setIndex(insnIndex);
        super.newControlFlowEdge(insnIndex, successorIndex);
    }

    public PlugableAnalyzerFrame<V> castFrame(Frame<V> frame) {
        return (PlugableAnalyzerFrame<V>) frame;
    }

    public ArrayList<AnalyzerPlugin<V>> pluginsFor(PluginUsage pluginUsage) {
        return sortedPlugins[pluginUsage.id];
    }
}
