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

@SuppressWarnings("WeakerAccess")
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

    @Handles("hearing.initiate")
    public void initiateHearing(final JsonEnvelope envelope) {
        this.sender.send(envelope);
    }

    @Handles("hearing.save-draft-result")
    public void saveDraftResult(final JsonEnvelope envelope) {
        this.sender.send(envelope);
    }

    @Handles("hearing.add-prosecution-counsel")
    public void addProsecutionCounsel(final JsonEnvelope envelope) {
        this.sender.send(envelope);
    }

    @Handles("hearing.add-defence-counsel")
    public void addDefenceCounsel(final JsonEnvelope envelope) {
        this.sender.send(envelope);
    }

    //TODO - CLEANUP - why have the new event name. - why not just keep hearing.offence-plea-update?
    @Handles("hearing.update-plea")
    public void updatePlea(final JsonEnvelope command) {
        this.sender.send(this.enveloper.withMetadataFrom(command, "hearing.offence-plea-update").apply(command.payloadAsJsonObject()));
    }

    @Handles("hearing.update-verdict")
    public void updateVerdict(final JsonEnvelope command) {
        this.sender.send(this.enveloper.withMetadataFrom(command, "hearing.command.update-verdict").apply(command.payloadAsJsonObject()));
    }

    @Handles(COMMAND_SHARE_RESULTS)
    public void shareResults(final JsonEnvelope command) {
        final JsonObject payload = command.payloadAsJsonObject();
        final JsonObjectBuilder payloadWithSharedTime = createObjectBuilder()
                .add(FIELD_HEARING_ID, payload.getString(FIELD_HEARING_ID))
                .add(FIELD_SHARED_TIME, ZonedDateTimes.toString(this.clock.now()))
                .add(FIELD_RESULT_LINES, payload.getJsonArray(FIELD_RESULT_LINES));

        this.sender.send(this.enveloper.withMetadataFrom(command, "hearing.command.share-results").apply(payloadWithSharedTime.build()));
    }
}
