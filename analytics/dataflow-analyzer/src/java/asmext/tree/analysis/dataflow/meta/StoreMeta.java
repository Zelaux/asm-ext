package asmext.tree.analysis.dataflow.meta;

import asmext.tree.analysis.dataflow.interpreter.MetaDataKey;
import org.objectweb.asm.tree.VarInsnNode;

/**
 * Marks that this specific marked value will be stored in a local variable.
 */
public record StoreMeta(VarInsnNode insn, int index) {
    public static final MetaDataKey<StoreMeta> meta = MetaDataKey.create();
}
