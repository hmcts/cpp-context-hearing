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

    @Inject
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Inject
    private HearingRepository hearingRepository;

    @Transactional
    @Handles("hearing.offence-verdict-updated")
    public void verdictUpdate(final JsonEnvelope event) {

        final VerdictUpsert verdictUpdated = jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(), VerdictUpsert.class);

        final Hearing hearing = hearingRepository.findBy(verdictUpdated.getHearingId());

        final uk.gov.justice.json.schemas.core.Verdict verdictPojo = verdictUpdated.getVerdict();

        final Offence offence = hearing
                .getProsecutionCases().stream()
                .flatMap(lc -> lc.getDefendants().stream())
                .flatMap(d -> d.getOffences().stream())
                .filter(o -> o.getId().getId().equals(verdictPojo.getOffenceId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Invalid offence id. Offence id is not found on hearing: " + verdictPojo.getOffenceId()));

        final Verdict verdict = createVerdict(verdictPojo);

        offence.setVerdict(verdict);

        hearingRepository.save(hearing);
    }

    private Verdict createVerdict(final uk.gov.justice.json.schemas.core.Verdict verdictPojo) {
        final Verdict verdict = new Verdict();
        verdict.setOriginatingHearingId(verdictPojo.getOriginatingHearingId());
        verdict.setVerdictDate(verdictPojo.getVerdictDate());
        verdict.setVerdictType(createVerdictType(verdictPojo.getVerdictType()));
        verdict.setLesserOrAlternativeOffence(createLesserOrAlternativeOffence(verdictPojo.getLesserOrAlternativeOffence()));
        verdict.setJurors(createJurors(verdictPojo.getJurors()));
        return verdict;
    }

    private Jurors createJurors(final uk.gov.justice.json.schemas.core.Jurors jurorsPojo) {
        final Jurors jurors = new Jurors();
        jurors.setNumberOfJurors(jurorsPojo.getNumberOfJurors());
        jurors.setNumberOfSplitJurors(jurorsPojo.getNumberOfSplitJurors());
        jurors.setUnanimous(jurorsPojo.getUnanimous());
        return jurors;
    }

    private LesserOrAlternativeOffence createLesserOrAlternativeOffence(
            final uk.gov.justice.json.schemas.core.LesserOrAlternativeOffence lesserOrAlternativeOffencePojo) {
        final LesserOrAlternativeOffence lesserOrAlternativeOffence = new LesserOrAlternativeOffence();
        lesserOrAlternativeOffence.setLesserOffenceTitle(lesserOrAlternativeOffencePojo.getOffenceTitle());
        lesserOrAlternativeOffence.setLesserOffenceLegislation(lesserOrAlternativeOffencePojo.getOffenceLegislation());
        lesserOrAlternativeOffence.setLesserOffenceDefinitionId(lesserOrAlternativeOffencePojo.getOffenceDefinitionId());
        lesserOrAlternativeOffence.setLesserOffenceCode(lesserOrAlternativeOffencePojo.getOffenceCode());
        lesserOrAlternativeOffence.setLesserOffenceTitleWelsh(lesserOrAlternativeOffencePojo.getOffenceTitleWelsh());
        lesserOrAlternativeOffence.setLesserOffenceLegislationWelsh(lesserOrAlternativeOffencePojo.getOffenceLegislationWelsh());
        return lesserOrAlternativeOffence;
    }

    private VerdictType createVerdictType(final uk.gov.justice.json.schemas.core.VerdictType verdictTypePojo) {
        final VerdictType verdictType = new VerdictType();
        verdictType.setVerdictTypeId(verdictTypePojo.getVerdictTypeId());
        verdictType.setVerdictCategoryType(verdictTypePojo.getCategoryType());
        verdictType.setVerdictCategory(verdictTypePojo.getCategory());
        verdictType.setDescription(verdictTypePojo.getDescription());
        verdictType.setSequence(verdictTypePojo.getSequence());
        return verdictType;
    }
}
