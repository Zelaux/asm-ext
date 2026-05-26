package asmext.analytics;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.Frame;
import org.objectweb.asm.tree.analysis.Interpreter;
import org.objectweb.asm.tree.analysis.Value;

/**
 * {@inheritDoc}
 *
 * @author Zelaux
 * @since 2025-09
 */
public class PlugableAnalyzerFrame<V extends Value> extends Frame<V> {
    public final PlugableAnalyzer<V> owner;
    @Setter
    @Getter
    private int index = -1;

    public PlugableAnalyzerFrame(int numLocals, int maxStack,@NonNull PlugableAnalyzer<V> owner) {
        super(numLocals, maxStack);
        this.owner = owner;
    }

    public PlugableAnalyzerFrame(Frame<? extends V> frame,@NonNull PlugableAnalyzer<V> owner) {
        super(frame);
        this.owner = owner;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initJumpTarget(int opcode, LabelNode target) {
        super.initJumpTarget(opcode, target);
        PluginUsage frameInitJump = PluginUsage.frameInitJump;
        for (AnalyzerPlugin<V> plugin : owner.pluginsFor(frameInitJump)) {
            plugin.initJumpTarget(owner, this, opcode, target);
        }
    }

    @Override
    public void execute(AbstractInsnNode insn, Interpreter<V> interpreter) throws AnalyzerException {
        var plugins = owner.pluginsFor(PluginUsage.frameExecute);
        if (!plugins.isEmpty()) {
            for (AnalyzerPlugin<V> analyzerPlugin : plugins) {
                analyzerPlugin.beforeExecute(owner, this, insn);
            }
        }
        executeWithoutPlugins(insn, interpreter);
        if (!plugins.isEmpty()) {
            for (AnalyzerPlugin<V> analyzerPlugin : plugins) {
                analyzerPlugin.afterExecute(owner, this, insn);
            }
        }
    }

    public void executeWithoutPlugins(AbstractInsnNode insn, Interpreter<V> interpreter) throws AnalyzerException {
        super.execute(insn, interpreter);
    }

    @Override
    public boolean merge(Frame<? extends V> frame, Interpreter<V> interpreter) throws AnalyzerException {
        var plugins = owner.pluginsFor(PluginUsage.frameMerge);
        for (AnalyzerPlugin<V> plugin : plugins) {
            //noinspection unchecked
            plugin.beforeMerge(owner, this, (PlugableAnalyzerFrame<V>) frame);
        }
        boolean changed = justMerge(frame, interpreter);
        for (AnalyzerPlugin<V> plugin : plugins) {
            //noinspection unchecked
            plugin.afterMerge(owner, this, (PlugableAnalyzerFrame<V>) frame, changed);
        }
        return changed;
    }

    protected boolean justMerge(Frame<? extends V> frame, Interpreter<V> interpreter) throws AnalyzerException {
        return super.merge(frame, interpreter);
    }

    public V valueOnTop() {
        return getStack(getStackSize() - 1);
    }

}
