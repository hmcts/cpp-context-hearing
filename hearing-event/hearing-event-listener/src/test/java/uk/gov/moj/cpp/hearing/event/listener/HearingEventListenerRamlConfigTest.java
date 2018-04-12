package uk.gov.moj.cpp.hearing.event.listener;


import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import uk.gov.justice.domain.annotation.Event;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventIgnored;
import uk.gov.moj.cpp.hearing.domain.event.HearingVerdictUpdated;
import uk.gov.moj.cpp.hearing.domain.event.InitiateHearingOffenceEnriched;
import uk.gov.moj.cpp.hearing.domain.event.MagsCourtHearingRecorded;
import uk.gov.moj.cpp.hearing.domain.event.NewMagsCourtHearingRecorded;
import uk.gov.moj.cpp.hearing.domain.event.OffencePleaUpdated;
import uk.gov.moj.cpp.hearing.domain.event.SendingSheetCompletedPreviouslyRecorded;
import uk.gov.moj.cpp.hearing.domain.event.SendingSheetCompletedRecorded;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

public class HearingEventListenerRamlConfigTest {
    private static final String PATH_TO_RAML = "src/raml/hearing-event-listener.messaging.raml";
    private static final String COMMAND_NAME = "hearing";
    private static final String CONTENT_TYPE_PREFIX = "application/vnd.";

    private Map<String, String> handlerNames = new HashMap<>();

    private final List<String> handlerNamesToIgnore = asList(

            InitiateHearingOffenceEnriched.class.getAnnotation(Event.class).value(),
            OffencePleaUpdated.class.getAnnotation(Event.class).value(),
            HearingEventIgnored.class.getAnnotation(Event.class).value(),

            HearingVerdictUpdated.class.getAnnotation(Event.class).value(),
            SendingSheetCompletedRecorded.class.getAnnotation(Event.class).value(),
            SendingSheetCompletedPreviouslyRecorded.class.getAnnotation(Event.class).value(),
            MagsCourtHearingRecorded.class.getAnnotation(Event.class).value(),
            NewMagsCourtHearingRecorded.class.getAnnotation(Event.class).value()
            );

    private List<String> ramlActionNames;

    @Before
    public void setup() throws IOException {
        handlerNames.putAll(getMethodsToHandlerNamesMapFor(HearingEventListener.class));
        handlerNames.putAll(getMethodsToHandlerNamesMapFor(NewHearingEventListener.class));
        handlerNames.putAll(getMethodsToHandlerNamesMapFor(NewModelPleaUpdateEventListener.class));
        handlerNames.putAll(getMethodsToHandlerNamesMapFor(VerdictUpdateEventListener.class));
        handlerNames.putAll(getMethodsToHandlerNamesMapFor(HearingLogEventListener.class));

        final List<String> allLines = FileUtils.readLines(new File(PATH_TO_RAML));

        this.ramlActionNames = allLines.stream()
                .filter(action -> !action.isEmpty())
                .filter(line -> line.contains(CONTENT_TYPE_PREFIX) && line.contains(COMMAND_NAME))
                .map(line -> line.replaceAll("(application/vnd\\.)|(\\+json:)","").trim())
                .collect(toList());
    }

    @Test
    public void testActionNameAndHandleNameAreSame() throws Exception {

        assertThat(handlerNames.values(), containsInAnyOrder(this.ramlActionNames.toArray()));
    }

    @Test
    public void testEventsHandledProperly() throws Exception {
        final List<String> eventHandlerNames = new FastClasspathScanner("uk.gov.moj.cpp.hearing.domain.event")
                .scan().getNamesOfClassesWithAnnotation(Event.class)
                .stream().map(className -> {
                    try {
                        return Class.forName(className).getAnnotation(Event.class).value();
                    } catch (final ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(toList());

        eventHandlerNames.removeAll(this.handlerNamesToIgnore);

        assertThat(this.ramlActionNames, containsInAnyOrder(eventHandlerNames.toArray()));
    }

    private Map<String, String> getMethodsToHandlerNamesMapFor(final Class<?>... commandApiClasses) {
        final Map<String, String> methodToHandlerNamesMap = new HashMap<>();
        for (final Class<?> commandApiClass : commandApiClasses) {
            for (final Method method : commandApiClass.getMethods()) {
                final Handles handles = method.getAnnotation(Handles.class);
                if (handles != null) {
                    methodToHandlerNamesMap.put(method.getName(), handles.value());
                }
            }
        }
        return methodToHandlerNamesMap;
    }

}
