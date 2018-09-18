package uk.gov.moj.cpp.hearing.test;

import com.jayway.jsonpath.internal.JsonContext;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class TestUtilities {

    public static <T> T with(T object, Consumer<T> consumer) {
        consumer.accept(object);
        return object;
    }

    @SafeVarargs
    @SuppressWarnings("varargs")
    public static <T> List<T> asList(T... a) {
        return new ArrayList<>(Arrays.asList(a));
    }

    public static <T> Matcher<T> print() {
        return new BaseMatcher<T>() {
            @Override
            public boolean matches(Object o) {
                if (o instanceof JsonContext) {
                    JsonContext jsonContext = (JsonContext) o;
                    System.out.println(jsonContext.jsonString());
                }
                return true;
            }

            @Override
            public void describeTo(Description description) {
            }
        };

    }
}
