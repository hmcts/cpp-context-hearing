package uk.gov.moj.cpp.hearing.domain.aggregate.hearing;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import uk.gov.justice.core.courts.Offence;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.justice.core.courts.Verdict;
import uk.gov.moj.cpp.hearing.domain.event.ConvictionDateAdded;
import uk.gov.moj.cpp.hearing.domain.event.ConvictionDateRemoved;
import uk.gov.moj.cpp.hearing.domain.event.InheritedVerdictAdded;
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

    public void handleVerdictUpsert(final VerdictUpsert verdictUpsert) {
        if (nonNull(verdictUpsert.getVerdict())) {
            this.momento.getVerdicts().put(verdictUpsert.getVerdict().getOffenceId(), verdictUpsert.getVerdict());
        }
    }

    public Stream<Object> updateVerdict(final UUID hearingId, final Verdict verdict) {

        final List<Object> events = new ArrayList<>();

        final ProsecutionCase prosecutionCase = this.momento.getHearing().getProsecutionCases().stream()
                .filter(pc -> pc.getDefendants().stream()
                        .flatMap(de -> de.getOffences().stream())
                        .anyMatch(o -> o.getId().equals(verdict.getOffenceId())))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Offence id is not present"));

        verdict.setOriginatingHearingId(hearingId);

        final Offence offence = this.momento.getHearing().getProsecutionCases().stream()
                .flatMap(pc -> pc.getDefendants().stream())
                .flatMap(de -> de.getOffences().stream())
                .filter(o -> o.getId().equals(verdict.getOffenceId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Offence id is not present"));

        final VerdictUpsert verdictUpsert = VerdictUpsert.verdictUpsert()
                .setHearingId(hearingId)
                .setVerdict(verdict);

        events.add(verdictUpsert);

        if (verdict.getVerdictType().getCategoryType().startsWith(GUILTY) && isNull(offence.getConvictionDate())) {
            events.add(ConvictionDateAdded.convictionDateAdded()
                    .setCaseId(prosecutionCase.getId())
                    .setHearingId(hearingId)
                    .setOffenceId(offence.getId())
                    .setConvictionDate(verdict.getVerdictDate()));
        }

        if (!verdict.getVerdictType().getCategoryType().startsWith(GUILTY) && nonNull(offence.getConvictionDate())) {
            events.add(ConvictionDateRemoved.convictionDateRemoved()
                    .setCaseId(prosecutionCase.getId())
                    .setHearingId(hearingId)
                    .setOffenceId(offence.getId()));
        }

        return events.stream();
    }

    public void handleInheritedVerdict(final InheritedVerdictAdded inheritedVerdict) {
        this.momento.getVerdicts().computeIfAbsent(inheritedVerdict.getVerdict().getOffenceId(),
                offenceId -> inheritedVerdict.getVerdict());
    }

    public Stream<Object> inheritVerdict(UUID hearingId, Verdict verdict) {
        return Stream.of(new InheritedVerdictAdded(hearingId, verdict));
    }
}