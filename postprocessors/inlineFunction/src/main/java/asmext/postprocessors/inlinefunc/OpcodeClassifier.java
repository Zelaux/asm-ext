package asmext.postprocessors.inlinefunc;

import org.objectweb.asm.Opcodes;

public class OpcodeClassifier {
    static boolean isLoad(int opcode) {
        return Opcodes.ILOAD <= opcode && opcode <= Opcodes.ALOAD;
    }
    static boolean isStore(int opcode) {return Opcodes.ISTORE <= opcode && opcode <= Opcodes.ASTORE;}
    static boolean isConst(int opcode) {return Opcodes.ICONST_0 <= opcode && opcode <= Opcodes.LDC;}
}
