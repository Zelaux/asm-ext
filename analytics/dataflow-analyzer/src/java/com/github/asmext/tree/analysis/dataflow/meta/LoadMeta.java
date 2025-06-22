package com.github.asmext.tree.analysis.dataflow.meta;

import com.github.asmext.tree.analysis.dataflow.interpreter.MetaDataKey;
import org.objectweb.asm.tree.VarInsnNode;

public record LoadMeta(VarInsnNode insn, int index) {
    public static final MetaDataKey<LoadMeta> meta = MetaDataKey.create();
}
