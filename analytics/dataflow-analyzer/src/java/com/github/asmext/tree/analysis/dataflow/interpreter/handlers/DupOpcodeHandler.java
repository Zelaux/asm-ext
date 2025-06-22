package com.github.asmext.tree.analysis.dataflow.interpreter.handlers;

import com.github.asmext.tree.analysis.dataflow.DupType;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.Value;

public interface DupOpcodeHandler<T extends Value> {
    T dupOperation(AbstractInsnNode insn, DupType dupType, T value, DupType.ObjectKind kind, int index) throws AnalyzerException;
}
