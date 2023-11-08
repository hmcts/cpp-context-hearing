package uk.gov.moj.cpp.hearing.domain.aggregate.hearing;

import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static uk.gov.justice.core.courts.IndicatedPleaValue.INDICATED_GUILTY;
import static uk.gov.moj.cpp.hearing.domain.aggregate.util.PleaVerdictUtil.isGuiltyVerdict;
import static uk.gov.moj.cpp.hearing.domain.event.ConvictionDateAdded.convictionDateAdded;
import static uk.gov.moj.cpp.hearing.domain.event.ConvictionDateRemoved.convictionDateRemoved;

import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.core.courts.CourtOrderOffence;
import uk.gov.justice.core.courts.IndicatedPlea;
import uk.gov.justice.core.courts.Offence;
import uk.gov.justice.core.courts.Plea;
import uk.gov.justice.core.courts.PleaModel;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.justice.core.courts.Verdict;
import uk.gov.moj.cpp.hearing.domain.event.InheritedPlea;
import uk.gov.moj.cpp.hearing.domain.event.PleaUpsert;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

@SuppressWarnings("squid:S00112")
public class PleaDelegate implements Serializable {

    private final HearingAggregateMomento momento;

    public PleaDelegate(final HearingAggregateMomento momento) {
        this.momento = momento;
    }

    public void handleInheritedPlea(final InheritedPlea inheritedPlea) {
        this.momento.getPleas().put(inheritedPlea.getPlea().getOffenceId(), inheritedPlea.getPlea());
    }

    public void handlePleaUpsert(final PleaUpsert pleaUpsert) {
        final UUID offenceId = pleaUpsert.getPleaModel().getOffenceId();
        if (nonNull(pleaUpsert.getPleaModel().getPlea())) {
            this.momento.getPleas().put(offenceId, pleaUpsert.getPleaModel().getPlea());
        }else{
            this.momento.getPleas().remove(offenceId);
        }
        if (nonNull(pleaUpsert.getPleaModel().getIndicatedPlea())) {
            this.momento.getIndicatedPlea().put(offenceId, pleaUpsert.getPleaModel().getIndicatedPlea());
        }else{
            this.momento.getIndicatedPlea().remove(offenceId);
        }

        if (nonNull(pleaUpsert.getPleaModel().getAllocationDecision())) {
            this.momento.getAllocationDecision().put(offenceId, pleaUpsert.getPleaModel().getAllocationDecision());
        }

        ofNullable(this.momento.getHearing().getProsecutionCases()).map(Collection::stream).orElseGet(Stream::empty)
                .flatMap(ps -> ps.getDefendants().stream())
                .flatMap(d -> d.getOffences().stream())
                .filter(o -> o.getId().equals(offenceId))
                .forEach(this::setOffence);


        ofNullable(this.momento.getHearing().getCourtApplications()).map(Collection::stream).orElseGet(Stream::empty)
                .flatMap(ca -> ofNullable(ca.getCourtApplicationCases()).map(Collection::stream).orElseGet(Stream::empty))
                .flatMap(c -> ofNullable(c.getOffences()).map(Collection::stream).orElseGet(Stream::empty))
                .filter(o -> o.getId().equals(offenceId))
                .forEach(this::setOffence);


        ofNullable(this.momento.getHearing().getCourtApplications()).map(Collection::stream).orElseGet(Stream::empty)
                .map(CourtApplication::getCourtOrder)
                .filter(Objects::nonNull)
                .flatMap(o -> o.getCourtOrderOffences().stream())
                .map(CourtOrderOffence::getOffence)
                .filter(o -> o.getId().equals(offenceId))
                .forEach(this::setOffence);
    }

    public Stream<Object> inheritPlea(final UUID hearingId, final Plea plea) {
        return Stream.of(InheritedPlea.inheritedPlea()
                .setHearingId(hearingId)
                .setPlea(plea));
    }

    public Stream<Object> updatePlea(final UUID hearingId, final PleaModel pleaModel, final Set<String> guiltyPleaTypes) {

        final UUID offenceId = pleaModel.getOffenceId();
        final UUID prosecutionCaseId;
        final UUID courtApplicationId;
        if (pleaModel.getApplicationId() == null) {
            prosecutionCaseId = ofNullable(this.momento.getHearing().getProsecutionCases()).map(Collection::stream).orElseGet(Stream::empty)
                    .filter(pc -> pc.getDefendants().stream()
                            .flatMap(de -> de.getOffences().stream())
                            .anyMatch(o -> o.getId().equals(offenceId)))
                    .findFirst()
                    .map(ProsecutionCase::getId)
                    .orElse(null);

            courtApplicationId = findCourtApplicationByOffence(offenceId);

            throwIfOffenceAbsent(prosecutionCaseId, courtApplicationId);
        } else {
            courtApplicationId = pleaModel.getApplicationId();
            prosecutionCaseId = null;
        }
        setOriginatingHearingId(hearingId, pleaModel);

        final List<Object> events = new ArrayList<>();
        final Plea plea = pleaModel.getPlea();
        events.add(PleaUpsert.pleaUpsert()
                .setHearingId(hearingId)
                .setPleaModel(pleaModel));

        if (nonNull(plea)) {
            addConvictionDateEventForPlea(hearingId, guiltyPleaTypes, offenceId, prosecutionCaseId, courtApplicationId, events, plea);
        } else if (nonNull(pleaModel.getIndicatedPlea()) ) {
            // indicated plea logic for updating conviction dates is not changing as it is out of scope for DD-2825
            // and will be covered under a separate CR
            addConvictionDateEventForIndicatedPlea(hearingId, pleaModel, offenceId, prosecutionCaseId, courtApplicationId, events);
        } else if (canRemoveConvictionDate(offenceId, courtApplicationId)){
            events.add(
                    convictionDateRemoved()
                            .setCaseId(prosecutionCaseId)
                            .setHearingId(hearingId)
                            .setOffenceId(offenceId)
                            .setCourtApplicationId(courtApplicationId));
        }

            return events.stream();
    }

