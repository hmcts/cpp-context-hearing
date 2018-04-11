package uk.gov.moj.cpp.hearing.event;


import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonValueConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.RecordMagsCourtHearingCommand;
import uk.gov.moj.cpp.hearing.domain.event.SendingSheetCompletedRecorded;

import javax.inject.Inject;

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
}
