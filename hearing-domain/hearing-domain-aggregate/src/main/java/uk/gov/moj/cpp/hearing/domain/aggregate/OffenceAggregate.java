package uk.gov.moj.cpp.hearing.domain.aggregate;

import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.match;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.otherwiseDoNothing;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.when;

import java.util.UUID;
import java.util.stream.Stream;

import uk.gov.justice.domain.aggregate.Aggregate;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingOffenceCommand;
import uk.gov.moj.cpp.hearing.command.plea.Plea;
import uk.gov.moj.cpp.hearing.command.verdict.Verdict;
import uk.gov.moj.cpp.hearing.domain.event.InitiateHearingOffenceEnriched;
import uk.gov.moj.cpp.hearing.domain.event.OffencePleaUpdated;
import uk.gov.moj.cpp.hearing.domain.event.OffenceVerdictUpdated;

public class OffenceAggregate implements Aggregate {

    private static final long serialVersionUID = 1L;
    
    private OffencePleaUpdated offencePleaUpdated;

    @Override
    public Object apply(Object event) {
        return match(event).with(
                when(OffencePleaUpdated.class).apply((offencePleaUpdated) -> this.offencePleaUpdated = offencePleaUpdated),
                otherwiseDoNothing()
        );
    }

    public Stream<Object> initiateHearingOffence(InitiateHearingOffenceCommand initiateHearingOffenceCommand) {

        if (this.offencePleaUpdated != null) {
            return apply(Stream.of(new InitiateHearingOffenceEnriched(
                    initiateHearingOffenceCommand.getOffenceId(),
                    initiateHearingOffenceCommand.getCaseId(),
                    initiateHearingOffenceCommand.getDefendantId(),
                    initiateHearingOffenceCommand.getHearingId(),
                    offencePleaUpdated.getOriginHearingId(),
                    offencePleaUpdated.getPleaDate(),
                    offencePleaUpdated.getValue()
            )));
        }
        return apply(Stream.empty());
    }

    public Stream<Object> updatePlea(final UUID originHearingId, final UUID offenceId, final Plea plea) {
        return apply(OffencePleaUpdated.builder()
                    .withOriginHearingId(originHearingId)
                    .withOffenceId(offenceId)
                    .withPleaDate(plea.getPleaDate())
                    .withValue(plea.getValue())
                    .buildStream());
    }

    public Stream<Object> updateVerdict(UUID hearingId, UUID caseId, UUID offenceId, Verdict verdict) {

        return apply(Stream.of(
                new OffenceVerdictUpdated(
                        caseId, //TODO - offenceId is unique within case, so do we need this?
                        hearingId,
                        offenceId,
                        verdict.getId(), //TODO - do we need verdictId
                        verdict.getValue().getId(),
                        verdict.getValue().getCategory(),
                        verdict.getValue().getCode(),
                        verdict.getValue().getDescription()
                )
        ));
    }

    public OffencePleaUpdated getPlea() {
        return offencePleaUpdated;
    }
}