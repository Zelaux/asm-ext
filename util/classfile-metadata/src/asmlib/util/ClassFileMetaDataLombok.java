/*
 * Copyright (C) 2010-2023 The Project Lombok Authors.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 * This class is an unmodified copy from Lombok v1.18.30 (https://projectlombok.org).
 * Original source: https://github.com/projectlombok/lombok/blob/87e8af476a40b9a7b1c53a82a0fd1d8ba1ead707/src/core/lombok/bytecode/ClassFileMetaData.java
 */
package asmlib.util;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public class ClassFileMetaDataLombok {
    protected static final byte UTF8 = 1;
    protected static final byte INTEGER = 3;
    protected static final byte FLOAT = 4;
    protected static final byte LONG = 5;
    protected static final byte DOUBLE = 6;
    protected static final byte CLASS = 7;
    protected static final byte STRING = 8;
    protected static final byte FIELD = 9;
    protected static final byte METHOD = 10;
    protected static final byte INTERFACE_METHOD = 11;
    protected static final byte NAME_TYPE = 12;
    protected static final byte METHOD_HANDLE = 15;
    protected static final byte METHOD_TYPE = 16;
    protected static final byte DYNAMIC = 17;
    protected static final byte INVOKE_DYNAMIC = 18;
    protected static final byte MODULE = 19;
    protected static final byte PACKAGE = 20;
    protected static final int NOT_FOUND = -1;
    protected static final int START_OF_CONSTANT_POOL = 8;
    protected final byte[] byteCode;
    protected final int maxPoolSize;
    protected final int[] offsets;
    protected final byte[] types;
    protected final String[] utf8s;
    protected final int endOfPool;

    ClassFileMetaDataLombok(byte[] byteCode) {
        this.byteCode = byteCode;
        this.maxPoolSize = this.readValue(8);
        this.offsets = new int[this.maxPoolSize];
        this.types = new byte[this.maxPoolSize];
        this.utf8s = new String[this.maxPoolSize];
        int position = 10;

        for (int i = 1; i < this.maxPoolSize; ++i) {
            byte type = byteCode[position];
            this.types[i] = type;
            ++position;
            this.offsets[i] = position;
            switch (type) {
                case 0:
                    break;
                case UTF8:
                    int length = this.readValue(position);
                    position += 2;
                    this.utf8s[i] = this.decodeString(position, length);
                    position += length;
                    break;
                case 2:
                case 13:
                case 14:
                default:
                    throw new AssertionError("Unknown constant pool type " + type);
                case INTEGER:
                case FLOAT:
                case FIELD:
                case METHOD:
                case INTERFACE_METHOD:
                case NAME_TYPE:
                case DYNAMIC:
                case INVOKE_DYNAMIC:
                    position += 4;
                    break;
                case LONG:
                case DOUBLE:
                    position += 8;
                    ++i;
                    break;
                case CLASS:
                case STRING:
                case METHOD_TYPE:
                case MODULE:
                case PACKAGE:
                    position += 2;
                    break;
                case METHOD_HANDLE:
                    position += 3;
            }
        }

        this.endOfPool = position;
    }

    protected String decodeString(int pos, int size) {
        int end = pos + size;
        char[] result = new char[size];
        int length = 0;

        while (pos < end) {
            int first = this.byteCode[pos++] & 255;
            if (first < 128) {
                result[length++] = (char) first;
            } else {
                int x;
                int y;
                if ((first & 224) == 192) {
                    x = (first & 31) << 6;
                    y = this.byteCode[pos++] & 63;
                    result[length++] = (char) (x | y);
                } else {
                    x = (first & 15) << 12;
                    y = (this.byteCode[pos++] & 63) << 6;
                    int z = this.byteCode[pos++] & 63;
                    result[length++] = (char) (x | y | z);
                }
            }
        }

        return new String(result, 0, length);
    }

    public int[] getOffsets(byte type) {
        int[] out = new int[this.types.length];
        int ptr = 0;

        for (int i = 0; i < this.types.length; ++i) {
            if (this.types[i] == (int) type) {
                out[ptr++] = this.offsets[i];
            }
        }

        return Arrays.copyOf(out, ptr);
    }

    public boolean containsUtf8(String value) {
        return this.findUtf8(value) != -1;
    }

    public boolean usesClass(String className) {
        return this.findClass(className) != -1;
    }

    public boolean usesField(String className, String fieldName) {
        int classIndex = this.findClass(className);
        if (classIndex == -1) {
            return false;
        }
        int fieldNameIndex = this.findUtf8(fieldName);
        if (fieldNameIndex == -1) {
            return false;
        }
        for (int i = 1; i < this.maxPoolSize; ++i) {
            if (this.types[i] == FIELD && this.readValue(this.offsets[i]) == classIndex) {
                int nameAndTypeIndex = this.readValue(this.offsets[i] + 2);
                if (this.readValue(this.offsets[nameAndTypeIndex]) == fieldNameIndex) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean usesMethod(String className, String methodName) {
        int classIndex = this.findClass(className);
        if (classIndex == -1) {
            return false;
        }
        int methodNameIndex = this.findUtf8(methodName);
        if (methodNameIndex == -1) {
            return false;
        }
        return isMethod(classIndex, methodNameIndex);
    }

    protected boolean isMethod(int classIndex, int methodNameIndex) {
        for (int i = 1; i < this.maxPoolSize; ++i) {
            if (this.isMethod(i) && this.readValue(this.offsets[i]) == classIndex) {
                int nameAndTypeIndex = this.readValue(this.offsets[i] + 2);
                if (this.readValue(this.offsets[nameAndTypeIndex]) == methodNameIndex) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean usesMethod(String className, String methodName, String descriptor) {
        int classIndex = this.findClass(className);
        if (classIndex == -1) {
            return false;
        }
        int nameAndTypeIndex = this.findNameAndType(methodName, descriptor);
        if (nameAndTypeIndex == -1) {
            return false;
        }
        return isMethodTyped(classIndex, nameAndTypeIndex);
    }

    public boolean isMethodTyped(int classIndex, int nameAndTypeIndex) {
        for (int i = 1; i < this.maxPoolSize; ++i) {
            if (this.isMethod(i) && this.readValue(this.offsets[i]) == classIndex && this.readValue(this.offsets[i] + 2) == nameAndTypeIndex) {
                return true;
            }
        }

        return false;
    }

    public boolean containsStringConstant(String value) {
        int index = this.findUtf8(value);
        if (index == -1) {
            return false;
        }
        for (int i = 1; i < this.maxPoolSize; ++i) {
            if (this.types[i] == STRING && this.readValue(this.offsets[i]) == index) {
                return true;
            }
        }

        return false;
    }

    public boolean containsLong(long value) {
        for (int i = 1; i < this.maxPoolSize; ++i) {
            if (this.types[i] == LONG && this.readLong(i) == value) {
                return true;
            }
        }

        return false;
    }

    public boolean containsDouble(double value) {
        boolean isNan = Double.isNaN(value);

        for (int i = 1; i < this.maxPoolSize; ++i) {
            if (this.types[i] == DOUBLE) {
                double d = this.readDouble(i);
                if (d == value || isNan && Double.isNaN(d)) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean containsInteger(int value) {
        for (int i = 1; i < this.maxPoolSize; ++i) {
            if (this.types[i] == INTEGER && this.readInteger(i) == value) {
                return true;
            }
        }

        return false;
    }

    public boolean containsFloat(float value) {
        boolean isNan = Float.isNaN(value);

        for (int i = 1; i < this.maxPoolSize; ++i) {
            if (this.types[i] == FLOAT) {
                float f = this.readFloat(i);
                if (f == value || isNan && Float.isNaN(f)) {
                    return true;
                }
            }
        }

        return false;
    }

    protected long readLong(int index) {
        int pos = this.offsets[index];
        return (long) this.read32(pos) << 32 | (long) this.read32(pos + 4) & 4294967295L;
    }

    protected double readDouble(int index) {
        return Double.longBitsToDouble(this.readLong(index));
    }

    protected int readInteger(int index) {
        return this.read32(this.offsets[index]);
    }

    protected float readFloat(int index) {
        return Float.intBitsToFloat(this.readInteger(index));
    }

    protected int read32(int pos) {
        return (this.byteCode[pos] & 255) << 24 | (this.byteCode[pos + 1] & 255) << 16 | (this.byteCode[pos + 2] & 255) << 8 | this.byteCode[pos + 3] & 255;
    }

    public String getClassName() {
        return this.getClassName(this.readValue(this.endOfPool + 2));
    }

    public String getSuperClassName() {
        return this.getClassName(this.readValue(this.endOfPool + 4));
    }

    public List<String> getInterfaces() {
        int size = this.readValue(this.endOfPool + 6);
        if (size == 0) {
            return Collections.emptyList();
        }
        List<String> result = new ArrayList<>();

        for (int i = 0; i < size; ++i) {
            result.add(this.getClassName(this.readValue(this.endOfPool + 8 + i * 2)));
        }

        return result;
    }

    public String poolContent() {
        StringBuilder result = new StringBuilder();

        for (int i = 1; i < this.maxPoolSize; ++i) {
            result.append(String.format("#%02x: ", i));
            int pos = this.offsets[i];
            switch (this.types[i]) {
                case 0:
                    result.append("(cont.)");
                    break;
                case UTF8:
                    result.append("Utf8 ").append(this.utf8s[i]);
                case 2:
                case 13:
                case 14:
                default:
                    break;
                case INTEGER:
                    result.append("int ").append(this.readInteger(i));
                    break;
                case FLOAT:
                    result.append("float ").append(this.readFloat(i));
                    break;
                case LONG:
                    result.append("long ").append(this.readLong(i));
                    break;
                case DOUBLE:
                    result.append("double ").append(this.readDouble(i));
                    break;
                case CLASS:
                    result.append("Class ").append(this.getClassName(i));
                    break;
                case STRING:
                    result.append("String \"").append(this.utf8s[this.readValue(pos)]).append("\"");
                    break;
                case FIELD:
                    this.appendAccess(result.append("Field "), i);
                    break;
                case METHOD:
                case INTERFACE_METHOD:
                    this.appendAccess(result.append("Method "), i);
                    break;
                case NAME_TYPE:
                    this.appendNameAndType(result.append("Name&Type "), i);
                    break;
                case METHOD_HANDLE:
                    result.append("MethodHandle...");
                    break;
                case METHOD_TYPE:
                    result.append("MethodType...");
                    break;
                case DYNAMIC:
                    result.append("Dynamic...");
                    break;
                case INVOKE_DYNAMIC:
                    result.append("InvokeDynamic...");
            }

            result.append("\n");
        }

        return result.toString();
    }

    protected void appendAccess(StringBuilder result, int index) {
        int pos = this.offsets[index];
        result.append(this.getClassName(this.readValue(pos))).append(".");
        this.appendNameAndType(result, this.readValue(pos + 2));
    }

    protected void appendNameAndType(StringBuilder result, int index) {
        int pos = this.offsets[index];
        result.append(this.utf8s[this.readValue(pos)]).append(":").append(this.utf8s[this.readValue(pos + 2)]);
    }

    protected String getClassName(int classIndex) {
        return classIndex < 1 ? null : this.utf8s[this.readValue(this.offsets[classIndex])];
    }

    protected boolean isMethod(int i) {
        byte type = this.types[i];
        return type == METHOD || type == INTERFACE_METHOD;
    }

    protected int findNameAndType(String name, String descriptor) {
        int nameIndex = this.findUtf8(name);
        if (nameIndex == -1) {
            return -1;
        }
        int descriptorIndex = this.findUtf8(descriptor);
        if (descriptorIndex == -1) {
            return -1;
        }
        return nameAndTypePoolIndex(nameIndex, descriptorIndex);
    }

    protected int nameAndTypePoolIndex(int nameIndex, int descriptorIndex) {
        for (int i = 1; i < this.maxPoolSize; ++i) {
            if (this.types[i] == NAME_TYPE && this.readValue(this.offsets[i]) == nameIndex && this.readValue(this.offsets[i] + 2) == descriptorIndex) {
                return i;
            }
        }

        return -1;
    }

    protected int findUtf8(String value) {
        for (int i = 1; i < this.maxPoolSize; ++i) {
            if (value.equals(this.utf8s[i])) {
                return i;
            }
        }

        return -1;
    }

    protected int findClass(String className) {
        int index = this.findUtf8(className);
        if (index == -1) {
            return -1;
        }
        for (int i = 1; i < this.maxPoolSize; ++i) {
            if (this.types[i] == ClassFileMetaDataLombok.CLASS && this.readValue(this.offsets[i]) == index) {
                return i;
            }
        }

        return -1;
    }

    protected int readValue(int position) {
        return (this.byteCode[position] & 255) << 8 | this.byteCode[position + 1] & 255;
    }

}


