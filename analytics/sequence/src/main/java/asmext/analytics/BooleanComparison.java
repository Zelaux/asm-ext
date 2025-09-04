package asmext.analytics;


public record BooleanComparison(
        int ifStmtIdx,
        int trueBranchStart, int trueBranchEnd, int trueValueIdx,
        int falseBranchStart, int falseBranchEnd, int falseValueIdx,
        int afterIfIdx) {
    @Override
    public String toString() {
        return String.format("%d [%d; %d] [%d; %d] %d", ifStmtIdx, trueBranchStart, trueBranchEnd, falseBranchStart, falseBranchEnd, afterIfIdx);
    }

    public enum Type {
        Equal,
        NotEqual,
        Greater,
        GreaterThan,
        Less,
        LessThan,
        IsNull,
        IsNotNull,

    }
}
