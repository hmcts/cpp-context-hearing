package uk.gov.moj.cpp.hearing.domain.aggregate.hearing;

import static java.util.Collections.emptyList;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static uk.gov.moj.cpp.hearing.domain.aggregate.util.PleaVerdictUtil.isGuiltyVerdict;
import static uk.gov.moj.cpp.hearing.domain.event.ConvictionDateAdded.convictionDateAdded;
import static uk.gov.moj.cpp.hearing.domain.event.ConvictionDateRemoved.convictionDateRemoved;

import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.core.courts.CourtOrderOffence;
import uk.gov.justice.core.courts.Offence;
import uk.gov.justice.core.courts.Plea;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.justice.core.courts.Verdict;
import uk.gov.moj.cpp.hearing.domain.event.InheritedVerdictAdded;
import uk.gov.moj.cpp.hearing.domain.event.VerdictUpsert;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

@SuppressWarnings("squid:S00112")
public class VerdictDelegate implements Serializable {

    private static final long serialVersionUID = 1L;

    private final HearingAggregateMomento momento;

    public VerdictDelegate(final HearingAggregateMomento momento) {
        this.momento = momento;
    }

    public void handleVerdictUpsert(final VerdictUpsert verdictUpsert) {
        final Verdict verdict = verdictUpsert.getVerdict();
        if (nonNull(verdict)) {
            ofNullable(verdict.getOffenceId()).ifPresent(offId -> {
                this.momento.getVerdicts().put(offId, verdict);
                ofNullable(this.momento.getHearing().getCourtApplications()).map(Collection::stream).orElseGet(Stream::empty)
                        .flatMap(ca -> ofNullable(ca.getCourtApplicationCases()).map(Collection::stream).orElseGet(Stream::empty))
                        .flatMap(c -> ofNullable(c.getOffences()).map(Collection::stream).orElseGet(Stream::empty))
                        .filter(o -> o.getId().equals(offId))
                        .forEach(this::setVerdictOnTheOffence);

                ofNullable(this.momento.getHearing().getCourtApplications()).map(Collection::stream).orElseGet(Stream::empty)
                        .map(CourtApplication::getCourtOrder)
                        .filter(Objects::nonNull)
                        .flatMap(o -> o.getCourtOrderOffences().stream())
                        .map(CourtOrderOffence::getOffence)
                        .filter(o -> o.getId().equals(offId))
                        .forEach(this::setVerdictOnTheOffence);
            });
            ofNullable(verdict.getApplicationId()).ifPresent(appId -> {
                this.momento.getVerdicts().put(appId, verdict);
                ofNullable(this.momento.getHearing().getCourtApplications()).map(Collection::stream).orElseGet(Stream::empty)
                        .filter(app -> app.getId().equals(verdict.getApplicationId()))
                        .forEach(app -> app.setVerdict(verdict));
            });
        }
    }


    private void setVerdictOnTheOffence(final Offence offence) {
        offence.setVerdict(this.momento.getVerdicts().get(offence.getId()));
    }

    public Stream<Object> updateVerdict(final UUID hearingId, final Verdict verdict, final Set<String> guiltyPleaTypes) {
        final List<Object> events = new ArrayList<>();
        final UUID prosecutionCaseId;
        final UUID courtApplicationId;
        final UUID offenceId = verdict.getOffenceId();
        if(offenceId != null) {
            prosecutionCaseId = ofNullable(this.momento.getHearing().getProsecutionCases()).orElse(emptyList()).stream()
                    .filter(pc -> pc.getDefendants().stream()
                            .flatMap(de -> de.getOffences().stream())
                            .anyMatch(o -> o.getId().equals(offenceId)))
                    .findFirst()
                    .map(ProsecutionCase::getId)
                    .orElse(null);

            courtApplicationId = ofNullable(this.momento.getHearing().getCourtApplications()).orElse(emptyList()).stream()
                    .filter(ca -> ofNullable(ca.getCourtApplicationCases()).orElse(emptyList()).stream()
                            .flatMap(cac -> ofNullable(cac.getOffences()).map(Collection::stream).orElseGet(Stream::empty))
                            .anyMatch(o -> o.getId().equals(offenceId)) ||
                            (ca.getCourtOrder() != null &&
                                    ca.getCourtOrder().getCourtOrderOffences().stream().anyMatch(co -> co.getOffence().getId().equals(offenceId)))
                    )
                    .findFirst()
                    .map(CourtApplication::getId)
                    .orElse(null);
            if (prosecutionCaseId == null && courtApplicationId == null) {
                throw new RuntimeException("Offence id is not present");
            }
        }else{
            prosecutionCaseId = null;
            courtApplicationId = verdict.getApplicationId();
        }
        verdict.setOriginatingHearingId(hearingId);

        final VerdictUpsert verdictUpsert = VerdictUpsert.verdictUpsert()
                .setHearingId(hearingId)
                .setVerdict(verdict);

        events.add(verdictUpsert);

        final Plea existingOffencePlea = momento.getPleas().get(ofNullable(offenceId).orElse(courtApplicationId));
        final boolean convictionDateAlreadySetForOffence = momento.getConvictionDates().containsKey(ofNullable(offenceId).orElse(courtApplicationId));
        final boolean guiltyPleaForOffenceAlreadySet = nonNull(existingOffencePlea) && guiltyPleaTypes.contains(existingOffencePlea.getPleaValue());

        if (isGuiltyVerdict(verdict.getVerdictType())) {
            if (!convictionDateAlreadySetForOffence) {
                events.add(convictionDateAdded()
                        .setCaseId(prosecutionCaseId)
                        .setHearingId(hearingId)
                        .setOffenceId(offenceId)
                        .setConvictionDate(verdict.getVerdictDate())
                        .setCourtApplicationId(courtApplicationId));
            }
        } else if (!guiltyPleaForOffenceAlreadySet && convictionDateAlreadySetForOffence) {
            events.add(convictionDateRemoved()
                    .setCaseId(prosecutionCaseId)
                    .setHearingId(hearingId)
                    .setOffenceId(offenceId)
                    .setCourtApplicationId(courtApplicationId));
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
