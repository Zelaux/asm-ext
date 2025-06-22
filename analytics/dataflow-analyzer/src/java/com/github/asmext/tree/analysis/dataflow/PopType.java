package com.github.asmext.tree.analysis.dataflow;

import lombok.AllArgsConstructor;
import org.objectweb.asm.Opcodes;

@AllArgsConstructor
public enum PopType {
    POP_1(Opcodes.POP, 1, 1),
    POP_2(Opcodes.POP2, 1, 2),
    POP_11(Opcodes.POP2, 2, 1),
    ;
    public final int opcode;
    public final int amount;
    public final int sizeEach;
}
