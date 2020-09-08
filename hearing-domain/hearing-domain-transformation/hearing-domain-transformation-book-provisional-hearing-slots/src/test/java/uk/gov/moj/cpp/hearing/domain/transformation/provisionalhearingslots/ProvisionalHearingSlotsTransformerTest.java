package uk.gov.moj.cpp.hearing.domain.transformation.provisionalhearingslots;

import static java.util.UUID.randomUUID;
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
import static uk.gov.moj.cpp.hearing.domain.transformation.provisionalhearingslots.domain.EventToTransform.BOOK_PROVISIONAL_HEARING_SLOTS;

import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.tools.eventsourcing.transformation.api.Action;
import uk.gov.moj.cpp.hearing.domain.transformation.provisionalhearingslots.service.EventPayloadTransformer;

import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;
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
public class ProvisionalHearingSlotsTransformerTest {

    private static final String SLOTS_ATTRIBUTE = "slots";
    private static final String HEARING_ID_ATTRIBUTE = "hearingId";
    private static final String COURT_SCHEDULE_ID_ATTRIBUTE = "courtScheduleId";

    final UUID HEARING_ID = randomUUID();
    final UUID COURT_SCHEDULE_ID_1 = randomUUID();
    final UUID COURT_SCHEDULE_ID_2 = randomUUID();

    @Mock
    private EventPayloadTransformer eventPayloadTransformer;

    private ProvisionalHearingSlotsTransformer underTest;

    @DataProvider
    public static Object[][] validEventToTransform() {
        return new Object[][]{
                {BOOK_PROVISIONAL_HEARING_SLOTS.getEventName()}
        };
    }

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
        underTest = new ProvisionalHearingSlotsTransformer();

        final Field eventPayloadTransformerField = ProvisionalHearingSlotsTransformer.class.getDeclaredField("eventPayloadTransformer");
        eventPayloadTransformerField.setAccessible(true);
        eventPayloadTransformerField.set(underTest, eventPayloadTransformer);
    }

    @Test
    @UseDataProvider("validEventToTransform")
    public void shouldTransformValidEventThatHasSlotsInThePayload(final String eventToTransform) {
        final JsonEnvelope event = prepareWithEventAndSlotsToTransform(eventToTransform);

        final Action action = underTest.actionFor(event);

        assertThat(action, is(TRANSFORM));
    }

    @Test
    @UseDataProvider("validEventToTransform")
    public void shouldNotTransformAnInvalidEventThatHasCourtScheduleIdsInThePayload(final String eventToTransform) {
        final JsonEnvelope event = prepareWithCourtScheduleIdsToTransform(eventToTransform);

        final Action action = underTest.actionFor(event);

        assertThat(action, is(NO_ACTION));
    }

    @Test
    public void shouldNotTransformAnInvalidEventThatHasSlotsInThePayload() {
        final JsonEnvelope event = prepareWithEventAndSlotsToTransform(STRING.next());

        final Action action = underTest.actionFor(event);

        assertThat(action, is(NO_ACTION));
    }

    @Test
    public void shouldTransformIncomingEventAndReturnTransformedEvent() {
        final JsonEnvelope event = prepareWithEventAndSlotsToTransform(STRING.next());
        final JsonObject transformedPayload = createObjectBuilder().build();
        given(eventPayloadTransformer.transform(event.payloadAsJsonObject())).willReturn(transformedPayload);

        final Stream<JsonEnvelope> stream = underTest.apply(event);

        final List<JsonEnvelope> expectedEvents = stream.collect(toList());
        assertThat(expectedEvents.size(), is(equalTo(1)));
        assertThat(expectedEvents.get(0).payload(), is(transformedPayload));
        assertThat(expectedEvents.get(0).metadata(), is(event.metadata()));
    }

    private JsonEnvelope prepareWithEventAndSlotsToTransform(final String eventName) {
        return envelope()
                .with(metadataWithRandomUUID(eventName))
                .withPayloadOf(createArrayBuilder().build(), SLOTS_ATTRIBUTE)
                .build();
    }

    private JsonEnvelope prepareWithCourtScheduleIdsToTransform(final String eventName) {
        final JsonObject bookProvisionalHearingSlots = createObjectBuilder()
                .add(HEARING_ID_ATTRIBUTE, HEARING_ID.toString())
                .add(SLOTS_ATTRIBUTE, createArrayBuilder()
                        .add(createObjectBuilder().add(COURT_SCHEDULE_ID_ATTRIBUTE, COURT_SCHEDULE_ID_1.toString()))
                        .add(createObjectBuilder().add(COURT_SCHEDULE_ID_ATTRIBUTE, COURT_SCHEDULE_ID_2.toString())))
                .build();

        return envelope()
                .with(metadataWithRandomUUID(eventName))
                .withPayloadFrom(bookProvisionalHearingSlots)
                .build();
    }
}