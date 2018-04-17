package uk.gov.moj.cpp.hearing.command.api;

import static com.google.common.collect.Lists.newArrayList;
import static com.jayway.jsonassert.impl.matcher.IsCollectionWithSize.hasSize;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.UUID.randomUUID;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Stream.concat;
import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.verify;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_API;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataWithRandomUUIDAndName;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloper;
import static uk.gov.justice.services.test.utils.core.matchers.HandlerClassMatcher.isHandlerClass;
import static uk.gov.justice.services.test.utils.core.matchers.HandlerMethodMatcher.method;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher.jsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.withMetadataEnvelopedFrom;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payloadIsJson;
import static uk.gov.justice.services.test.utils.core.messaging.JsonEnvelopeBuilder.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;

import uk.gov.justice.services.common.converter.ZonedDateTimes;
import uk.gov.justice.services.common.util.Clock;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.test.utils.common.helper.StoppedClock;

import java.io.File;
import java.lang.reflect.Method;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;

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

@SuppressWarnings("unused")
@RunWith(MockitoJUnitRunner.class)
public class HearingCommandApiTest {

    private static final String PATH_TO_RAML = "src/raml/hearing-command-api.raml";
    private static final String NAME = "name:";

    private static final String FIELD_GENERIC_ID = "id";

    private static final String FIELD_HEARING_ID = "hearingId";
    private static final String FIELD_RESULT_LINES = "resultLines";
    private static final String FIELD_RESULT_LEVEL = "level";
    private static final String FIELD_RESULT_LABEL = "resultLabel";
    private static final String FIELD_RESULT_PROMPTS = "prompts";
    private static final String FIELD_LABEL = "label";
    private static final String FIELD_VALUE = "value";

    private static final String FIELD_PERSON_ID = "personId";
    private static final String FIELD_CASE_ID = "caseId";
    private static final String FIELD_OFFENCE_ID = "offenceId";
    private static final String FIELD_SHARED_TIME = "sharedTime";

    private static final UUID HEARING_ID = randomUUID();
    private static final UUID HEARING_RESULT_ID = randomUUID();
    private static final UUID HEARING_RESULT_ID_2 = randomUUID();
    private static final UUID PERSON_ID = randomUUID();
    private static final UUID OFFENCE_ID = randomUUID();
    private static final UUID OFFENCE_ID_2 = randomUUID();
    private static final UUID CASE_ID = randomUUID();
    private static final String RESULT_LEVEL = "OFFENCE";
    private static final String RESULT_LEVEL_2 = "DEFENDANT";
    private static final String RESULT_LABEL = STRING.next();
    private static final String RESULT_LABEL_2 = STRING.next();
    private static final String LABEL = STRING.next();
    private static final String VALUE = STRING.next();

    private static final String COMMAND_SHARE_RESULTS = "hearing.command.share-results";
    private static final List<String> NON_PASS_THROUGH_METHODS = newArrayList(
            "shareResults", "logHearingEvent", "correctEvent", "updatePlea", "updateVerdict", "addWitness");

    private Map<String, String> apiMethodsToHandlerNames;
    private Map<String, String> eventApiMethodsToHandlerNames;

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
    }

    @Test
    public void testActionNameAndHandlerNameAreSame() throws Exception {
        final List<String> allLines = FileUtils.readLines(new File(PATH_TO_RAML));
        final List<String> ramlActionNames = allLines.stream()
                .filter(action -> !action.isEmpty())
                .filter(line -> line.contains(NAME))
                .map(line -> line.replaceAll(NAME, "").trim())
                .collect(toList());

        final List<String> allHandlerNames = concat(apiMethodsToHandlerNames.values().stream(), eventApiMethodsToHandlerNames.values().stream()).collect(toList());

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
    }

    @Test
    public void shouldUpdateWithSharedTimeWhenResultsAreShared() {
        final JsonEnvelope command = prepareShareResultsCommand();

        hearingCommandApi.shareResults(command);

        verify(sender).send(senderArgumentCaptor.capture());
        assertThat(senderArgumentCaptor.getValue(), is(jsonEnvelope(
                withMetadataEnvelopedFrom(command).withName(COMMAND_SHARE_RESULTS),
                payloadIsJson(allOf(
                        withJsonPath(format("$.%s", FIELD_HEARING_ID), equalTo(HEARING_ID.toString())),
                        withJsonPath(format("$.%s", FIELD_SHARED_TIME), equalTo(ZonedDateTimes.toString(clock.now()))),
                        withJsonPath(format("$.%s", FIELD_RESULT_LINES), hasSize(2))
                )))
        ));
    }

    private JsonEnvelope prepareShareResultsCommand() {
        final JsonArrayBuilder resultLinesJson = createArrayBuilder()
                .add(createObjectBuilder()
                        .add(FIELD_GENERIC_ID, HEARING_RESULT_ID.toString())
                        .add(FIELD_OFFENCE_ID, OFFENCE_ID.toString())
                        .add(FIELD_CASE_ID, CASE_ID.toString())
                        .add(FIELD_PERSON_ID, PERSON_ID.toString())
                        .add(FIELD_RESULT_LEVEL, RESULT_LEVEL)
                        .add(FIELD_RESULT_LABEL, RESULT_LABEL)
                        .add(FIELD_RESULT_PROMPTS, createArrayBuilder()
                                .add(createObjectBuilder()
                                        .add(FIELD_LABEL, LABEL)
                                        .add(FIELD_VALUE, VALUE)
                                )))
                .add(createObjectBuilder()
                        .add(FIELD_GENERIC_ID, HEARING_RESULT_ID_2.toString())
                        .add(FIELD_OFFENCE_ID, OFFENCE_ID_2.toString())
                        .add(FIELD_CASE_ID, CASE_ID.toString())
                        .add(FIELD_PERSON_ID, PERSON_ID.toString())
                        .add(FIELD_RESULT_LEVEL, RESULT_LEVEL_2)
                        .add(FIELD_RESULT_LABEL, RESULT_LABEL_2)
                        .add(FIELD_RESULT_PROMPTS, createArrayBuilder()
                                .add(createObjectBuilder()
                                        .add(FIELD_LABEL, LABEL)
                                        .add(FIELD_VALUE, VALUE)
                                )));

        final JsonObjectBuilder requestPayload = createObjectBuilder()
                .add(FIELD_HEARING_ID, HEARING_ID.toString())
                .add(FIELD_RESULT_LINES, resultLinesJson);

        return envelopeFrom(metadataWithRandomUUIDAndName(), requestPayload.build());
    }

    private <T> void assertHandlerMethodsArePassThrough(final Class<T> commandApiClass, final Map<String, String> methodsToHandlerNamesMap) {
        for (final Map.Entry<String, String> entry : methodsToHandlerNamesMap.entrySet()) {
            assertThat(commandApiClass, isHandlerClass(COMMAND_API)
                    .with(method(entry.getKey())
                            .thatHandles(entry.getValue())
                            .withSenderPassThrough()));
        }
    }

    private Map<String, String> apiMethodsToHandlerNames(final Class clazz) {
        return stream(clazz.getMethods())
                .filter(method -> method.getAnnotation(Handles.class) != null)
                .collect(toMap(Method::getName, method -> method.getAnnotation(Handles.class).value()));
    }
}
