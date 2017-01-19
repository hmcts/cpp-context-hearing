package uk.gov.moj.cpp.hearing.query.controller;

import static java.util.Collections.sort;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.justice.services.test.utils.core.matchers.HandlerClassMatcher.isHandlerClass;
import static uk.gov.justice.services.test.utils.core.matchers.HandlerMethodMatcher.method;

import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.annotation.Handles;

import java.io.File;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

public class HearingQueryControllerTest {

    private static final String ACTION_LABEL = "name:";
    private static final String QUERY_NAME = "hearing";

    private final Map<String, String> apiHandlers = new HashMap<>();

    private List<String> ramlActionNames;

    @Before
    public void setup() throws Exception {
        for (final Method method : HearingQueryController.class.getMethods()) {
            final Handles handles = method.getAnnotation(Handles.class);
            if (null != handles) {
                apiHandlers.put(method.getName(), handles.value());

            }
        }

        /*
          Reading Raml and extraction action name @ {ramlActionNames}
         */
        final List<String> allLines = FileUtils.readLines(new File("src/raml/hearing-query-controller.raml"));
        final Predicate<String> nonEmptyStringPredicate = (String s) -> !s.isEmpty();

        ramlActionNames = allLines.stream()
                .filter(nonEmptyStringPredicate)
                .filter(line -> line.contains(ACTION_LABEL))
                .filter(line -> line.contains(QUERY_NAME))
                .map(line -> line.replaceAll(ACTION_LABEL, "").trim())
                .collect(toList());
    }


    @Test
    public void testActionNameAndHandleNameAreSame() throws Exception {
        final List<String> handleNames = apiHandlers.entrySet()
                .stream()
                .map(Map.Entry::getValue)
                .collect(toList());

        sort(handleNames);
        sort(ramlActionNames);

        assertThat(handleNames, is(ramlActionNames));
    }

    @Test
    public void testHandleNamesPassThroughRequester() throws Exception {
        for (Map.Entry<String, String> entry : apiHandlers.entrySet())
            assertThat(HearingQueryController.class, isHandlerClass(Component.QUERY_CONTROLLER).with(method(entry.getKey()).thatHandles(entry.getValue()).withRequesterPassThrough()));
    }

}
