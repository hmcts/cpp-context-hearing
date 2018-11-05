package uk.gov.moj.cpp.hearing.test.matchers;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.SelfDescribing;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class BeanMatcher<T> extends BaseMatcher<T> {

    private Class<T> clazz;
    private Error error;
    private String methodName;
    private Assertion<T, ?> failedAssertion;
    private List<Assertion<T, ?>> assertions = new ArrayList<>();

    private BeanMatcher(Class<T> clazz) {
        this.clazz = clazz;
    }

    public <R> BeanMatcher<T> with(Function<T, R> accessor, Matcher<R> matcher) {
        assertions.add(new Assertion<>(accessor, matcher));
        return this;
    }

    @Override
    public boolean matches(Object item) {

        if (item == null) {
            this.error = Error.NULL;
            return false;
        }

        if (!clazz.isInstance(item)) {
            this.error = Error.INVALID_TYPE;
            return false;
        }

        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(clazz);
        enhancer.setCallback((MethodInterceptor) (obj, method, args, proxy) -> {
            methodName = method.getName();
            return method.invoke(item, args);
        });

        try {
            T proxy = (T) enhancer.create();

            for (Assertion<T, ?> assertion: assertions){
                if (!assertion.getMatcher().matches(assertion.getAccessor().apply((T) proxy))){
                    this.failedAssertion = assertion;
                    this.error = Error.INVALID_ASSERTION;
                    return false;
                }
            }

        } catch (IllegalArgumentException e){
            throw new IllegalArgumentException("invalid super clazz: " + clazz.getName(), e);
        }



        return true;
    }


    @Override
    public void describeTo(Description description) {
        Description descriptionWrapper;
        if (Proxy.isProxyClass(description.getClass())) {
            descriptionWrapper = description;
        } else {
            descriptionWrapper = (Description) Proxy.newProxyInstance(Description.class.getClassLoader(),
                    new Class[]{Description.class}, new DescriptionProxyHandler(description));
        }

        if (error == Error.NULL) {
            descriptionWrapper.appendText(" to not be null");
        }

        if (error == Error.INVALID_TYPE) {
            descriptionWrapper.appendText(" to be of type ").appendText(this.clazz.getCanonicalName());
        }

        if (error == Error.INVALID_ASSERTION) {
            descriptionWrapper.appendText(".").appendText(this.methodName).appendText("()").appendDescriptionOf(this.failedAssertion.getMatcher());
        }
    }


    @Override
    public void describeMismatch(Object item, Description description) {

        if (error == Error.NULL) {
            description.appendText("was null");
        }

        if (error == Error.INVALID_TYPE) {
            description.appendText("was of type ").appendText(item.getClass().getCanonicalName());
        }

        if (error == Error.INVALID_ASSERTION) {
            this.failedAssertion.getMatcher().describeMismatch(this.failedAssertion.getAccessor().apply((T) item), description);
        }
    }

    private enum Error {
        NULL,
        INVALID_ASSERTION,
        INVALID_TYPE
    }

    private static class DescriptionProxyHandler implements InvocationHandler {

        private boolean latch = false;
        private Description description;

        DescriptionProxyHandler(Description description) {
            this.description = description;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (!latch) {
                this.description.appendText("this");
            }
            latch = true;

            if ("appendDescriptionOf".equals(method.getName())) {
                if (!(args[0] instanceof BeanMatcher || args[0] instanceof ElementAtListMatcher)) {
                    description.appendText(" ");
                }
                SelfDescribing selfDescribing = (SelfDescribing) args[0];
                selfDescribing.describeTo((Description) proxy);
                return proxy;
            }
            method.invoke(description, args);
            return proxy;
        }
    }

    private static class Assertion<T, R> {

        private Function<T, R> accessor;
        private Matcher<R> matcher;

        private Assertion(Function<T, R> accessor, Matcher<R> matcher) {
            this.accessor = accessor;
            this.matcher = matcher;
        }

        Function<T, R> getAccessor() {
            return accessor;
        }

        Matcher<R> getMatcher() {
            return matcher;
        }
    }

    public static <T> BeanMatcher<T> isBean(Class<T> clazz) {
        return new BeanMatcher<>(clazz);
    }
}
