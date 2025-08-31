package asmext.tools.graph.layout;

import static asmext.tools.graph.layout.NodeKindProperties.*;

public enum FullNodeKind {
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

    public final FullNodeKind nonMergeVariant;
    @NodeKindProperties
    public final int properties;

    FullNodeKind(FullNodeKind nonMergeVariant, @NodeKindProperties int properties) {
        this.nonMergeVariant = nonMergeVariant;
        this.properties = properties;
    }

    FullNodeKind(@NodeKindProperties int properties) {
        this(null, properties);
    }


    FullNodeKind(FullNodeKind variant) {
        this(variant, variant.properties);
    }

    public boolean isMerge() {
        return ordinal() >= MERGE_START;
    }

    public FullNodeKind merged() {
        if (nonMergeVariant != null) return this;
        return FullNodeKind.values()[ordinal() + MERGE_START];
    }

    public NodeKind nonMerged() {
        return NodeKind.values()[(nonMergeVariant != null ? nonMergeVariant : this).ordinal()];
    }
}
