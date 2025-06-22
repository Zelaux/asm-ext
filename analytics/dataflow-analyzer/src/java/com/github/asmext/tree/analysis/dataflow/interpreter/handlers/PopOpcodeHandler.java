package com.github.asmext.tree.analysis.dataflow.interpreter.handlers;

import com.github.asmext.tree.analysis.dataflow.PopType;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.analysis.Value;

public interface PopOpcodeHandler<T extends Value> {

    void popped(AbstractInsnNode insn, T pop, PopType popType, int i);
}
