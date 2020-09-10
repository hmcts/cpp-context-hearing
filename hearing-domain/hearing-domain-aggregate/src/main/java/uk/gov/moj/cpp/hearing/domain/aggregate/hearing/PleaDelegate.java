package uk.gov.moj.cpp.hearing.domain.aggregate.hearing;

import static java.util.Objects.nonNull;
import static uk.gov.justice.core.courts.IndicatedPleaValue.INDICATED_GUILTY;
import static uk.gov.moj.cpp.hearing.domain.aggregate.util.PleaVerdictUtil.isGuiltyVerdict;
import static uk.gov.moj.cpp.hearing.domain.event.ConvictionDateAdded.convictionDateAdded;
import static uk.gov.moj.cpp.hearing.domain.event.ConvictionDateRemoved.convictionDateRemoved;

import uk.gov.justice.core.courts.IndicatedPlea;
import uk.gov.justice.core.courts.Plea;
import uk.gov.justice.core.courts.PleaModel;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.justice.core.courts.Verdict;
import uk.gov.moj.cpp.hearing.domain.event.InheritedPlea;
import uk.gov.moj.cpp.hearing.domain.event.PleaUpsert;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

public class PleaDelegate implements Serializable {

    private final HearingAggregateMomento momento;

    public PleaDelegate(final HearingAggregateMomento momento) {
        this.momento = momento;
    }

    public void handleInheritedPlea(final InheritedPlea inheritedPlea) {
        this.momento.getPleas().computeIfAbsent(inheritedPlea.getPlea().getOffenceId(),
                offenceId -> inheritedPlea.getPlea());
    }

    public void handlePleaUpsert(final PleaUpsert pleaUpsert) {
        final UUID offenceId = pleaUpsert.getPleaModel().getOffenceId();
        if (nonNull(pleaUpsert.getPleaModel().getPlea())) {
            this.momento.getPleas().put(offenceId, pleaUpsert.getPleaModel().getPlea());
        }
        if (nonNull(pleaUpsert.getPleaModel().getIndicatedPlea())) {
            this.momento.getIndicatedPlea().put(offenceId, pleaUpsert.getPleaModel().getIndicatedPlea());
        }
        if (nonNull(pleaUpsert.getPleaModel().getAllocationDecision())) {
            this.momento.getAllocationDecision().put(offenceId, pleaUpsert.getPleaModel().getAllocationDecision());
        }
    }

    public Stream<Object> inheritPlea(final UUID hearingId, final Plea plea) {
        return Stream.of(InheritedPlea.inheritedPlea()
                .setHearingId(hearingId)
                .setPlea(plea));
    }

    public Stream<Object> updatePlea(final UUID hearingId, final PleaModel pleaModel, final Set<String> guiltyPleaTypes) {

        final UUID offenceId = pleaModel.getOffenceId();
        final ProsecutionCase prosecutionCase = this.momento.getHearing().getProsecutionCases().stream()
                .filter(pc -> pc.getDefendants().stream()
                        .flatMap(de -> de.getOffences().stream())
                        .anyMatch(o -> o.getId().equals(offenceId)))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Offence id is not present"));

        setOriginatingHearingId(hearingId, pleaModel);

        final List<Object> events = new ArrayList<>();
        final Plea plea = pleaModel.getPlea();
        events.add(PleaUpsert.pleaUpsert()
                .setHearingId(hearingId)
                .setPleaModel(pleaModel));

        final Verdict existingOffenceVerdict = momento.getVerdicts().get(offenceId);
        final boolean convictionDateAlreadySetForOffence = momento.getConvictionDates().containsKey(offenceId);
        final boolean guiltyVerdictForOffenceAlreadySet = nonNull(existingOffenceVerdict) && isGuiltyVerdict(existingOffenceVerdict.getVerdictType());

        if (nonNull(plea)) {
            if (guiltyPleaTypes.contains(plea.getPleaValue())) {
                // do not update conviction date, if already present for offence
                if (!convictionDateAlreadySetForOffence) {
                    events.add(
                            convictionDateAdded()
                                    .setCaseId(prosecutionCase.getId())
                                    .setHearingId(hearingId)
                                    .setOffenceId(offenceId)
                                    .setConvictionDate(plea.getPleaDate()));
                }
            } else if (!guiltyVerdictForOffenceAlreadySet && convictionDateAlreadySetForOffence) {
                // its 'not guilty' plea and verdict is not already set to guilty
                events.add(
                        convictionDateRemoved()
                                .setCaseId(prosecutionCase.getId())
                                .setHearingId(hearingId)
                                .setOffenceId(offenceId));
            }

        } else if (nonNull(pleaModel.getIndicatedPlea())) {
            // indicated plea logic for updating conviction dates is not changing as it is out of scope for DD-2825
            // and will be covered under a separate CR
            final IndicatedPlea indicatedPlea = pleaModel.getIndicatedPlea();
            events.add(indicatedPlea.getIndicatedPleaValue() == INDICATED_GUILTY ?
                    convictionDateAdded()
                            .setCaseId(prosecutionCase.getId())
                            .setHearingId(hearingId)
                            .setOffenceId(offenceId)
                            .setConvictionDate(indicatedPlea.getIndicatedPleaDate()) :
                    convictionDateRemoved()
                            .setCaseId(prosecutionCase.getId())
                            .setHearingId(hearingId)
                            .setOffenceId(offenceId)
            );
        }

        return events.stream();
    }

    private void setOriginatingHearingId(final UUID hearingId, final PleaModel pleaModel) {
        if (nonNull(pleaModel.getPlea())) {
            pleaModel.getPlea().setOriginatingHearingId(hearingId);
        }
        if (nonNull(pleaModel.getIndicatedPlea())) {
            pleaModel.getIndicatedPlea().setOriginatingHearingId(hearingId);
        }
        if (nonNull(pleaModel.getAllocationDecision())) {
            pleaModel.getAllocationDecision().setOriginatingHearingId(hearingId);
        }
    }

}