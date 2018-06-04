package uk.gov.moj.cpp.hearing.command.api;

import static uk.gov.justice.services.core.annotation.Component.COMMAND_API;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;

@ServiceComponent(COMMAND_API)
public class HearingCommandApi {

    private final Sender sender;
    private final Enveloper enveloper;
    private static final Logger LOGGER = LoggerFactory.getLogger(HearingCommandApi.class);

    @Inject
    public HearingCommandApi(final Sender sender, final Enveloper enveloper) {
        this.sender = sender;
        this.enveloper = enveloper;
    }

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

    @Handles("hearing.add-update-witness")
    public void addWitness(final JsonEnvelope command) {
        this.sender.send(this.enveloper
                        .withMetadataFrom(command, "hearing.command.add-update-witness")
                        .apply(command.payloadAsJsonObject()));
    }

    @Handles("hearing.generate-nows")
    public void generateNows(final JsonEnvelope command) {
        this.sender.send(this.enveloper.withMetadataFrom(command, "hearing.command.generate-nows").apply(command.payloadAsJsonObject()));
    }

    @Handles("hearing.update-nows-material-status")
    public void updateNowsMaterialStatus(final JsonEnvelope command) {
        this.sender.send(this.enveloper.withMetadataFrom(command, "hearing.command.update-nows-material-status").apply(command.payloadAsJsonObject()));
    }
    @Handles("hearing.generate-nows.v2")
    public void generateNowsV2(final JsonEnvelope command) {
        LOGGER.info("******* generateNowsV2 hearing.generate-nows.v2 N/A");
    }

    @Handles("hearing.share-results")
    public void shareResults(final JsonEnvelope envelope) {
        this.sender.send(this.enveloper.withMetadataFrom(envelope, "hearing.command.share-results").apply(envelope.payloadAsJsonObject()));
    }
}