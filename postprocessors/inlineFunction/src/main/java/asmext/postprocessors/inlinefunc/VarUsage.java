package asmext.postprocessors.inlinefunc;

import asmext.postprocessors.inlinefunc.struct.IntSeq;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PUBLIC, makeFinal = true)
@AllArgsConstructor
public class VarUsage {
    int index;
    IntSeq load = new IntSeq();
    IntSeq store = new IntSeq();
}
