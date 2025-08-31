package asmext.tree.analysis.dataflow.meta;

import asmext.tree.analysis.dataflow.interpreter.MetaDataKey;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.objectweb.asm.ConstantDynamic;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Type;
/**
 * Represents metadata describing constant values originating from {@code LDC} or similar instructions
 * during bytecode analysis.
 *
 * <p>This class and its subtypes model JVM constants such as numbers, strings, class types,
 * method handles, and dynamic constants.</p>
 *
 * <p>Instances are typically attached to analysis values using {@link MetaDataKey}, allowing
 * symbolic execution or optimization logic to retrieve and inspect constant properties.</p>
 *
 * @see #makeFromLDC(Object)
 * @see #meta
 */
public sealed abstract class ConstMeta {
    /**
     * Metadata key for associating {@code ConstMeta} instances with analysis values.
     */
    public static final MetaDataKey<ConstMeta> meta = MetaDataKey.create();
    /**
     * Attempts to construct a {@code ConstMeta} instance from a constant pool value,
     * as typically encountered in {@code LDC} instructions.
     *
     * <p>Supports standard JVM constant types including {@code Integer}, {@code Long},
     * {@code Float}, {@code Double}, {@code String}, {@code Type}, {@code Handle},
     * and {@code ConstantDynamic}. Falls back to {@link Other} if no specific subclass matches.</p>
     *
     * @param value the constant value (must be non-null)
     * @return corresponding {@code ConstMeta} representation
     */
    public static ConstMeta makeFromLDC(Object value) {
        if (value instanceof Integer v) return new IntegerConstant.Int(v);
        if (value instanceof Long v) return new IntegerConstant.Long(v);
        if (value instanceof Float v) return new FloatingConstant.Float(v);
        if (value instanceof Double v) return new FloatingConstant.Double(v);
        if (value instanceof java.lang.String v) return new String(v);
        if (value instanceof Type v) return new TypeConst(v);
        if (value instanceof Handle v) return new HandleConst(v);
        if (value instanceof ConstantDynamic v) return new ConstDynamic(v);
        return new Other(value);
    }
    /**
     * Typically {@code 1} for most values, or {@code 2} for {@code long} and {@code double}.
     *
     * @return stack size of the constant
     */
    abstract int size();

    /**
     * Base class for integer-based constants, including {@code int} and {@code long}
     * (also used for {@code boolean}, {@code char}, {@code byte}, and {@code short}).
     */
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PUBLIC, makeFinal = true)
    public static sealed abstract class IntegerConstant extends ConstMeta { /**
     * Returns the numeric value of this constant as a {@code long}.
     * May need to be downcast depending on the original type.
     */
        abstract long value();


        /**
         * Constant representing a 32-bit {@code int} value.
         * <p>
         * This class is also used to represent smaller integral types such as
         * {@code boolean}, {@code byte}, {@code short}, and {@code char},
         */
        @AllArgsConstructor
        @FieldDefaults(level = AccessLevel.PUBLIC, makeFinal = true)
        public static final class Int extends IntegerConstant {
            int value;

            @Override
            int size() {
                return 1;
            }

            @Override
            long value() {
                return value;
            }
        }
        /**
         * Constant representing a 64-bit {@code long} value.
         */
        @AllArgsConstructor
        @FieldDefaults(level = AccessLevel.PUBLIC, makeFinal = true)
        public static final class Long extends IntegerConstant {
            long value;

            @Override
            int size() {
                return 2;
            }

            @Override
            long value() {
                return value;
            }
        }
    }
    /**
     * Base class for floating-point constants ({@code float} and {@code double}).
     */
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PUBLIC, makeFinal = true)
    public static sealed abstract class FloatingConstant extends ConstMeta {
        abstract double value();
        /**
         * Constant representing a 32-bit {@code float} value.
         */
        @AllArgsConstructor
        @FieldDefaults(level = AccessLevel.PUBLIC, makeFinal = true)
        public static final class Float extends FloatingConstant {
            float value;

            @Override
            int size() {
                return 1;
            }

            @Override
            double value() {
                return value;
            }
        }
        /**
         * Constant representing a 64-bit {@code double} value.
         */
        @AllArgsConstructor
        @FieldDefaults(level = AccessLevel.PUBLIC, makeFinal = true)
        public static final class Double extends FloatingConstant {
            double value;

            @Override
            int size() {
                return 2;
            }

            @Override
            double value() {
                return value;
            }
        }

    }

    /**
     * Represents a constant {@code String} value.
     */
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PUBLIC, makeFinal = true)
    public static final class String extends ConstMeta {
        java.lang.String value;

        @Override
        int size() {
            return 1;
        }
    }
    /**
     * Represents a constant {@code Type} (usually {@code Class<T>}).
     */
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PUBLIC, makeFinal = true)
    public static final class TypeConst extends ConstMeta {
        Type value;

        @Override
        int size() {
            return 1;
        }
    }
    /**
     * Represents a constant {@code Handle} (used in invokedynamic, method handles).
     */
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PUBLIC, makeFinal = true)
    public static final class HandleConst extends ConstMeta {
        Handle value;

        @Override
        int size() {
            return 1;
        }
    }
    /**
     * Represents a {@code ConstantDynamic}, as used in {@code ldc} instructions with dynamic linkage.
     */
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PUBLIC, makeFinal = true)
    public static final class ConstDynamic extends ConstMeta {
        ConstantDynamic value;

        @Override
        int size() {
            return 1;
        }
    }

    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PUBLIC, makeFinal = true)
    public static final class Other extends ConstMeta {
        Object value;

        @Override
        int size() {
            return 1;
        }
    }

    /**
     * Singleton type that represents a constant {@code null} value in the bytecode.
     */
    @FieldDefaults(level = AccessLevel.PUBLIC, makeFinal = true)
    public static final class Null extends ConstMeta {
        public static final Null instance = new Null();

        private Null() {
        }

        @Override
        int size() {
            return 0;
        }
    }
}
