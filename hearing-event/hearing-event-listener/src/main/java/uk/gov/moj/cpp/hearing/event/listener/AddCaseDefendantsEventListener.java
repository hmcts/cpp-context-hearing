package uk.gov.moj.cpp.hearing.event.listener;

import static java.util.Objects.isNull;
import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.event.AddCaseDefendantsForHearing;
import uk.gov.moj.cpp.hearing.mapping.DefendantJPAMapper;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.repository.HearingRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServiceComponent(EVENT_LISTENER)
public class AddCaseDefendantsEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(AddCaseDefendantsEventListener.class.getName());

    @Inject
    private HearingRepository hearingRepository;

    @Inject
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Inject
    private DefendantJPAMapper defendantJPAMapper;

    @Handles("hearing.add-case-defendants-for-hearing")
    public void addCaseDefendantsForHearing(final JsonEnvelope event) {
        final AddCaseDefendantsForHearing addCaseDefendantsForHearing = jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(), AddCaseDefendantsForHearing.class);
        LOGGER.info("Received event {} ", event.toObfuscatedDebugString());
        final UUID hearingId = addCaseDefendantsForHearing.getHearingId();
        final UUID caseId = addCaseDefendantsForHearing.getCaseId();
        final List<Defendant> defendantList = addCaseDefendantsForHearing.getDefendants();
        final Hearing hearingEntity = hearingRepository.findBy(hearingId);
        if (isNull(hearingEntity)) {
            LOGGER.info("Hearing not found for hearing id {} ", hearingId);
        } else {

            hearingEntity.getProsecutionCases().stream().filter(pc -> pc.getId().getId().equals(caseId)).findFirst().ifPresent(persistentCase -> {
                final List<uk.gov.moj.cpp.hearing.persist.entity.ha.Defendant> newDefendants = new ArrayList<>();
                defendantList.forEach(def ->
                        newDefendants.add(defendantJPAMapper.toJPA(hearingEntity, persistentCase, def)));
                persistentCase.getDefendants().addAll(newDefendants);
            });
            hearingRepository.save(hearingEntity);
        }

    }
}
