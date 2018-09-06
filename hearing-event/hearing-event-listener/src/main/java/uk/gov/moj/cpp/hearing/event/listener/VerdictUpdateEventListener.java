package uk.gov.moj.cpp.hearing.event.listener;

import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.event.VerdictUpsert;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Jurors;
import uk.gov.moj.cpp.hearing.persist.entity.ha.LesserOrAlternativeOffence;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Offence;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Verdict;
import uk.gov.moj.cpp.hearing.persist.entity.ha.VerdictType;
import uk.gov.moj.cpp.hearing.repository.HearingRepository;

import javax.inject.Inject;
import javax.transaction.Transactional;

@ServiceComponent(EVENT_LISTENER)
public class VerdictUpdateEventListener {

    private final HearingRepository hearingRepository;
    private final JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Inject
    public VerdictUpdateEventListener(final HearingRepository hearingRepository,
                                      final JsonObjectToObjectConverter jsonObjectToObjectConverter) {
        this.hearingRepository = hearingRepository;
        this.jsonObjectToObjectConverter = jsonObjectToObjectConverter;
    }

    @Transactional
    @Handles("hearing.offence-verdict-updated")
    public void verdictUpdate(final JsonEnvelope event) {
        final VerdictUpsert verdictUpdated = jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(), VerdictUpsert.class);

        final Hearing hearing = hearingRepository.findBy(verdictUpdated.getHearingId());

        final Offence offence = hearing
                .getProsecutionCases().stream()
                .flatMap(lc -> lc.getDefendants().stream())
                .flatMap(d -> d.getOffences().stream())
                .filter(o -> o.getId().getId().equals(verdictUpdated.getOffenceId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Invalid offence id.  Offence id is not found on hearing: " + verdictUpdated.getOffenceId()));


        final VerdictType verdictType = new VerdictType();
        verdictType.setVerdictTypeId(verdictUpdated.getVerdictTypeId());
        verdictType.setVerdictCategoryType(verdictUpdated.getCategoryType());
        verdictType.setVerdictCategory(verdictUpdated.getCategory());

        final LesserOrAlternativeOffence lesserOrAlternativeOffence = new LesserOrAlternativeOffence();

        lesserOrAlternativeOffence.setLesserOffenceTitle(verdictUpdated.getTitle());
        lesserOrAlternativeOffence.setLesserOffenceLegislation(verdictUpdated.getLegislation());
        lesserOrAlternativeOffence.setLesserOffenceDefinitionId(verdictUpdated.getOffenceDefinitionId());
        lesserOrAlternativeOffence.setLesserOffenceCode(verdictUpdated.getOffenceCode());

        final Jurors jurors = new Jurors();
        jurors.setNumberOfJurors(verdictUpdated.getNumberOfJurors());
        jurors.setNumberOfSplitJurors(verdictUpdated.getNumberOfSplitJurors());
        jurors.setUnanimous(verdictUpdated.getUnanimous());

        final Verdict verdict = new Verdict();
        verdict.setVerdictDate(verdictUpdated.getVerdictDate());
        verdict.setVerdictType(verdictType);
        verdict.setLesserOrAlternativeOffence(lesserOrAlternativeOffence);
        verdict.setJurors(jurors);

        offence.setVerdict(verdict);

        hearingRepository.save(hearing);
    }
}
