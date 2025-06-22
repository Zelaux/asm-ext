package com.github.asmext.tree.analysis.dataflow;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.BasicVerifier;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.*;

public class TestAsmUtil {
    public static ClassNode debugSaveByteCode(String dispatcher, byte[] bytecode, String newClassName) throws IOException {
        File folder = TestOutput.subfolder(true, dispatcher);

        ClassNode classNode = new ClassNode();
        new ClassReader(bytecode).accept(classNode, 0);

        try(var stream = new FileOutputStream(new File(folder, newClassName + ".class"))) {
            stream.write(bytecode);
        }
        try(var stream = new FileOutputStream(new File(folder, newClassName + ".asm.java"))) {
            classNode.accept(new TraceClassVisitor(null, new Textifier(), new PrintWriter(stream)));
        }
        return classNode;
    }

    public static ClassNode getClassNode(byte[] bytecode) {
        ClassNode classNode = new ClassNode();

        new ClassReader(bytecode).accept(classNode, 0);
        return classNode;
    }

    public static void correctByteCode(byte[] dump) throws AnalyzerException {
        ClassNode classNode = TestAsmUtil.getClassNode(dump);
        Analyzer<BasicValue> analyzer = new Analyzer<>(new BasicVerifier());
        for(MethodNode method : classNode.methods) {
            analyzer.analyze(classNode.name,method);
        }

    }

    public static String asmText(byte[] bytes) {
        StringWriter out = new StringWriter();
        new ClassReader(bytes).accept(new TraceClassVisitor(null, new Textifier(), new PrintWriter(out)), 0);
        return out.toString();
    }

}
