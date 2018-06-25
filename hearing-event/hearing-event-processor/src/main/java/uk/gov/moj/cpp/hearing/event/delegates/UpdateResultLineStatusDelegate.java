package uk.gov.moj.cpp.hearing.event.delegates;

import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.result.CompletedResultLine;
import uk.gov.moj.cpp.hearing.command.result.CompletedResultLineStatus;
import uk.gov.moj.cpp.hearing.command.result.SharedResultLineId;
import uk.gov.moj.cpp.hearing.command.result.UpdateResultLinesStatusCommand;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsShared;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.stream.Collectors.toList;

public class UpdateResultLineStatusDelegate {

    private final Enveloper enveloper;

    private final ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @Inject
    public UpdateResultLineStatusDelegate(final Enveloper enveloper, final ObjectToJsonObjectConverter objectToJsonObjectConverter) {
        this.enveloper = enveloper;
        this.objectToJsonObjectConverter = objectToJsonObjectConverter;
    }

    public void updateResultLineStatus(final Sender sender, final JsonEnvelope event, final ResultsShared resultsShared) {
        final UpdateResultLinesStatusCommand updateResultLinesStatusCommand = UpdateResultLinesStatusCommand.builder()
                .withLastSharedDateTime(resultsShared.getSharedTime())
                .withHearingId(resultsShared.getHearingId())
                .withCourtClerk(resultsShared.getCourtClerk())
                .withSharedResultLines(mapSharedResultsLinesStatus(resultsShared.getCompletedResultLines(), resultsShared.getCompletedResultLinesStatus()))
                .build();
        sender.send(this.enveloper.withMetadataFrom(event, "hearing.command.update-result-lines-status")
                .apply((this.objectToJsonObjectConverter.convert(updateResultLinesStatusCommand)))
        );
    }

    private List<SharedResultLineId> mapSharedResultsLinesStatus(final List<CompletedResultLine> completedResultLines, final Map<UUID, CompletedResultLineStatus> completedResultLinesStatus) {
        return completedResultLines.stream()
                .filter(crl -> completedResultLinesStatus.get(crl.getId()).getLastSharedDateTime() == null)
                .map(status -> SharedResultLineId.builder()
                        .withSharedResultLineId(status.getId())
                        .build()
                )
                .collect(toList());
    }
}
