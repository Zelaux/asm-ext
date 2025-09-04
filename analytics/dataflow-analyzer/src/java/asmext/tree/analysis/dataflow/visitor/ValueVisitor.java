package asmext.tree.analysis.dataflow.visitor;

import asmext.tree.analysis.dataflow.value.*;

public interface ValueVisitor {
    void visit(MergedDataFlowValue value);

    void visit(BaseDataFlowValue.ReturnValue value);

    void visit(BaseDataFlowValue.ParameterValue value);

    void visit(CommonDataFlowValue value);

    void visit(NotADataFlowValue value);

    void visit(SizedCommonDataFlowValue value);
}
