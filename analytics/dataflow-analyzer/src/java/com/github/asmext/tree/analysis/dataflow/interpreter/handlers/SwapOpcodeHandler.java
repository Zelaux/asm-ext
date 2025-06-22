package com.github.asmext.tree.analysis.dataflow.interpreter.handlers;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.Value;

public interface SwapOpcodeHandler<T extends Value> {
    T swapOpcode(AbstractInsnNode insn, boolean first, T value1, T value2) throws AnalyzerException;
}
