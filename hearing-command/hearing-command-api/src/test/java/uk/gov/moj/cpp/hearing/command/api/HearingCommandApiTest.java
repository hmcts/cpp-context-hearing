package uk.gov.moj.cpp.hearing.command.api;

import static com.google.common.collect.Lists.newArrayList;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.Arrays.stream;
import static java.util.UUID.randomUUID;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.verify;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_API;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloper;
import static uk.gov.justice.services.test.utils.core.matchers.HandlerClassMatcher.isHandlerClass;
import static uk.gov.justice.services.test.utils.core.matchers.HandlerMethodMatcher.method;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher.jsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.withMetadataEnvelopedFrom;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payloadIsJson;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUIDAndName;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.justice.services.common.util.Clock;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.test.utils.common.helper.StoppedClock;

import javax.json.JsonObject;
import java.io.File;
import java.lang.reflect.Method;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

@RunWith(MockitoJUnitRunner.class)
public class HearingCommandApiTest {

    private static final String PATH_TO_RAML = "src/raml/hearing-command-api.raml";
    private static final String NAME = "name:";

    private static final String FIELD_HEARING_ID = "hearingId";
    private static final String FIELD_RESULT_LINES = "resultLines";

    private static final UUID HEARING_ID = randomUUID();

    private static final String COMMAND_SHARE_RESULTS = "hearing.command.share-results";

    private static final List<String> NON_PASS_THROUGH_METHODS = newArrayList("shareResults", "logHearingEvent",
            "correctEvent", "updatePlea", "updateVerdict", "addWitness", "generateNows", "updateNowsMaterialStatus", "addDefenceCounsel",
            "addProsecutionCounsel", "initiateHearing", "saveDraftResult",
            "updateHearingEvents", "generateNowsV2", "deleteAttendee", "uploadSubscriptions", "saveNowsVariants", "updateDefendantAttendance");

    private Map<String, String> apiMethodsToHandlerNames;
    private Map<String, String> eventApiMethodsToHandlerNames;
    private Map<String, String> notificationApiMethodsToHandlerNames;

    @Mock
    private Sender sender;

    @Spy
    private final Enveloper enveloper = createEnveloper();

    @Spy
    private final Clock clock = new StoppedClock(ZonedDateTime.now());

    @Captor
    private ArgumentCaptor<JsonEnvelope> senderArgumentCaptor;

    @InjectMocks
    private HearingCommandApi hearingCommandApi;

    @Before
    public void setup() {
        apiMethodsToHandlerNames = apiMethodsToHandlerNames(HearingCommandApi.class);
        eventApiMethodsToHandlerNames = apiMethodsToHandlerNames(HearingEventCommandApi.class);
        notificationApiMethodsToHandlerNames = apiMethodsToHandlerNames(NotificationCommandApi.class);
    }

    @Test
    public void testActionNameAndHandlerNameAreSame() throws Exception {
        final List<String> allLines = FileUtils.readLines(new File(PATH_TO_RAML));
        final List<String> ramlActionNames = allLines.stream()
                .filter(action -> !action.isEmpty())
                .filter(line -> line.contains(NAME))
                .map(line -> line.replaceAll(NAME, "").trim())
                .collect(toList());

        final List<String> allHandlerNames = Stream.of(
                apiMethodsToHandlerNames.values().stream(),
                eventApiMethodsToHandlerNames.values().stream(),
                notificationApiMethodsToHandlerNames.values().stream())
                .reduce(Stream::concat)
                .orElseGet(Stream::empty)
                .collect(toList());

        assertThat(allHandlerNames, containsInAnyOrder(ramlActionNames.toArray()));
    }

    @Test
    public void testHandlerNamesPassThroughSender() {
        assertHandlerMethodsArePassThrough(HearingCommandApi.class, apiMethodsToHandlerNames.keySet().stream()
                .filter(methodName -> !NON_PASS_THROUGH_METHODS.contains(methodName))
                .collect(toMap(identity(), apiMethodsToHandlerNames::get)));
        assertHandlerMethodsArePassThrough(HearingEventCommandApi.class, eventApiMethodsToHandlerNames.keySet().stream()
                .filter(methodName -> !NON_PASS_THROUGH_METHODS.contains(methodName))
                .collect(toMap(identity(), eventApiMethodsToHandlerNames::get)));
        assertHandlerMethodsArePassThrough(NotificationCommandApi.class, notificationApiMethodsToHandlerNames.keySet().stream()
                .filter(methodName -> !NON_PASS_THROUGH_METHODS.contains(methodName))
                .collect(toMap(identity(), notificationApiMethodsToHandlerNames::get)));
    }

    @Test
    public void shouldPassthroughShareResultCommandToCommandHandler() {

        final JsonObject requestPayload = createObjectBuilder()
                .add("dummyField", "dummyFieldValue")
                .build();

        JsonEnvelope commandJsonEnvelope = envelopeFrom(metadataWithRandomUUIDAndName(), requestPayload);

        hearingCommandApi.shareResults(commandJsonEnvelope);

        verify(sender).send(senderArgumentCaptor.capture());

        final JsonEnvelope jsonEnvelopOut = senderArgumentCaptor.getValue();
        //check that payload was passed through and meta data name was changed
        assertThat(jsonEnvelopOut, is(jsonEnvelope(
                withMetadataEnvelopedFrom(commandJsonEnvelope).withName(COMMAND_SHARE_RESULTS),
                payloadIsJson(allOf(
                        withJsonPath("dummyField", equalTo("dummyFieldValue")
                        )))
        )));
    }

    private <T> void assertHandlerMethodsArePassThrough(final Class<T> commandApiClass, final Map<String, String> methodsToHandlerNamesMap) {
        for (final Map.Entry<String, String> entry : methodsToHandlerNamesMap.entrySet()) {
            assertThat(commandApiClass, isHandlerClass(COMMAND_API)
                    .with(method(entry.getKey())
                            .thatHandles(entry.getValue())
                            .withSenderPassThrough()));
        }
    }

    private Map<String, String> apiMethodsToHandlerNames(final Class<?> clazz) {
        return stream(clazz.getMethods())
                .filter(method -> method.getAnnotation(Handles.class) != null)
                .collect(toMap(Method::getName, method -> method.getAnnotation(Handles.class).value()));
    }
}
