package asmext.tools.graph.ui;

import asmext.tools.graph.layout.ControlFlowNode;
import asmext.tools.graph.ui.opcode.OpcodeEntry;
import asmext.tools.graph.util.Utils;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

public class HighlightHandler {
    public static boolean shouldHighlight(OpcodeEntry self, OpcodeEntry hover) {
//        if (textEquality(self, hover)) return true;
//        if (labelEquality(self, hover)) return true;
        if (fieldEquality(self, hover)) return true;
        //noinspection RedundantIfStatement
        if (destinationHighlight(self, hover)) return true;
        return false;
    }

    public static boolean destinationHighlight(OpcodeEntry self, OpcodeEntry hover) {
        var selfNode = self.controlFlowNode;
        var hoverNode = hover.controlFlowNode;
        if (nearGoto(selfNode, hoverNode)) return true;
        if (nearGoto(hoverNode, selfNode)) return true;
        return false;
    }

    public static boolean nearGoto(ControlFlowNode selfNode, ControlFlowNode otherNode) {
        int otherIndex = otherNode.myIndex;
        if (selfNode.previous.size() > 1 && selfNode.previous.contains(otherIndex)) return true;
        if (selfNode.gotoNext.contains(otherIndex)) return true;
        if (selfNode.node instanceof JumpInsnNode jumpNode && jumpNode.getOpcode() != Opcodes.GOTO) {
            if (selfNode.simpleNext == otherIndex) return true;
        }
        return false;
    }

    public static boolean fieldEquality(OpcodeEntry self, OpcodeEntry hover) {
        return hover.insnNode instanceof VarInsnNode otherVar && self.insnNode instanceof VarInsnNode thisVar && otherVar.var == thisVar.var;
    }

    public static boolean labelEquality(OpcodeEntry self, OpcodeEntry hover) {
        var otherLabelSet = hover.labelSet;
        if (otherLabelSet == null) return false;
        var myLabels = self.labelSet;
        if (myLabels == null || myLabels.isEmpty()) return false;
        for (Label myLabel : myLabels) {
            if (otherLabelSet.contains(myLabel)) {

                return true;
            }
        }
        return false;
    }

    public static boolean textEquality(OpcodeEntry self, OpcodeEntry hover) {
        if (Utils.toString(hover.insnNode, hover.getTextifier()).equals(Utils.toString(self.insnNode, self.getTextifier()))) {
            return true;
        }
        return false;
    }
}
