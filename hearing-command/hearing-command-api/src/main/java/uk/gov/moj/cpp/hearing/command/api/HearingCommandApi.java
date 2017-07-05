package uk.gov.moj.cpp.hearing.command.api;

import static javax.json.Json.createObjectBuilder;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_API;

import uk.gov.justice.services.common.converter.ZonedDateTimes;
import uk.gov.justice.services.common.util.Clock;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.inject.Inject;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

@ServiceComponent(COMMAND_API)
public class HearingCommandApi {

    private static final String COMMAND_SHARE_RESULTS = "hearing.share-results";

    private static final String FIELD_HEARING_ID = "hearingId";
    private static final String FIELD_SHARED_TIME = "sharedTime";
    private static final String FIELD_RESULT_LINES = "resultLines";

    @Inject
    private Sender sender;

    @Inject
    private Enveloper enveloper;

    @Inject
    private Clock clock;

    @Handles("hearing.initiate-hearing")
    public void initiateHearing(final JsonEnvelope envelope) {
        sender.send(envelope);
    }

    @Handles("hearing.allocate-court")
    public void allocateCourt(final JsonEnvelope envelope) {
        sender.send(envelope);
    }

    @Handles("hearing.book-room")
    public void bookRoom(final JsonEnvelope envelope) {
        sender.send(envelope);
    }

    @Handles("hearing.add-case")
    public void addCase(final JsonEnvelope envelope) {
        sender.send(envelope);
    }

    @Handles("hearing.adjourn-date")
    public void adjournHearingDate(final JsonEnvelope envelope) {
        sender.send(envelope);
    }

    @Handles("hearing.save-draft-result")
    public void saveDraftResult(final JsonEnvelope envelope) {
        sender.send(envelope);
    }

    @Handles("hearing.add-prosecution-counsel")
    public void addProsecutionCounsel(final JsonEnvelope envelope) {
        sender.send(envelope);
    }

    @Handles("hearing.add-defence-counsel")
    public void addDefenceCounsel(final JsonEnvelope envelope) {
        sender.send(envelope);
    }

    @Handles(COMMAND_SHARE_RESULTS)
    public void shareResults(final JsonEnvelope command) {
        final JsonObject payload = command.payloadAsJsonObject();
        final JsonObjectBuilder payloadWithSharedTime = createObjectBuilder()
                .add(FIELD_HEARING_ID, payload.getString(FIELD_HEARING_ID))
                .add(FIELD_SHARED_TIME, ZonedDateTimes.toString(clock.now()))
                .add(FIELD_RESULT_LINES, payload.getJsonArray(FIELD_RESULT_LINES));

        sender.send(enveloper.withMetadataFrom(command, COMMAND_SHARE_RESULTS).apply(payloadWithSharedTime.build()));
    }
}
