package uk.gov.moj.cpp.hearing.event.listener;

import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.event.VerdictUpsert;
import uk.gov.moj.cpp.hearing.mapping.VerdictJPAMapper;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Offence;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Verdict;
import uk.gov.moj.cpp.hearing.repository.HearingRepository;

import javax.inject.Inject;
import javax.transaction.Transactional;

@ServiceComponent(EVENT_LISTENER)
public class VerdictUpdateEventListener {

    @Inject
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Inject
    private HearingRepository hearingRepository;

    @Inject
    private VerdictJPAMapper verdictJPAMapper;

    @Transactional
    @Handles("hearing.hearing-offence-verdict-updated")
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

        final Verdict verdict = verdictJPAMapper.toJPA(verdictPojo);

        offence.setVerdict(verdict);

        hearingRepository.save(hearing);
    }
}
