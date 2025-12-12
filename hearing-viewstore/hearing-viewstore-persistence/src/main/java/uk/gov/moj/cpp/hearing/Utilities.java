package uk.gov.moj.cpp.hearing;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class Utilities {

    private Utilities() {
    }

    public static <T> T with(T object, Consumer<T> consumer) {
        consumer.accept(object);
        return object;
    }

    public static <T, S> T with(T lhs, S rhs, BiConsumer<T, S> consumer) {
        consumer.accept(lhs, rhs);
        return lhs;
    }
}
