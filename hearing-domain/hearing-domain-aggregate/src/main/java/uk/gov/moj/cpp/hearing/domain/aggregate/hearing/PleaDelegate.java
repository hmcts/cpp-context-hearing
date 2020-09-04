package uk.gov.moj.cpp.hearing.domain.aggregate.hearing;

import static java.util.Objects.nonNull;
import static uk.gov.justice.core.courts.IndicatedPleaValue.INDICATED_GUILTY;
import static uk.gov.justice.core.courts.PleaValue.ADMITS;
import static uk.gov.justice.core.courts.PleaValue.AUTREFOIS_CONVICT;
import static uk.gov.justice.core.courts.PleaValue.CHANGE_TO_GUILTY_AFTER_SWORN_IN;
import static uk.gov.justice.core.courts.PleaValue.CHANGE_TO_GUILTY_MAGISTRATES_COURT;
import static uk.gov.justice.core.courts.PleaValue.CHANGE_TO_GUILTY_NO_SWORN_IN;
import static uk.gov.justice.core.courts.PleaValue.CONSENTS;
import static uk.gov.justice.core.courts.PleaValue.GUILTY;
import static uk.gov.justice.core.courts.PleaValue.GUILTY_TO_AN_ALTERNATIVE_OFFENCE_NOT_CHARGED_NAMELY;
import static uk.gov.justice.core.courts.PleaValue.GUILTY_TO_A_LESSER_OFFENCE_NAMELY;
import static uk.gov.justice.core.courts.PleaValue.MCA_GUILTY;
import static uk.gov.moj.cpp.hearing.domain.event.ConvictionDateAdded.convictionDateAdded;
import static uk.gov.moj.cpp.hearing.domain.event.ConvictionDateRemoved.convictionDateRemoved;

import uk.gov.justice.core.courts.IndicatedPlea;
import uk.gov.justice.core.courts.Plea;
import uk.gov.justice.core.courts.PleaModel;
import uk.gov.justice.core.courts.PleaValue;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.moj.cpp.hearing.domain.event.InheritedPlea;
import uk.gov.moj.cpp.hearing.domain.event.PleaUpsert;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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

    public Stream<Object> updatePlea(final UUID hearingId, final PleaModel pleaModel) {

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
        final IndicatedPlea indicatedPlea = pleaModel.getIndicatedPlea();

        events.add(PleaUpsert.pleaUpsert()
                .setHearingId(hearingId)
                .setPleaModel(pleaModel));


        if (nonNull(plea)) {
            events.add(isPleaTypeGuilty(plea.getPleaValue()) ?
                    convictionDateAdded()
                            .setCaseId(prosecutionCase.getId())
                            .setHearingId(hearingId)
                            .setOffenceId(offenceId)
                            .setConvictionDate(plea.getPleaDate()) :
                    convictionDateRemoved()
                            .setCaseId(prosecutionCase.getId())
                            .setHearingId(hearingId)
                            .setOffenceId(offenceId)
            );
        } else if (nonNull(indicatedPlea)) {
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

    private boolean isPleaTypeGuilty(PleaValue pleaValue) {
        return Arrays.asList(GUILTY,
                MCA_GUILTY,
                AUTREFOIS_CONVICT,
                CONSENTS,
                CHANGE_TO_GUILTY_AFTER_SWORN_IN,
                CHANGE_TO_GUILTY_NO_SWORN_IN,
                CHANGE_TO_GUILTY_MAGISTRATES_COURT,
                GUILTY_TO_A_LESSER_OFFENCE_NAMELY,
                GUILTY_TO_AN_ALTERNATIVE_OFFENCE_NOT_CHARGED_NAMELY,
                ADMITS).contains(pleaValue);
    }
}