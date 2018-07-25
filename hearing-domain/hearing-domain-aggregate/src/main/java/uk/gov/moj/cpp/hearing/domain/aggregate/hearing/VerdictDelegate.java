package uk.gov.moj.cpp.hearing.domain.aggregate.hearing;

import static java.util.Optional.ofNullable;

import uk.gov.moj.cpp.hearing.command.verdict.Verdict;
import uk.gov.moj.cpp.hearing.domain.event.ConvictionDateAdded;
import uk.gov.moj.cpp.hearing.domain.event.ConvictionDateRemoved;
import uk.gov.moj.cpp.hearing.domain.event.VerdictUpsert;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

public class VerdictDelegate  implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final String GUILTY = "GUILTY";

    private final HearingAggregateMomento momento;

    public VerdictDelegate(final HearingAggregateMomento momento){
        this.momento = momento;
    }

    public void handleVerdictUpsert(VerdictUpsert verdictUpsert){
        this.momento.getVerdicts().put(verdictUpsert.getOffenceId(), verdictUpsert);
    }

    public Stream<Object> updateVerdict(final UUID hearingId, final UUID caseId, final UUID offenceId, final Verdict verdict) {

        final List<Object> events = new ArrayList<>();

        events.add(VerdictUpsert.builder()
                .withCaseId(caseId)
                .withHearingId(hearingId)
                .withOffenceId(offenceId)
                .withVerdictId(verdict.getId())
                .withVerdictValueId(verdict.getValue().getId())
                .withVerdictTypeId(verdict.getValue().getVerdictTypeId())
                .withCategory(verdict.getValue().getCategory())
                .withCategoryType(verdict.getValue().getCategoryType())
                .withLesserOffence(Optional.ofNullable(verdict.getValue().getLesserOffence()).orElse(null))
                .withCode(verdict.getValue().getCode())
                .withDescription(verdict.getValue().getDescription())
                .withNumberOfJurors(Optional.ofNullable(verdict.getNumberOfJurors()).orElse(null))
                .withNumberOfSplitJurors(Optional.ofNullable(verdict.getNumberOfSplitJurors()).orElse(null))
                .withUnanimous(Optional.ofNullable(verdict.getUnanimous()).orElse(null))
                .withVerdictDate(verdict.getVerdictDate())
                .build()
        );

        final String categoryType = ofNullable(verdict.getValue().getCategoryType()).orElse("");

        final LocalDate offenceConvictionDate = this.momento.getHearing().getDefendants().stream()
                .flatMap(d -> d.getOffences().stream())
                .filter(o -> offenceId.equals(o.getId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Offence id is not present"))
                .getConvictionDate();

        if (categoryType.startsWith(GUILTY)) {
            if (offenceConvictionDate == null) {
                events.add(new ConvictionDateAdded(caseId, hearingId, offenceId, verdict.getVerdictDate()));
            }
        } else {
            if (offenceConvictionDate != null) {
                events.add(new ConvictionDateRemoved(caseId, hearingId, offenceId));
            }
        }
        return events.stream();
    }
}
