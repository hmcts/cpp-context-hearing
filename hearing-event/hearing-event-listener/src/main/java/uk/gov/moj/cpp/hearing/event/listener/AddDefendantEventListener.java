package uk.gov.moj.cpp.hearing.event.listener;

import static java.util.Objects.isNull;
import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.event.DefendantAdded;
import uk.gov.moj.cpp.hearing.mapping.DefendantJPAMapper;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.repository.HearingRepository;

import java.util.UUID;

import javax.inject.Inject;
import javax.transaction.Transactional;

@ServiceComponent(EVENT_LISTENER)
public class AddDefendantEventListener {

    @Inject
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Inject
    private HearingRepository hearingRepository;

    @Inject
    private DefendantJPAMapper defendantJPAMapper;


    @Transactional
    @Handles("hearing.defendant-added")
    public void caseDefendantAdded(final JsonEnvelope envelope) {

        final DefendantAdded caseDefendantAdded = jsonObjectToObjectConverter.convert(envelope.payloadAsJsonObject(), DefendantAdded.class);

        final uk.gov.justice.core.courts.Defendant defendantIn = caseDefendantAdded.getDefendant();

        final UUID hearingId = caseDefendantAdded.getHearingId();

        final Hearing hearingEntity = hearingRepository.findBy(hearingId);
        if(isNull(hearingEntity)){
            return;
        }

        hearingEntity.getProsecutionCases().forEach(prosecutionCase -> prosecutionCase.getDefendants().add(defendantJPAMapper.toJPA(hearingEntity, prosecutionCase, defendantIn)));

        hearingRepository.save(hearingEntity);
    }
}