package uk.gov.moj.cpp.hearing.command.api;

import static javax.json.Json.createObjectBuilder;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_API;

import javax.inject.Inject;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import uk.gov.justice.services.common.converter.ZonedDateTimes;
import uk.gov.justice.services.common.util.Clock;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;

@ServiceComponent(COMMAND_API)
public class HearingCommandApi {

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

    @Handles("hearing.update-plea")
    public void updatePlea(final JsonEnvelope command) {
        this.sender.send(this.enveloper.withMetadataFrom(command, "hearing.hearing-offence-plea-update").apply(command.payloadAsJsonObject()));
    }

    @Handles("hearing.update-verdict")
    public void updateVerdict(final JsonEnvelope command) {
        this.sender.send(this.enveloper.withMetadataFrom(command, "hearing.command.update-verdict").apply(command.payloadAsJsonObject()));
    }

    @Handles("hearing.add-witness")
    public void addWitness(final JsonEnvelope command) {
        this.sender.send(this.enveloper.withMetadataFrom(command, "hearing.command.add-witness").apply(command.payloadAsJsonObject()));
    }

    @Handles("hearing.generate-nows")
    public void generateNows(final JsonEnvelope command) {
        this.sender.send(this.enveloper.withMetadataFrom(command, "hearing.command.generate-nows").apply(command.payloadAsJsonObject()));
    }

    @Handles("hearing.share-results")
    public void shareResults(final JsonEnvelope command) {
        final JsonObject payload = command.payloadAsJsonObject();
        final JsonObjectBuilder payloadWithSharedTime = createObjectBuilder()
                .add("hearingId", payload.getString("hearingId"))
                .add("sharedTime", ZonedDateTimes.toString(this.clock.now()))
                .add("resultLines", payload.getJsonArray("resultLines"));
        this.sender.send(this.enveloper.withMetadataFrom(command, "hearing.command.share-results").apply(payloadWithSharedTime.build()));
    }
    
}
