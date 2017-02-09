package uk.gov.moj.cpp.hearing;

import static java.util.Collections.sort;
import static org.apache.commons.io.FileUtils.readLines;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.justice.services.test.utils.core.matchers.HandlerClassMatcher.isHandlerClass;
import static uk.gov.justice.services.test.utils.core.matchers.HandlerMethodMatcher.method;

import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.moj.cpp.hearing.command.handler.HearingCommandHandler;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;

public class ValidateHearingCommandRamlConfigAndPassThroughSenderTest {

    private final Map<String, String> handlers = new HashMap<>();
    private final String COMMAND_NAME = "hearing";

    private List<String> ramlActionNames;

    @Before
    public void setup() throws IOException {
        for (Method method : HearingCommandHandler.class.getMethods()) {
            final Handles handles = method.getAnnotation(Handles.class);
            if (handles != null) {
                handlers.put(method.getName(), handles.value());

            }
        }

        /*
          Reading Raml and extraction action name @ {ramlActionNames}
         */
        final List<String> allLines = readLines(new File("src/raml/hearing-command-handler.messaging.raml"));
        final Predicate<String> nonEmptyStringPredicate = (String s) -> !s.isEmpty();

        ramlActionNames = allLines.stream()
                .filter(nonEmptyStringPredicate)
                .filter(line -> line.contains("application/vnd."))
                .filter(line -> line.contains(COMMAND_NAME))
                .map(line -> line.replaceAll("application/vnd.", "").trim())
                .map(line -> line.replaceAll("\\+json:", "").trim())
                .collect(Collectors.toList());
    }


    @Test
    public void testActionNameAndHandleNameAreSame() throws Exception {
        final List<String> handleNames = handlers.entrySet()
                .stream()
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());

        sort(handleNames);
        sort(ramlActionNames);

        assertThat(handleNames, is(ramlActionNames));
    }

    @Test
    public void testActionNamesAreHandledProperly() throws Exception {
        for (Map.Entry<String, String> entry : handlers.entrySet())
            assertThat(HearingCommandHandler.class, isHandlerClass(Component.COMMAND_HANDLER).with(method(entry.getKey()).thatHandles(entry.getValue())));
    }

}
