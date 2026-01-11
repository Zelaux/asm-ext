package asmext.postprocessors.inlinefunc;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;

public abstract class Result<V, E> {
    private Result() {
    }

    public static <T, E> @NotNull Result<T, E> error(E errorKind) {return new Error<>(errorKind);}

    public static <T, E> @NotNull Result<T, E> ok(T value) {return new Ok<>(value);}

    public abstract V getValue();

    public abstract E getError();

    public abstract boolean isValue();

    public boolean isError() {
        return !isValue();
    }

    @AllArgsConstructor
    @FieldDefaults(makeFinal = true, level = AccessLevel.PUBLIC)
    @Getter
    public static class Ok<V, E> extends Result<V, E> {
        V value;

        @Override
        public E getError() {return null;}

        @Override
        public boolean isValue() {return true;}
    }

    @AllArgsConstructor
    @FieldDefaults(makeFinal = true, level = AccessLevel.PUBLIC)
    @Getter
    public static class Error<V, E> extends Result<V, E> {
        E error;


        @Override
        public V getValue() {
            return null;
        }

        @Override
        public boolean isValue() {return false;}
    }
}
