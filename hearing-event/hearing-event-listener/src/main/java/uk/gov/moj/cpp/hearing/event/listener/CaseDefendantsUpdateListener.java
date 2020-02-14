package uk.gov.moj.cpp.hearing.event.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.event.CaseDefendantsUpdatedForHearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.repository.HearingRepository;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.util.Objects.isNull;
import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

@ServiceComponent(EVENT_LISTENER)
public class CaseDefendantsUpdateListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(CaseDefendantsUpdateListener.class.getName());

    @Inject
    private HearingRepository hearingRepository;

    @Inject
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Handles("hearing.case-defendants-updated-for-hearing")
    public void caseDefendantsUpdatedForHearing(final JsonEnvelope event) {
        final CaseDefendantsUpdatedForHearing caseDefendantsUpdatedForHearing = jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(), CaseDefendantsUpdatedForHearing.class);
        LOGGER.info("Received event {} with payload {}", "hearing.case-defendants-updated-for-hearing", event.payloadAsJsonObject());
        final UUID hearingId = caseDefendantsUpdatedForHearing.getHearingId();
        final ProsecutionCase prosecutionCase = caseDefendantsUpdatedForHearing.getProsecutionCase();
        final List<Defendant> defendantList = prosecutionCase.getDefendants();
        final Hearing hearingEntity = hearingRepository.findBy(hearingId);
        if (isNull(hearingEntity)) {
            LOGGER.info("Hearing not found for hearing id {} ", hearingId);
        } else {

            hearingEntity.getProsecutionCases().stream().filter(pc -> pc.getId().getId().equals(prosecutionCase.getId())).findFirst().ifPresent(persistentCase -> {
                persistentCase.setCaseStatus(prosecutionCase.getCaseStatus());
                persistentCase.getDefendants().stream().forEach(defendant -> {
                    final UUID defendantId = defendant.getId().getId();
                    final Optional<Defendant> optionalDefendant = defendantList.stream().filter(def->def.getId().equals(defendantId)).findFirst();
                    if(optionalDefendant.isPresent()) {
                        defendant.setProceedingsConcluded(optionalDefendant.get().getProceedingsConcluded());
                    }
                });
            });
            hearingRepository.save(hearingEntity);
        }

    }
}
