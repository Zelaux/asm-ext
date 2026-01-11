package asmext.postprocessors.inlinefunc;

import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LocalVariableNode;

import java.util.List;

public class ResultMethod {
    public InsnList body = new InsnList();
    public int maxLocals=0;
    public int maxStack=0;
    List<LocalVariableNode> localVariables;
}
