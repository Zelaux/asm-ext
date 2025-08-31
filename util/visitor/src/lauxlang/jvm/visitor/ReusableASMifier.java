package asmext.jvm.visitor;

import org.objectweb.asm.util.*;

import java.io.*;

@SuppressWarnings("unused")
public class ReusableASMifier extends ASMifier{


    public ReusableASMifier(int api, String visitorVariableName, int annotationVisitorId){
        super(api, visitorVariableName, annotationVisitorId);
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
