package uk.gov.moj.cpp.hearing.domain.transformation.judicialresult;

import static java.util.stream.Collectors.toList;
import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.BDDMockito.given;
import static uk.gov.justice.services.test.utils.core.messaging.JsonEnvelopeBuilder.envelope;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.justice.tools.eventsourcing.transformation.api.Action.NO_ACTION;
import static uk.gov.justice.tools.eventsourcing.transformation.api.Action.TRANSFORM;
import static uk.gov.moj.cpp.hearing.domain.transformation.judicialresult.domain.EventToTransform.APPLICATION_DETAIL_CHANGED;
import static uk.gov.moj.cpp.hearing.domain.transformation.judicialresult.domain.EventToTransform.CASE_DEFENDANTS_UPDATED;
import static uk.gov.moj.cpp.hearing.domain.transformation.judicialresult.domain.EventToTransform.CASE_DEFENDANTS_UPDATED_FOR_HEARING;
import static uk.gov.moj.cpp.hearing.domain.transformation.judicialresult.domain.EventToTransform.HEARING_EXTENDED;
import static uk.gov.moj.cpp.hearing.domain.transformation.judicialresult.domain.EventToTransform.HEARING_INITIATED;
import static uk.gov.moj.cpp.hearing.domain.transformation.judicialresult.domain.EventToTransform.NOWS_REQUESTED;
import static uk.gov.moj.cpp.hearing.domain.transformation.judicialresult.domain.EventToTransform.PENDING_NOWS_REQUESTED;
import static uk.gov.moj.cpp.hearing.domain.transformation.judicialresult.domain.EventToTransform.RESULTS_SHARED;

import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.tools.eventsourcing.transformation.api.Action;
import uk.gov.moj.cpp.hearing.domain.transformation.judicialresult.service.EventPayloadTransformer;

import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Stream;

import javax.json.JsonObject;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@RunWith(DataProviderRunner.class)
public class JudicialResultsTransformerTest {

    private static final String JUDICIAL_RESULTS_KEYWORD = "judicialResults";

    @Mock
    private EventPayloadTransformer eventPayloadTransformer;

    private JudicialResultsTransformer underTest;

    @DataProvider
    public static Object[][] validEventToTransform() {
        return new Object[][]{
                {RESULTS_SHARED.getEventName()},
                {PENDING_NOWS_REQUESTED.getEventName()},
                {NOWS_REQUESTED.getEventName()},
                {HEARING_INITIATED.getEventName()},
                {HEARING_EXTENDED.getEventName()},
                {APPLICATION_DETAIL_CHANGED.getEventName()},
                {CASE_DEFENDANTS_UPDATED_FOR_HEARING.getEventName()},
                {CASE_DEFENDANTS_UPDATED.getEventName()}
        };
    }

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
        underTest = new JudicialResultsTransformer();

        final Field eventPayloadTransformerField = JudicialResultsTransformer.class.getDeclaredField("eventPayloadTransformer");
        eventPayloadTransformerField.setAccessible(true);
        eventPayloadTransformerField.set(underTest, eventPayloadTransformer);
    }

    @Test
    @UseDataProvider("validEventToTransform")
    public void shouldTransformValidEventThatHasJudicialResultsInThePayload(final String eventToTransform) {
        final JsonEnvelope event = prepareWithEventAndJudicialResultsToTransform(eventToTransform);

        final Action action = underTest.actionFor(event);

        assertThat(action, is(TRANSFORM));
    }

    @Test
    public void shouldNotTransformAnInvalidEventThatHasJudicialResultsInThePayload() {
        final JsonEnvelope event = prepareWithEventAndJudicialResultsToTransform(STRING.next());

        final Action action = underTest.actionFor(event);

        assertThat(action, is(NO_ACTION));
    }

    @Test
    @UseDataProvider("validEventToTransform")
    public void shouldNotTransformValidEventWhenJudicialResultsIsNotInThePayload(final String eventToTransform) {
        final JsonEnvelope event = prepareWithEventWithoutJudicialResultsToTransform(eventToTransform);

        final Action action = underTest.actionFor(event);

        assertThat(action, is(NO_ACTION));
    }

    @Test
    public void shouldTransformIncomingEventAndReturnTransformedEvent() {
        final JsonEnvelope event = prepareWithEventAndJudicialResultsToTransform(STRING.next());
        final JsonObject transformedPayload = createObjectBuilder().build();
        given(eventPayloadTransformer.transform(event)).willReturn(transformedPayload);

        final Stream<JsonEnvelope> stream = underTest.apply(event);

        final List<JsonEnvelope> expectedEvents = stream.collect(toList());
        assertThat(expectedEvents.size(), is(equalTo(1)));
        assertThat(expectedEvents.get(0).payload(), is(transformedPayload));
        assertThat(expectedEvents.get(0).metadata(), is(event.metadata()));
    }

    private JsonEnvelope prepareWithEventAndJudicialResultsToTransform(final String eventName) {
        return envelope()
                .with(metadataWithRandomUUID(eventName))
                .withPayloadOf(createArrayBuilder().build(), JUDICIAL_RESULTS_KEYWORD)
                .build();
    }

    private JsonEnvelope prepareWithEventWithoutJudicialResultsToTransform(final String eventName) {
        return envelope()
                .with(metadataWithRandomUUID(eventName))
                .withPayloadOf(createArrayBuilder().build(), STRING.next())
                .build();
    }
}