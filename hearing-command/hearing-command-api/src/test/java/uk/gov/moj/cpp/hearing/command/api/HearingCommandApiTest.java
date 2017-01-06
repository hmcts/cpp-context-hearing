package uk.gov.moj.cpp.hearing.command.api;

import static java.util.Collections.sort;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.justice.services.test.utils.core.matchers.HandlerClassMatcher.isHandlerClass;
import static uk.gov.justice.services.test.utils.core.matchers.HandlerMethodMatcher.method;

import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.annotation.Handles;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

public class HearingCommandApiTest {

    private final Map<String, String> apiHandlers = new HashMap<>();
    private List<String> ramlActionNames;

    @Before
    public void setup() throws IOException {
        for (Method method : HearingCommandApi.class.getMethods()) {
            final Handles handles = method.getAnnotation(Handles.class);
            if (null != handles) {
                apiHandlers.put(method.getName(), handles.value());
            }
        }

        /*
          Reading Raml and extraction action name @ {ramlActionNames}
         */
        List<String> allLines = FileUtils.readLines(new File("src/raml/hearing-command-api.raml"));
        Predicate<String> nonEmptyStringPredicate = (String s) -> !s.isEmpty();

        ramlActionNames = allLines.stream()
                .filter(nonEmptyStringPredicate)
                .filter(line -> line.contains("name:"))
                .map(line -> line.replaceAll("name:", "").trim())
                .collect(Collectors.toList());
    }

    @Test
    public void testActionNameAndHandleNameAreSame() throws Exception {
        final List<String> handleNames = apiHandlers.entrySet()
                .stream()
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());

        sort(handleNames);
        sort(ramlActionNames);

        assertThat(handleNames, is(ramlActionNames));
    }

    @Test
    public void testHandleNamesPassThroughSender() throws Exception {
        for (Map.Entry<String, String> entry : apiHandlers.entrySet()) {
            assertThat(HearingCommandApi.class, isHandlerClass(Component.COMMAND_API).with(method(entry.getKey()).thatHandles(entry.getValue()).withSenderPassThrough()));
        }
    }

}