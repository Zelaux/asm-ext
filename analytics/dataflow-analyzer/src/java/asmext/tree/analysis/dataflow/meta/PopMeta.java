package asmext.tree.analysis.dataflow.meta;

import asmext.tree.analysis.dataflow.PopType;
import asmext.tree.analysis.dataflow.interpreter.MetaDataKey;
import org.objectweb.asm.tree.InsnNode;
/**
 * Represents metadata describes how a value is consumed by a JVM stack {@code POP} or {@code POP2} instruction
 *
 * @param insn  the instruction performing the pop
 * @param type  the type of pop operation
 * @param index the index of the value in the context of the pop operation (e.g., 0 for top of stack)
 *
 * @see PopType
 */
public record PopMeta(InsnNode insn, PopType type, int index) {
    public static final MetaDataKey<PopMeta> meta = MetaDataKey.create();
}
