package asmext.analytics.controlflow;

import org.jetbrains.annotations.NotNull;

import static asmext.analytics.controlflow.NodeKindProperties.*;

/**
 * @author Zelaux
 */
public enum OutputType {
    Simple(GO_NEXT),
    IfStmt(GO_NEXT | GOTO_LABEL),
    IfLoop(GO_NEXT | GOTO_LABEL | LOOP),
    Switch(MULTI_GOTO),
    Goto(GOTO_LABEL),
    GotoLoop(GOTO_LABEL | LOOP),
    End(0);

    @NodeKindProperties
    public final int properties;
    public final int id = ordinal();

    OutputType(@NodeKindProperties int properties) {
        this.properties = properties;
    }

    public static @NotNull OutputType getNodeKind(boolean hasNext, InsnIdxSet gotoNext1, int myIndex1) {
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


    public ConnectionType connectionType(boolean merged) {
        return ConnectionType.values()[merged ? id + ConnectionType.MERGE_START : id];
    }

}
