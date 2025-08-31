package asmext.util.dispatch;

import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public interface INumberVisitor<Data> {
    @DispatchHub
    default Object visit(Number var1, Data var2) {
        if (var1 instanceof Integer) {
            return this.visit((Integer) var1, var2);
        } else if (var1 instanceof Long) {
            return this.visit((Long) var1, var2);
        } else if (var1 instanceof Float) {
            return this.visit((Float) var1, var2);
        } else {
            if (var1 instanceof Double) return this.visit((Double) var1, var2);
            throw null;
        }
    }

    Object visit(Integer flt, Data data);

    Object visit(Long flt, Data data);

    Object visit(Float flt, Data data);

    Object visit(Double flt, Data data);

    static
    Class<INumberVisitor> myClazz = INumberVisitor.class;
    MethodHandles.Lookup lookup = getLookup();

    @SneakyThrows
    private static MethodHandles.@NotNull Lookup getLookup() {
        return MethodHandles.privateLookupIn(myClazz, MethodHandles.lookup());
    }

    MethodHandle[] defaultHandle = {null};

    @SneakyThrows
    static void main(String[] args) {
        Number[][] data = {
                {1, 2},
                {1L, 3L},
                {1f, 4f},
                {1., 5.},
        };
        int[] counter = {0};


        //noinspection unchecked
        INumberVisitor<String[]>[] ref = new INumberVisitor[]{null};
        //noinspection unchecked
        INumberVisitor<String[]> implProxy = (INumberVisitor<String[]>) Proxy.newProxyInstance(myClazz.getClassLoader(),
                new Class[]{myClazz},
                new MyInvocationHandler(counter, ref)
        );
        ref[0] = implProxy;

        Impl impl = new Impl();
        for (Number[] datum : data) {
            Assertions.assertEquals(datum[1], impl.visit(datum[0], args));
        }
        for (Number[] datum : data) {
            counter[0] = 0;
            Assertions.assertEquals(datum[0].getClass(), implProxy.visit(datum[0], args));
        }
        args[0] = "done";
    }

    static class Impl implements INumberVisitor<String[]> {

        @Override
        public Object visit(Integer flt, String[] strings) {
            return flt * 2;
        }

        @Override
        public Object visit(Long flt, String[] strings) {
            return flt * 3L;
        }

        @Override
        public Object visit(Float flt, String[] strings) {
            return flt * 4f;
        }

        @Override
        public Object visit(Double flt, String[] strings) {
            return flt * 5.;
        }
    }

    class MyInvocationHandler implements InvocationHandler {

        private final int[] counter;
        private final INumberVisitor<String[]>[] ref;

        public MyInvocationHandler(int[] counter, INumberVisitor<String[]>[] ref) {
            this.counter = counter;
            this.ref = ref;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args1) throws Throwable {
            counter[0]++;

            System.out.println(method);
            if (counter[0] > 2) throw new RuntimeException();
            if (method.getParameterTypes()[0] == Number.class) {
                MethodHandle methodHandle = defaultHandle[0];
                if (methodHandle == null) {
                    methodHandle =
                            lookup.findSpecial(myClazz, method.getName(), MethodType.methodType(method.getReturnType(), method.getParameterTypes()), myClazz);
                }
                return methodHandle.bindTo(proxy).invokeWithArguments(args1);

            }
            return method.getParameterTypes()[0];
        }
    }
}
