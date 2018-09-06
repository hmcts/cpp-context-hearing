package uk.gov.moj.cpp.hearing.domain.aggregate.hearing;

import uk.gov.justice.json.schemas.core.Offence;
import uk.gov.justice.json.schemas.core.ProsecutionCase;
import uk.gov.moj.cpp.hearing.command.verdict.Verdict;
import uk.gov.moj.cpp.hearing.domain.event.ConvictionDateAdded;
import uk.gov.moj.cpp.hearing.domain.event.ConvictionDateRemoved;
import uk.gov.moj.cpp.hearing.domain.event.VerdictUpsert;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

public class VerdictDelegate implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final String GUILTY = "GUILTY";

    private final HearingAggregateMomento momento;

    public VerdictDelegate(final HearingAggregateMomento momento) {
        this.momento = momento;
    }

    public void handleVerdictUpsert(VerdictUpsert verdictUpsert) {
        this.momento.getVerdicts().put(verdictUpsert.getOffenceId(), verdictUpsert);
    }

    public Stream<Object> updateVerdict(final UUID hearingId, final Verdict verdict) {

        final List<Object> events = new ArrayList<>();

        final ProsecutionCase prosecutionCase = this.momento.getHearing().getProsecutionCases().stream()
                .filter(pc -> pc.getDefendants().stream()
                        .flatMap(de -> de.getOffences().stream())
                        .anyMatch(o -> o.getId().equals(verdict.getOffenceId()))
                )
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Offence id is not present"));

        final Offence offence = this.momento.getHearing().getProsecutionCases().stream()
                .flatMap(pc -> pc.getDefendants().stream())
                .flatMap(de -> de.getOffences().stream())
                .filter(o -> o.getId().equals(verdict.getOffenceId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Offence id is not present"));

        final VerdictUpsert verdictUpsert = VerdictUpsert.verdictUpsert()
                .setCaseId(prosecutionCase.getId())
                .setHearingId(hearingId)
                .setOffenceId(offence.getId())
                .setVerdictTypeId(verdict.getVerdictType().getId())
                .setCategory(verdict.getVerdictType().getCategory())
                .setCategoryType(verdict.getVerdictType().getCategoryType())
                .setVerdictDate(verdict.getVerdictDate());

        if (verdict.getLesserOffence() != null) {
            verdictUpsert
                    .setOffenceDefinitionId(verdict.getLesserOffence().getOffenceDefinitionId())
                    .setTitle(verdict.getLesserOffence().getTitle())
                    .setLegislation(verdict.getLesserOffence().getLegislation())
                    .setOffenceCode(verdict.getLesserOffence().getOffenceCode());
        }

        if (verdict.getJurors() != null) {
            verdictUpsert
                    .setNumberOfJurors(verdict.getJurors().getNumberOfJurors())
                    .setNumberOfSplitJurors(verdict.getJurors().getNumberOfSplitJurors())
                    .setUnanimous(verdict.getJurors().getUnanimous());
        }

        events.add(verdictUpsert);

        if (verdict.getVerdictType().getCategoryType().startsWith(GUILTY) && offence.getConvictionDate() == null) {
            events.add(ConvictionDateAdded.convictionDateAdded()
                    .setCaseId(prosecutionCase.getId())
                    .setHearingId(hearingId)
                    .setOffenceId(offence.getId())
                    .setConvictionDate(verdict.getVerdictDate())
            );
        }

        if (!verdict.getVerdictType().getCategoryType().startsWith(GUILTY) && offence.getConvictionDate() != null) {
            events.add(ConvictionDateRemoved.convictionDateRemoved()
                    .setCaseId(prosecutionCase.getId())
                    .setHearingId(hearingId)
                    .setOffenceId(offence.getId())
            );
        }

        return events.stream();
    }
}
