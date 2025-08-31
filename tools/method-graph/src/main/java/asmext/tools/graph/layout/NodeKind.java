package asmext.tools.graph.layout;

import asmext.tools.graph.util.IntSet;
import org.jetbrains.annotations.NotNull;

import static asmext.tools.graph.layout.NodeKindProperties.*;

public enum NodeKind {
    Simple(GO_NEXT),
    IfStmt(GO_NEXT | GOTO_LABEL),
    IfLoop(GO_NEXT | GOTO_LABEL | LOOP),
    Switch(MULTI_GOTO),
    Goto(GOTO_LABEL),
    GotoLoop(GOTO_LABEL | LOOP),
    End(0);

    @NodeKindProperties
    public final int properties;

    NodeKind(@NodeKindProperties int properties) {
        this.properties = properties;
    }

    public static @NotNull NodeKind getNodeKind(boolean hasNext, IntSet gotoNext1, int myIndex1) {
        if (!hasNext && gotoNext1.isEmpty()) return End;
        if (hasNext && gotoNext1.isEmpty()) return Simple;
        if (hasNext && gotoNext1.isOne())
            return gotoNext1.first() >= myIndex1 ? IfStmt : IfLoop;
        int size = gotoNext1.size();
        if (!hasNext && size > 0) {
            if (size > 1) return Switch;
            return gotoNext1.first() >= myIndex1 ? Goto : GotoLoop;
        }
        throw new UnsupportedOperationException();
    }


    public FullNodeKind merged() {
        return FullNodeKind.values()[ordinal() + FullNodeKind.MERGE_START];
    }

    public FullNodeKind fullType() {
        return FullNodeKind.values()[ordinal()];
    }
}
