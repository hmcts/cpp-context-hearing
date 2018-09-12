package uk.gov.moj.cpp.hearing.event.delegates;

import uk.gov.justice.json.schemas.core.ResultLine;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.result.CompletedResultLine;
import uk.gov.moj.cpp.hearing.command.result.CompletedResultLineStatus;
import uk.gov.moj.cpp.hearing.command.result.CourtClerk;
import uk.gov.moj.cpp.hearing.command.result.SharedResultLineId;
import uk.gov.moj.cpp.hearing.command.result.UpdateResultLinesStatusCommand;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsShared;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

public class UpdateResultLineStatusDelegate {

    private final Enveloper enveloper;

    private final ObjectToJsonObjectConverter objectToJsonObjectConverter;

    private List<ResultLine> getCompletedResultLines(final ResultsShared resultsShared) {
        return resultsShared.getHearing().getTargets().stream().flatMap(target->target.getResultLines().stream()).collect(Collectors.toList());
    }

    @Inject
    public UpdateResultLineStatusDelegate(final Enveloper enveloper, final ObjectToJsonObjectConverter objectToJsonObjectConverter) {
        this.enveloper = enveloper;
        this.objectToJsonObjectConverter = objectToJsonObjectConverter;
    }

    public void updateResultLineStatus(final Sender sender, final JsonEnvelope event, final ResultsShared resultsShared) {
        final UpdateResultLinesStatusCommand updateResultLinesStatusCommand = UpdateResultLinesStatusCommand.builder()
                .withLastSharedDateTime(resultsShared.getSharedTime())
                .withHearingId(resultsShared.getHearingId())
                //TODO GPE-5480  update the command with UpdateResultLinesStatusCommand global CourtClerk domain object
                .withCourtClerk(CourtClerk.builder()
                        .withFirstName(resultsShared.getCourtClerk().getFirstName())
                        .withLastName(resultsShared.getCourtClerk().getLastName())
                        .withId(resultsShared.getCourtClerk().getId())
                        .build()
                        )
                .withSharedResultLines(mapSharedResultsLinesStatus(getCompletedResultLines(resultsShared), resultsShared.getCompletedResultLinesStatus()))
                .build();
        sender.send(this.enveloper.withMetadataFrom(event, "hearing.command.update-result-lines-status")
                .apply((this.objectToJsonObjectConverter.convert(updateResultLinesStatusCommand)))
        );
    }

    private List<SharedResultLineId> mapSharedResultsLinesStatus(final List<ResultLine> completedResultLines, final Map<UUID, CompletedResultLineStatus> completedResultLinesStatus) {
        return completedResultLines.stream()
                .filter(crl -> completedResultLinesStatus.get(crl.getResultLineId()) == null
                        || completedResultLinesStatus.get(crl.getResultLineId()).getLastSharedDateTime() == null)
                .map(status -> SharedResultLineId.builder()
                        .withSharedResultLineId(status.getResultLineId())
                        .build()
                )
                .collect(toList());
    }
}
