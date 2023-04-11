package uk.gov.moj.cpp.hearing.event.delegates;

import static java.util.stream.Collectors.toList;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.JsonEnvelope.metadataFrom;

import uk.gov.justice.core.courts.DelegatedPowers;
import uk.gov.justice.core.courts.ResultLine2;
import uk.gov.justice.core.courts.Target2;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.result.CompletedResultLineStatus;
import uk.gov.moj.cpp.hearing.command.result.SharedResultLineId;
import uk.gov.moj.cpp.hearing.command.result.UpdateDaysResultLinesStatusCommand;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsSharedV3;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Inject;

public class UpdateResultLineStatusDelegateV3 {

    private final Enveloper enveloper;

    private final ObjectToJsonObjectConverter objectToJsonObjectConverter;


    @Inject
    public UpdateResultLineStatusDelegateV3(final Enveloper enveloper, final ObjectToJsonObjectConverter objectToJsonObjectConverter) {
        this.enveloper = enveloper;
        this.objectToJsonObjectConverter = objectToJsonObjectConverter;
    }



    public void updateDaysResultLineStatus(final Sender sender, final JsonEnvelope event, final ResultsSharedV3 resultsShared) {
        final UpdateDaysResultLinesStatusCommand updateDaysResultLinesStatusCommand = UpdateDaysResultLinesStatusCommand.builder()
                .withLastSharedDateTime(resultsShared.getSharedTime())
                .withHearingId(resultsShared.getHearingId())
                .withCourtClerk(DelegatedPowers.delegatedPowers()
                        .withFirstName(resultsShared.getCourtClerk().getFirstName())
                        .withLastName(resultsShared.getCourtClerk().getLastName())
                        .withUserId(resultsShared.getCourtClerk().getUserId())
                        .build()
                )
                .withSharedResultLines(findCompletedResultLineIdsThatAreNew(getCompletedResultLines(resultsShared.getTargets()), resultsShared.getCompletedResultLinesStatus()))
                .withHearingDay(resultsShared.getHearingDay())
                .build();

        final JsonEnvelope jsonEnvelope = envelopeFrom(metadataFrom(event.metadata()).withName("hearing.command.update-days-result-lines-status"), this.objectToJsonObjectConverter.convert(updateDaysResultLinesStatusCommand));
        sender.send(jsonEnvelope);
    }




    private List<ResultLine2> getCompletedResultLines(final List<Target2> targets) {
        return targets.stream()
                .flatMap(target -> target.getResultLines().stream())
                .filter(ResultLine2::getIsComplete)
                .collect(Collectors.toList());
    }

    private List<SharedResultLineId> findCompletedResultLineIdsThatAreNew(final List<ResultLine2> completedResultLines, final Map<UUID, CompletedResultLineStatus> completedResultLinesStatus) {
        return completedResultLines.stream()
                .filter(crl -> !completedResultLinesStatus.containsKey(crl.getResultLineId())
                        || completedResultLinesStatus.get(crl.getResultLineId()).getLastSharedDateTime() == null)
                .map(status -> SharedResultLineId.builder()
                        .withSharedResultLineId(status.getResultLineId())
                        .build()
                )
                .collect(toList());
    }
}
