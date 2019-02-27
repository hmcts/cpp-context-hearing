package uk.gov.moj.cpp.hearing.command.handler;

import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;

import uk.gov.justice.core.courts.HearingCaseNote;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.moj.cpp.hearing.command.casenote.SaveHearingCaseNote;


@ServiceComponent(COMMAND_HANDLER)
public class SaveHearingCaseNoteCommandHandler extends AbstractCommandHandler {

    @Handles("hearing.command.save-hearing-case-note")
    public void saveHearingCaseNote(final Envelope<SaveHearingCaseNote> envelope) throws EventStreamException {
        final HearingCaseNote command = envelope.payload().getHearingCaseNote();
        aggregate(
                uk.gov.moj.cpp.hearing.domain.aggregate.HearingCaseNote.class,
                command.getOriginatingHearingId(),
                envelope,
                aggregate -> aggregate.saveCaseNote(command));
    }

}
