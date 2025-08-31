package asmext.jvm.visitor;

import org.objectweb.asm.*;
import org.objectweb.asm.util.*;

import java.io.*;

@SuppressWarnings("unused")
public class ReusableTextifier extends Textifier{
    public ReusableTextifier(){
        super(Opcodes.ASM9);
    }

    @Override
    public void print(PrintWriter printWriter){
        if(!stringBuilder.isEmpty()){
            printWriter.print(stringBuilder);
            stringBuilder.setLength(0);
        }
        super.print(printWriter);
        printWriter.flush();
    }

    public void clear(){
        stringBuilder.setLength(0);
        text.clear();
    }
}
