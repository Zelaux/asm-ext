package com.github.asmext.tree.analysis.dataflow.meta;

import com.github.asmext.tree.analysis.dataflow.DupType;
import com.github.asmext.tree.analysis.dataflow.interpreter.DataFlowInterpreter;
import com.github.asmext.tree.analysis.dataflow.interpreter.MetaDataKey;
import com.github.asmext.tree.analysis.dataflow.interpreter.handlers.DupOpcodeHandler;
import com.github.asmext.tree.analysis.dataflow.value.DataFlowValue;
import org.intellij.lang.annotations.MagicConstant;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.analysis.Value;

/**
 * Captures how a specific value participates in a JVM stack duplication instruction
 * such as {@code DUP}, {@code DUP_X1}, {@code DUP2}, and related opcodes.
 *
 *
 * @param type  the duplication instruction type, see {@link DupType}
 * @param kind  the role of the value in the duplication pattern, see {@link DupType.ObjectKind}
 * @param index the index of the value within the duplication group (0 or 1). Typically used
 *              when a value appears more than once (e.g., original and duplicate).
 *
 * @see DupType
 * @see DupType.ObjectKind
 * @see DataFlowInterpreter#dupOperation(AbstractInsnNode, DupType, DataFlowValue, DupType.ObjectKind, int)
 * @see DupOpcodeHandler#dupOperation(AbstractInsnNode, DupType, Value, DupType.ObjectKind, int)
 */
public record DupMeta(DupType type, DupType.ObjectKind kind, @MagicConstant(intValues = {0, 1}) int index) {
    public static final MetaDataKey<DupMeta> meta = MetaDataKey.create();
}
