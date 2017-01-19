package uk.gov.moj.cpp.hearing.event.listener;


import static java.util.Arrays.asList;
import static java.util.Collections.sort;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;
import static uk.gov.justice.services.test.utils.core.matchers.HandlerClassMatcher.isHandlerClass;
import static uk.gov.justice.services.test.utils.core.matchers.HandlerMethodMatcher.method;

import uk.gov.justice.domain.annotation.Event;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventDeletionIgnored;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventIgnored;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

public class ValidateHearingEventRamlConfigAndHandlesActionTest {

    private final Map<String, String> eventHandlers = new HashMap<>();
    private final String COMMAND_NAME = "hearing";
    private final List<String> eventsToIgnore = asList(
            HearingEventIgnored.class.getName(),
            HearingEventDeletionIgnored.class.getName()
    );

    private List<String> ramlActionNames;

    @Before
    public void setup() throws IOException {
        for (Method method : HearingEventListener.class.getMethods()) {
            final Handles handles = method.getAnnotation(Handles.class);
            if (null != handles) {
                eventHandlers.put(method.getName(), handles.value());

            }
        }

        /*
          Reading Raml and extraction action name @ {ramlActionNames}
         */
        final List<String> allLines = FileUtils.readLines(new File("src/raml/hearing-event-listener.messaging.raml"));
        final Predicate<String> nonEmptyStringPredicate = (String s) -> !s.isEmpty();

        ramlActionNames = allLines.stream()
                .filter(nonEmptyStringPredicate)
                .filter(line -> line.contains("application/vnd."))
                .filter(line -> line.contains(COMMAND_NAME))
                .map(line -> line.replaceAll("application/vnd.", "").trim())
                .map(line -> line.replaceAll("\\+json:", "").trim())
                .collect(toList());
    }


    @Test
    public void testActionNameAndHandleNameAreSame() throws Exception {
        final List<String> handleNames = eventHandlers.entrySet()
                .stream()
                .map(Map.Entry::getValue)
                .collect(toList());

        sort(handleNames);
        sort(ramlActionNames);

        assertThat(handleNames, is(ramlActionNames));
    }

    @Test
    public void testActionNamesAreHandledProperly() throws Exception {
        for (Map.Entry<String, String> entry : eventHandlers.entrySet()) {
            assertThat(HearingEventListener.class, isHandlerClass(EVENT_LISTENER).with(method(entry.getKey()).thatHandles(entry.getValue())));
        }
    }

    @Test
    public void testEventsHandledProperly() throws Exception {
        final List<String> namesOfClassesWithAnnotation = new io.github.lukehutch.fastclasspathscanner.FastClasspathScanner("uk.gov.moj.cpp.hearing.domain.event")
                .scan()
                .getNamesOfClassesWithAnnotation(Event.class);

        namesOfClassesWithAnnotation.removeAll(eventsToIgnore);

        for (String className : namesOfClassesWithAnnotation) {
            final Class classZ = Class.forName(className);
            final Event handles = (Event) classZ.getAnnotation(Event.class);
            assertThat(className + " event name missing in the RAML ", true, is(ramlActionNames.contains(handles.value())));
        }
    }
}
