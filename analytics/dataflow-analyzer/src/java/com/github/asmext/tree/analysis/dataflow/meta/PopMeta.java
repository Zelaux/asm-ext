package com.github.asmext.tree.analysis.dataflow.meta;

import com.github.asmext.tree.analysis.dataflow.PopType;
import com.github.asmext.tree.analysis.dataflow.interpreter.MetaDataKey;
import org.objectweb.asm.tree.InsnNode;

public record PopMeta(InsnNode insn, PopType type, int index) {
    public static final MetaDataKey<PopMeta> meta = MetaDataKey.create();
}
