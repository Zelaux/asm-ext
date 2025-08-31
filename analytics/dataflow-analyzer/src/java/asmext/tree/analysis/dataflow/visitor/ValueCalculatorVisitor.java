package asmext.tree.analysis.dataflow.visitor;

import asmext.tree.analysis.dataflow.value.*;
import com.github.asmext.tree.analysis.dataflow.value.*;

public interface ValueCalculatorVisitor<RETURN_TYPE> {
    RETURN_TYPE visit(MergedDataFlowValue value);

    RETURN_TYPE visit(BaseDataFlowValue.ReturnValue value);

    RETURN_TYPE visit(BaseDataFlowValue.ParameterValue value);

    RETURN_TYPE visit(CommonDataFlowValue value);

    RETURN_TYPE visit(NotADataFlowValue value);

    RETURN_TYPE visit(SizedCommonDataFlowValue value);
}
