package uk.gov.moj.cpp.hearing.event;


import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonValueConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.external.domain.progression.sendingsheetcompleted.Defendant;
import uk.gov.moj.cpp.external.domain.progression.sendingsheetcompleted.Offence;
import uk.gov.moj.cpp.external.domain.progression.sendingsheetcompleted.PleaValue;
import uk.gov.moj.cpp.hearing.command.RecordMagsCourtHearingCommand;
import uk.gov.moj.cpp.hearing.domain.event.PleaUpsert;
import uk.gov.moj.cpp.hearing.domain.event.MagsCourtHearingRecorded;
import uk.gov.moj.cpp.hearing.domain.event.SendingSheetCompletedRecorded;

import javax.inject.Inject;

import static javax.json.Json.createObjectBuilder;
import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;

@ServiceComponent(EVENT_PROCESSOR)
public class MagistratesCourtInitiateHearingEventProcessor {

    @Inject
    private Enveloper enveloper;

    @Inject
    private Sender sender;

    @Inject
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Inject
    private ObjectToJsonValueConverter objectToJsonValueConverter;

    @Handles("public.progression.events.sending-sheet-completed")
    public void recordSendSheetCompleted(final JsonEnvelope event) {

        sender.send(enveloper.withMetadataFrom(event, "hearing.record-sending-sheet-complete")
                .apply(event.payloadAsJsonObject()));
    }

    @Handles("hearing.sending-sheet-recorded")
    public void processSendingSheetRecordedRecordMags(final JsonEnvelope event) {

        final SendingSheetCompletedRecorded sendingSheetCompletedRecorded = this.jsonObjectToObjectConverter
                .convert(event.payloadAsJsonObject(), SendingSheetCompletedRecorded.class);

        this.sender.send(this.enveloper.withMetadataFrom(event, "hearing.record-mags-court-hearing")
                .apply(this.objectToJsonValueConverter.convert(new RecordMagsCourtHearingCommand(sendingSheetCompletedRecorded.getHearing()))));
    }

    @Handles("hearing.mags-court-hearing-recorded")
    public void processMagistratesCourtHearing(final JsonEnvelope event) {

        final MagsCourtHearingRecorded magsCourtHearingRecorded = this.jsonObjectToObjectConverter
                .convert(event.payloadAsJsonObject(), MagsCourtHearingRecorded.class);

        this.sender.send(this.enveloper.withMetadataFrom(event, "public.mags.hearing.initiated").apply(createObjectBuilder()
                .add("hearingId", magsCourtHearingRecorded.getHearingId().toString())
                .add("caseId", magsCourtHearingRecorded.getOriginatingHearing().getCaseId().toString())
                .build()));


        for (Defendant defendant : magsCourtHearingRecorded.getOriginatingHearing().getDefendants()) {
            for (Offence offence : defendant.getOffences()) {

                if (offence.getPlea() == null || offence.getPlea().getValue() == PleaValue.NOT_GUILTY) {
                    continue;
                }

                final PleaUpsert pleaUpsert = PleaUpsert.builder()
                        .withHearingId(magsCourtHearingRecorded.getHearingId())
                        .withOffenceId(offence.getId())
                        .withPleaDate(offence.getPlea().getPleaDate())
                        .withValue(offence.getPlea().getValue().toString())
                        .build();

                this.sender.send(this.enveloper.withMetadataFrom(event, "hearing.offence-plea-updated")
                        .apply(this.objectToJsonValueConverter.convert(pleaUpsert)));
            }
        }
    }
}
