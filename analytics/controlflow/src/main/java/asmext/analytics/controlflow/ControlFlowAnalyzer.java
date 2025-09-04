package asmext.analytics.controlflow;

import asmext.analytics.PlugableAnalyzer;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.SourceInterpreter;
import org.objectweb.asm.tree.analysis.SourceValue;

import java.util.ArrayList;

/**
 * @author Zelaux
 */
public class ControlFlowAnalyzer {

    private final ControlFlowPlugin controlFlowPlugin = new ControlFlowPlugin();
    private final PlugableAnalyzer<SourceValue> analyzer = new PlugableAnalyzer<SourceValue>(new SourceInterpreter())
            .plugins(controlFlowPlugin);

    public ControlFlowAnalyzer() {
        super();
    }

    public ControlFlowNode[] analyze(String owner, MethodNode method) throws AnalyzerException {

        this.analyzer.analyze(owner, method);
        return controlFlowPlugin.nodes();
    }

    public ArrayList<ControlFlowBlock> collectGroups() {
        return controlFlowPlugin.collectGroups();
    }

}
