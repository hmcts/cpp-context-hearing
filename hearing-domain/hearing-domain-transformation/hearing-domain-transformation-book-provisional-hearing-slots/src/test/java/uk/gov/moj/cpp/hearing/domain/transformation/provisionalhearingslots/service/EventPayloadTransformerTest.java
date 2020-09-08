package uk.gov.moj.cpp.hearing.domain.transformation.provisionalhearingslots.service;

import static java.util.UUID.randomUUID;
import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.rules.ExpectedException.none;
import static uk.gov.justice.services.test.utils.core.messaging.JsonEnvelopeBuilder.envelope;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.randomEnum;
import static uk.gov.moj.cpp.hearing.domain.transformation.provisionalhearingslots.domain.EventToTransform.BOOK_PROVISIONAL_HEARING_SLOTS;

import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.transformation.provisionalhearingslots.domain.EventToTransform;

import java.util.UUID;

import javax.json.JsonObject;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

@RunWith(DataProviderRunner.class)
public class EventPayloadTransformerTest {

    private static final String SLOTS_ATTRIBUTE = "slots";
    private static final String HEARING_ID_ATTRIBUTE = "hearingId";
    private static final String COURT_SCHEDULE_ID_ATTRIBUTE = "courtScheduleId";

    final UUID HEARING_ID = randomUUID();
    final UUID COURT_SCHEDULE_ID_1 = randomUUID();
    final UUID COURT_SCHEDULE_ID_2 = randomUUID();

    @Rule
    public ExpectedException expectedException = none();

    @DataProvider
    public static Object[][] validEventToTransform() {
        return new Object[][]{
                {BOOK_PROVISIONAL_HEARING_SLOTS.getEventName()}
        };
    }

    private final EventPayloadTransformer underTest = new EventPayloadTransformer();

    @Test
    public void shouldEvaluateTransformation() {
        final JsonEnvelope event = prepareValidEventToTransform();

        final JsonObject transformedPayload = underTest.transform(event.payloadAsJsonObject());

        assertThat(transformedPayload.getString(HEARING_ID_ATTRIBUTE), equalTo(HEARING_ID.toString()));
        assertThat(transformedPayload.getJsonArray(SLOTS_ATTRIBUTE).getJsonObject(0).getString(COURT_SCHEDULE_ID_ATTRIBUTE), equalTo(COURT_SCHEDULE_ID_1.toString()));
        assertThat(transformedPayload.getJsonArray(SLOTS_ATTRIBUTE).getJsonObject(1).getString(COURT_SCHEDULE_ID_ATTRIBUTE), equalTo(COURT_SCHEDULE_ID_2.toString()));
    }

    private JsonEnvelope prepareValidEventToTransform() {
        final JsonObject bookProvisionalHearingSlots = createObjectBuilder()
                .add(HEARING_ID_ATTRIBUTE, HEARING_ID.toString())
                .add(SLOTS_ATTRIBUTE, createArrayBuilder()
                        .add(COURT_SCHEDULE_ID_1.toString())
                        .add(COURT_SCHEDULE_ID_2.toString()))
                .build();

        return envelope()
                .with(metadataWithRandomUUID(randomEnum(EventToTransform.class).next().getEventName()))
                .withPayloadFrom(bookProvisionalHearingSlots)
                .build();
    }
}