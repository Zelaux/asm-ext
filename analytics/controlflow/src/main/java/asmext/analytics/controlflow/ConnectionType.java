package asmext.analytics.controlflow;

import static asmext.analytics.controlflow.NodeKindProperties.*;
/**
 * @author Zelaux
 * */
public enum ConnectionType {
    Simple(GO_NEXT),
    IfStmt(GO_NEXT | GOTO_LABEL),
    IfLoop(GO_NEXT | GOTO_LABEL | LOOP),
    Switch(MULTI_GOTO),
    Goto(GOTO_LABEL),
    GotoLoop(GOTO_LABEL | LOOP),
    End(0),
    MergePoint(Simple),
    MergeIf(IfStmt),
    MergeIfLoop(IfLoop),
    MergeSwitch(Switch),
    MergeGoto(Goto),
    MergeGotoLoop(GotoLoop),
    MergeEnd(End);
    public static final int MERGE_START = MergePoint.ordinal();

    public final ConnectionType nonMergeVariant;
    @NodeKindProperties
    public final int properties;

    ConnectionType(ConnectionType nonMergeVariant, @NodeKindProperties int properties) {
        this.nonMergeVariant = nonMergeVariant;
        this.properties = properties;
    }

    ConnectionType(@NodeKindProperties int properties) {
        this(null, properties);
    }


    ConnectionType(ConnectionType variant) {
        this(variant, variant.properties);
    }

    public boolean isMerge() {
        return ordinal() >= MERGE_START;
    }


    public OutputType outputType() {
        return OutputType.values()[(nonMergeVariant != null ? nonMergeVariant : this).ordinal()];
    }
}
