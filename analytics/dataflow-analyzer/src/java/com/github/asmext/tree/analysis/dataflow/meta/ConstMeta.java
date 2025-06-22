package com.github.asmext.tree.analysis.dataflow.meta;

import com.github.asmext.tree.analysis.dataflow.interpreter.MetaDataKey;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;

public sealed abstract class ConstMeta {
    public static final MetaDataKey<ConstMeta> meta = MetaDataKey.create();

    public static ConstMeta makeFromLDC(Object value) {
        if(value instanceof Integer v) return new IntegerConstant.Int(v);
        if(value instanceof Long v) return new IntegerConstant.Long(v);
        if(value instanceof Float v) return new FloatingConstant.Float(v);
        if(value instanceof Double v) return new FloatingConstant.Double(v);
        if(value instanceof java.lang.String v) return new String(v);
        return new Other(value);
    }

    abstract int size();

    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PUBLIC, makeFinal = true)
    public static sealed abstract class IntegerConstant extends ConstMeta {
        abstract long value();

        @AllArgsConstructor
        @FieldDefaults(level = AccessLevel.PUBLIC, makeFinal = true)
        public static final class Byte extends IntegerConstant {
            byte value;

            @Override
            int size() {
                return 1;
            }

            @Override
            long value() {
                return value;
            }
        }

        @AllArgsConstructor
        @FieldDefaults(level = AccessLevel.PUBLIC, makeFinal = true)
        public static final class Short extends IntegerConstant {
            short value;

            @Override
            int size() {
                return 1;
            }

            @Override
            long value() {
                return value;
            }
        }

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

    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PUBLIC, makeFinal = true)
    public static sealed abstract class FloatingConstant extends ConstMeta {
        abstract double value();

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

    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PUBLIC, makeFinal = true)
    public static final class Boolean extends ConstMeta {
        boolean value;

        @Override
        int size() {
            return 1;
        }
    }

    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PUBLIC, makeFinal = true)
    public static final class String extends ConstMeta {
        java.lang.String value;

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
