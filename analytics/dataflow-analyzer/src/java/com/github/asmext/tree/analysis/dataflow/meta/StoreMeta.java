package com.github.asmext.tree.analysis.dataflow.meta;

import com.github.asmext.tree.analysis.dataflow.interpreter.MetaDataKey;
import org.objectweb.asm.tree.VarInsnNode;

public record StoreMeta(VarInsnNode insn, int index) {
    public static final MetaDataKey<StoreMeta> meta = MetaDataKey.create();
}
