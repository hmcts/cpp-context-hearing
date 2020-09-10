package uk.gov.moj.cpp.hearing.domain.aggregate.hearing;

import static java.util.Objects.nonNull;
import static uk.gov.moj.cpp.hearing.domain.aggregate.util.PleaVerdictUtil.isGuiltyVerdict;
import static uk.gov.moj.cpp.hearing.domain.event.ConvictionDateAdded.convictionDateAdded;
import static uk.gov.moj.cpp.hearing.domain.event.ConvictionDateRemoved.convictionDateRemoved;

import uk.gov.justice.core.courts.Plea;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.justice.core.courts.Verdict;
import uk.gov.moj.cpp.hearing.domain.event.InheritedVerdictAdded;
import uk.gov.moj.cpp.hearing.domain.event.VerdictUpsert;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

public class VerdictDelegate implements Serializable {

    private static final long serialVersionUID = 1L;

    private final HearingAggregateMomento momento;

    public VerdictDelegate(final HearingAggregateMomento momento) {
        this.momento = momento;
    }

    public void handleVerdictUpsert(final VerdictUpsert verdictUpsert) {
        final Verdict verdict = verdictUpsert.getVerdict();
        if (nonNull(verdict)) {
            this.momento.getVerdicts().put(verdict.getOffenceId(), verdict);
        }
    }

    public Stream<Object> updateVerdict(final UUID hearingId, final Verdict verdict, final Set<String> guiltyPleaTypes) {
        final List<Object> events = new ArrayList<>();

        final UUID offenceId = verdict.getOffenceId();
        final ProsecutionCase prosecutionCase = this.momento.getHearing().getProsecutionCases().stream()
                .filter(pc -> pc.getDefendants().stream()
                        .flatMap(de -> de.getOffences().stream())
                        .anyMatch(o -> o.getId().equals(offenceId)))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Offence id is not present"));

        verdict.setOriginatingHearingId(hearingId);

        final VerdictUpsert verdictUpsert = VerdictUpsert.verdictUpsert()
                .setHearingId(hearingId)
                .setVerdict(verdict);

        events.add(verdictUpsert);

        final Plea existingOffencePlea = momento.getPleas().get(offenceId);
        final boolean convictionDateAlreadySetForOffence = momento.getConvictionDates().containsKey(offenceId);
        final boolean guiltyPleaForOffenceAlreadySet = nonNull(existingOffencePlea) && guiltyPleaTypes.contains(existingOffencePlea.getPleaValue());

        if (isGuiltyVerdict(verdict.getVerdictType())) {
            if (!convictionDateAlreadySetForOffence) {
                events.add(convictionDateAdded()
                        .setCaseId(prosecutionCase.getId())
                        .setHearingId(hearingId)
                        .setOffenceId(offenceId)
                        .setConvictionDate(verdict.getVerdictDate()));
            }
        } else if (!guiltyPleaForOffenceAlreadySet && convictionDateAlreadySetForOffence) {
            events.add(convictionDateRemoved()
                    .setCaseId(prosecutionCase.getId())
                    .setHearingId(hearingId)
                    .setOffenceId(offenceId));
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