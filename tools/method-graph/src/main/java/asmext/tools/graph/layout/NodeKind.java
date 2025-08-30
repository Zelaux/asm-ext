package asmext.tools.graph.layout;

import static asmext.tools.graph.layout.NodeKindProperties.*;

public enum NodeKind {
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

    public final NodeKind nonMergeVariant;
    @NodeKindProperties
    public final int properties;

    NodeKind(NodeKind nonMergeVariant, @NodeKindProperties int properties) {
        this.nonMergeVariant = nonMergeVariant;
        this.properties = properties;
    }

    NodeKind(@NodeKindProperties int properties) {
        this(null, properties);
    }


    NodeKind(NodeKind variant) {
        this(variant, variant.properties);
    }

    public boolean isMerge() {
        return ordinal() >= MERGE_START;
    }

    public NodeKind merged() {
        return NodeKind.values()[ordinal() + MERGE_START];
    }
}
