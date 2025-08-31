package asmext.tree.analysis.dataflow.value;

import asmext.tree.analysis.dataflow.value.merge.MergeContext;
import asmext.tree.analysis.dataflow.visitor.ValueCalculatorVisitor;
import asmext.tree.analysis.dataflow.visitor.ValueVisitor;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LineNumberNode;

public sealed abstract class BaseDataFlowValue extends DataFlowValue permits BaseDataFlowValue.ParameterValue, BaseDataFlowValue.ReturnValue, CommonDataFlowValue {



    @NotNull
    public final Type type;


    public abstract boolean isCopyOp();


    static int totalCreated;

    protected BaseDataFlowValue(@NonNull Type type) {
        this.type = type;
        totalCreated++;
    }

    public static DataFlowValue parameter(@NonNull Type type, int idx) {
        return new ParameterValue(type, idx);
    }

    public static DataFlowValue typed(@NonNull Type type, AbstractInsnNode producer, DataFlowValue... from) {
        if(producer instanceof LineNumberNode)
            throw new IllegalArgumentException("LineNumberNode not allowed as produces");
        return new CommonDataFlowValue(type, producer, from, false);
    }

    public static DataFlowValue returnType(@NonNull Type type) {
        return new ReturnValue(type);
    }


    @Override
    @MagicConstant(intValues = {0, 1, 2})
    public int getSize() {
        //noinspection MagicConstant
        return this.type.getSize();
    }


    public CommonDataFlowValue copied(AbstractInsnNode insn) {
        return new CommonDataFlowValue(type, insn, new DataFlowValue[]{this}, true);
    }

    @Override
    public DataFlowValue merge(BaseDataFlowValue other, MergeContext mergeContext) {
        return MergedDataFlowValue.make(this, other);
    }

    public abstract boolean isSameSource(BaseDataFlowValue newValue);

    @EqualsAndHashCode(callSuper = true)
    public non-sealed static class ReturnValue extends BaseDataFlowValue {
        public ReturnValue(@NonNull Type type) {
            super(type);
        }

        @Override
        public boolean isSameSource(BaseDataFlowValue newValue) {
            return newValue instanceof ReturnValue;
        }

        @Override
        public String toString() {
            return "return(" + type + ")";
        }

        @Override
        public boolean isCopyOp() {
            return false;
        }

        @Override
        public @Nullable DataFlowValue deepMerge(DataFlowValue newValue, MergeContext mergeContext) {
            return equals(newValue) ? newValue : null;
        }
        //region visitors
        @Override
        public void accept(ValueVisitor visitor) {
            visitor.visit(this);
        }

        @Override
        public <T> T accept(ValueCalculatorVisitor<T> visitor) {
            return visitor.visit(this);
        }
        //endregion
    }

    @EqualsAndHashCode(callSuper = true)
    public non-sealed static class ParameterValue extends BaseDataFlowValue {
        public final int idx;

        public ParameterValue(@NonNull Type type, int idx) {
            super(type);
            this.idx = idx;
        }

        @Override
        public String toString() {
            return "param(" + type + ";" + idx + ")";
        }

        @Override
        public boolean isCopyOp() {
            return false;
        }

        @Override
        public boolean isSameSource(BaseDataFlowValue newValue) {
            return newValue instanceof ParameterValue param && param.idx == idx;
        }

        @Override
        public @Nullable DataFlowValue deepMerge(DataFlowValue newValue, MergeContext mergeContext) {
            return newValue instanceof ParameterValue param && param.idx == idx ? this : null;
        }
        //region visitors
        @Override
        public void accept(ValueVisitor visitor) {
            visitor.visit(this);
        }

        @Override
        public <T> T accept(ValueCalculatorVisitor<T> visitor) {
            return visitor.visit(this);
        }
        //endregion
    }
}
