package asmlib.util;

import org.jetbrains.annotations.*;
import org.objectweb.asm.*;

import java.util.*;

public class ByteCodeClassLoader extends ClassLoader{
    private final Object CLASSNAME_LOCK = new Object();
    byte[][] byteCodes;
    @Nullable
    String[] classNames;

    @SuppressWarnings("unused")
    public ByteCodeClassLoader(ClassLoader parent, byte[]... byteCodes){
        super(parent);
        this.byteCodes = byteCodes;
    }

    public ByteCodeClassLoader(byte[]... byteCodes){
        this.byteCodes = byteCodes;
    }

    @SuppressWarnings("unused")
    public void addByteCode(byte[]... bytes){
        int prefSize = byteCodes.length;
        {
            byte[][] newByteCodes = new byte[byteCodes.length + bytes.length][];
            System.arraycopy(byteCodes, 0, newByteCodes, 0, prefSize);
            System.arraycopy(bytes, 0, newByteCodes, prefSize, bytes.length);
            this.byteCodes = newByteCodes;
        }
        if(classNames == null) return;
        {
            String[] newNames = new String[classNames.length + bytes.length];
            System.arraycopy(classNames, 0, newNames, 0, classNames.length);
            classNames = newNames;
        }


        for(int i = 0; i < bytes.length; i++){
            classNames[i + prefSize] = new ClassReader(bytes[i]).getClassName();
        }

    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException{
        init();
        for(int i = 0; i < classNames.length; i++){
            if(Objects.equals(classNames[i], name)){
                byte[] code = byteCodes[i];
                return defineClass(name, code, 0, code.length);
            }
        }
        return super.findClass(name);
    }

    private void init(){
        if(classNames != null) return;
        synchronized(CLASSNAME_LOCK){
            if(classNames != null) return;
            classNames = new String[byteCodes.length];
            for(int i = 0; i < byteCodes.length; i++){
                classNames[i] = new ClassReader(byteCodes[i]).getClassName();
            }

        }
    }
}
