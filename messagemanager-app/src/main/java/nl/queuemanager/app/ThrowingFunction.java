package nl.queuemanager.app;

import java.util.function.Function;

@FunctionalInterface
public interface ThrowingFunction<T, R, E extends Throwable> {

    R apply(T arg) throws E;

    static <T, R, E extends Throwable> Function<T, R> wrap(ThrowingFunction<T, R, E> wrapped) {
        return arg -> {
            try {
                return wrapped.apply(arg);
            } catch(Throwable e) {
                throw new RuntimeException(e);
            }
        };
    }

}