    private void addConvictionDateEventForIndicatedPlea(final UUID hearingId, final PleaModel pleaModel, final UUID offenceId, final UUID prosecutionCaseId, final UUID courtApplicationId, final List<Object> events) {
        final IndicatedPlea indicatedPlea = pleaModel.getIndicatedPlea();
        if(indicatedPlea.getIndicatedPleaValue() == INDICATED_GUILTY){
            events.add(convictionDateAdded()
                            .setCaseId(prosecutionCaseId)
                            .setHearingId(hearingId)
                            .setOffenceId(offenceId)
                            .setConvictionDate(indicatedPlea.getIndicatedPleaDate())
                            .setCourtApplicationId(courtApplicationId));
        }else if(canRemoveConvictionDate(offenceId, courtApplicationId)) {
            events.add(convictionDateRemoved()
                            .setCaseId(prosecutionCaseId)
                            .setHearingId(hearingId)
                            .setOffenceId(offenceId)
                            .setCourtApplicationId(courtApplicationId)
            );
        }
    }

    private void addConvictionDateEventForPlea(final UUID hearingId, final Set<String> guiltyPleaTypes, final UUID offenceId, final UUID prosecutionCaseId, final UUID courtApplicationId, final List<Object> events, final Plea plea) {
        if (guiltyPleaTypes.contains(plea.getPleaValue())) {
            events.add(
                    convictionDateAdded()
                            .setCaseId(prosecutionCaseId)
                            .setHearingId(hearingId)
                            .setOffenceId(offenceId)
                            .setConvictionDate(plea.getPleaDate())
                            .setCourtApplicationId(courtApplicationId));
        } else if (canRemoveConvictionDate(offenceId, courtApplicationId)) {
            // its 'not guilty' plea and verdict is not already set to guilty
            events.add(
                    convictionDateRemoved()
                            .setCaseId(prosecutionCaseId)
                            .setHearingId(hearingId)
                            .setOffenceId(offenceId)
                            .setCourtApplicationId(courtApplicationId));
        }
    }

    private boolean canRemoveConvictionDate(final UUID offenceId,  final UUID courtApplicationId){
        final Verdict existingOffenceVerdict = momento.getVerdicts().get(Optional.ofNullable(offenceId).orElse(courtApplicationId));
        final boolean convictionDateAlreadySetForOffence = momento.getConvictionDates().containsKey(Optional.ofNullable(offenceId).orElse(courtApplicationId));
        final boolean guiltyVerdictForOffenceAlreadySet = nonNull(existingOffenceVerdict) && isGuiltyVerdict(existingOffenceVerdict.getVerdictType());
        return !guiltyVerdictForOffenceAlreadySet && convictionDateAlreadySetForOffence;
    }

    private UUID findCourtApplicationByOffence(final UUID offenceId) {
        final UUID courtApplicationId;
        courtApplicationId = ofNullable(this.momento.getHearing().getCourtApplications()).map(Collection::stream).orElseGet(Stream::empty)
                .filter(ca -> ofNullable(ca.getCourtApplicationCases()).map(Collection::stream).orElseGet(Stream::empty)
                        .flatMap(cac -> ofNullable(cac.getOffences()).map(Collection::stream).orElseGet(Stream::empty))
                        .anyMatch(o -> o.getId().equals(offenceId)) ||
                        (ca.getCourtOrder() != null &&
                                ca.getCourtOrder().getCourtOrderOffences().stream().anyMatch(co -> co.getOffence().getId().equals(offenceId)))
                )
                .findFirst()
                .map(CourtApplication::getId)
                .orElse(null);
        return courtApplicationId;
    }

    public void throwIfOffenceAbsent(final UUID prosecutionCaseId, final UUID courtApplicationId) {
        if (prosecutionCaseId == null && courtApplicationId == null) {
            throw new RuntimeException("Offence id is not present");
        }
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

    private void setOffence(final Offence offence) {
        offence.setPlea(this.momento.getPleas().get(offence.getId()));
        offence.setIndicatedPlea(this.momento.getIndicatedPlea().get(offence.getId()));
        offence.setAllocationDecision(this.momento.getAllocationDecision().get(offence.getId()));
    }
}