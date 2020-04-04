package uk.gov.moj.cpp.hearing.domain.transformation.ctl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.tools.eventsourcing.transformation.api.EventTransformation;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import static java.util.UUID.randomUUID;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertEquals;

import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.spi.DefaultJsonMetadata.metadataBuilder;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloper;
import static uk.gov.justice.tools.eventsourcing.transformation.api.Action.NO_ACTION;
import static uk.gov.moj.cpp.hearing.domain.transformation.ctl.core.SchemaVariableConstants.DEFENDANT_DETAILS_UPDATED;
import static uk.gov.moj.cpp.hearing.domain.transformation.ctl.core.SchemaVariableConstants.HEARING_INITIATED;
import static uk.gov.moj.cpp.hearing.domain.transformation.ctl.core.SchemaVariableConstants.MAGS_COURT_HEARING_RECORDED;
import static uk.gov.moj.cpp.hearing.domain.transformation.ctl.core.SchemaVariableConstants.NOWS_REQUESTED;
import static uk.gov.moj.cpp.hearing.domain.transformation.ctl.core.SchemaVariableConstants.PENDING_NOWS_REQUESTED;
import static uk.gov.moj.cpp.hearing.domain.transformation.ctl.core.SchemaVariableConstants.RESULTS_SHARED;
import static uk.gov.moj.cpp.hearing.domain.transformation.ctl.core.SchemaVariableConstants.SENDING_SHEET_RECORDED;
import static uk.gov.moj.cpp.hearing.domain.transformation.ctl.core.SchemaVariableConstants.UPDATE_CASE_DEFENDANT_DETAILS_ENRICHED_HEARING_IDS;

import java.io.InputStream;
import java.util.function.Function;


@RunWith(MockitoJUnitRunner.class)
public class HearingEventStreamTransformTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(HearingEventStreamTransformTest.class);

    private HearingEventStreamTransform target = new HearingEventStreamTransform();

    private Enveloper enveloper = createEnveloper();

    @Before
    public void setup() {
        target.setEnveloper(enveloper);
    }

    @Test
    public void shouldCreateInstanceOfEventTransformation() {
        assertThat(target, is(instanceOf(EventTransformation.class)));
    }

    @Test
    public void shouldSetActionToTransformForTheEventsThatMatch() {
     /* TODO   final JsonEnvelope event = buildEnvelope(HEARING_EVENTS_INITIATED);
        assertThat(underTest.actionFor(event), is(TRANSFORM));
     */
    }


    @Test
    public void shouldSetActionToNoActionForTheEventsThatDoesNotMatch() {
        final JsonEnvelope event = buildEnvelope("hearing.events.other");
        assertThat(target.actionFor(event), is(NO_ACTION));
    }

    @Test
    public void shouldTransformDefendantDetailsUpdated() {
        shouldTransformBailStatus(DEFENDANT_DETAILS_UPDATED, payload -> payload.getJsonObject("defendant").getJsonObject("personDefendant")
                .getJsonObject("bailStatus"), "U");
    }


    @Test
    public void shouldTransformHearingInitiaitedEvent() {
        shouldTransformBailStatus(HEARING_INITIATED, extractFromHearing, "U");
    }

    @Test
    public void shouldTransformNowsRequested() {
        shouldTransformBailStatus(NOWS_REQUESTED, extractFromNowsRequest, "C");
    }

    @Test
    public void shouldTransformPendingNowsRequested() {
        shouldTransformBailStatus(PENDING_NOWS_REQUESTED, extractFromNowsRequest, "C");
    }

    @Test
    public void shouldTransformMagsCourtHearingRecorded() {
        shouldTransformBailStatus(MAGS_COURT_HEARING_RECORDED, payload -> payload
                        .getJsonObject("originatingHearing")
                        .getJsonArray("defendants").getJsonObject(0)
                        .getJsonObject("bailStatus")
                , "U");
    }

    @Test
    public void shouldTransformResultsShared() {
        shouldTransformBailStatus(RESULTS_SHARED, extractFromHearing, "C");
    }

    @Test
    public void shouldTransformSendingSheetRecorded() {
        shouldTransformBailStatus(SENDING_SHEET_RECORDED, payload -> payload
                        .getJsonObject("hearing")
                        .getJsonArray("defendants").getJsonObject(0)
                        .getJsonObject("bailStatus")
                , "U");
    }

    @Test
    public void shouldTransformUpdateCaseDefendantDetailsEnrichedWithHearingIds() {
        shouldTransformBailStatus(UPDATE_CASE_DEFENDANT_DETAILS_ENRICHED_HEARING_IDS, payload -> payload
                        .getJsonObject("defendant")
                        .getJsonObject("personDefendant")
                        .getJsonObject("bailStatus")
                , "B");
    }

    private Function<JsonObject, JsonObject> extractFromHearing = payload -> payload.getJsonObject("hearing")
            .getJsonArray("prosecutionCases").getJsonObject(0)
            .getJsonArray("defendants").getJsonObject(0)
            .getJsonObject("personDefendant")
            .getJsonObject("bailStatus");

    private Function<JsonObject, JsonObject> extractFromNowsRequest = payload -> extractFromHearing.apply(
            payload.getJsonObject("createNowsRequest"));

    private void shouldTransformBailStatus(String eventName, Function<JsonObject, JsonObject> extractBailStatus, String expectedCode) {
        final JsonEnvelope inputEnvelope = loadTestFile(eventName, eventName);

        final JsonEnvelope result = target.apply(inputEnvelope).findFirst().get();

        JsonObject bailStatusOut = extractBailStatus.apply(result.payloadAsJsonObject());

        System.out.println(eventName + " bailStatusOut: " + bailStatusOut);

        assertEquals(expectedCode, bailStatusOut.getString("code"));

    }
    
    private JsonEnvelope buildEnvelope(final String eventName, final JsonObject jsonPayload) {
        return envelopeFrom(metadataBuilder().withId(randomUUID()).withName(eventName), jsonPayload);
    }

    private JsonEnvelope buildEnvelope(final String eventName) {
        return envelopeFrom(
                metadataBuilder().withId(randomUUID()).withName(eventName),
                createObjectBuilder().add("field", "value").build());
    }

    private JsonEnvelope loadTestFile(String eventName, String resourceFileName) {
        try {
            InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceFileName + ".json");
            final JsonReader jsonReader = Json.createReader(is);
            final JsonObject payload = jsonReader.readObject();
            return envelopeFrom(metadataBuilder().withId(randomUUID()).withName(eventName), payload);

        } catch (Exception ex) {
            throw new RuntimeException("failed to load test file " + resourceFileName, ex);
        }
    }
}