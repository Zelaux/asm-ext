package com.github.asmext.tree.analysis.dataflow.meta;

import com.github.asmext.tree.analysis.dataflow.DupType;
import com.github.asmext.tree.analysis.dataflow.interpreter.MetaDataKey;
import org.intellij.lang.annotations.MagicConstant;

public record DupMeta(DupType type, DupType.ObjectKind kind, @MagicConstant(intValues = {0, 1}) int index) {
    public static final MetaDataKey<DupMeta> meta = MetaDataKey.create();

}
