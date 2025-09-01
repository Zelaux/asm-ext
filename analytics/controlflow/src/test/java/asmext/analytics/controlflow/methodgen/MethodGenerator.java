package asmext.analytics.controlflow.methodgen;

import asmext.analytics.controlflow.TestCase;
import lombok.SneakyThrows;
import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.params.provider.Arguments;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.tree.MethodNode;

import static org.objectweb.asm.Type.INT_TYPE;

/**
 * @author Zelaux
 * @since 2025-09
 */
public abstract class MethodGenerator extends GeneratorAdapter implements Opcodes {
    public MethodGenerator() {
        super(ASM9,null,ACC_PUBLIC, TestClassGenerator.TARGET_METHOD_NAME, TestClassGenerator.METHOD_DESCRIPTOR);
    }
    public abstract String[] controlFlowBlocks();

    void setMethodVisitor(MethodVisitor visitor) {
        this.mv = visitor;
    }
    public TestCase[] cases(){
        return null;
    }

    public abstract void generateInternal(Type selfType, Type owner) throws Exception;


    @SneakyThrows
    public Arguments toJunitArgument(Type type) {
        return Arguments.of(getClass(), getBytes(type));
    }

    public byte[] getBytes(Type type) throws Exception {
        return asGenerator().generate(type);
    }

    public @NotNull TestClassGenerator asGenerator() {
        return new TestClassGenerator(this);
    }


    protected void imath(@MagicConstant(valuesFromClass = GeneratorAdapter.class) int opcode) {
        math(opcode, INT_TYPE);
    }

    public boolean hasCases() {
        return cases()!=null;
    }

    public MethodNode asNode(Type ownerType) {
        return asGenerator().getMethodNode(ownerType);
    }
}
