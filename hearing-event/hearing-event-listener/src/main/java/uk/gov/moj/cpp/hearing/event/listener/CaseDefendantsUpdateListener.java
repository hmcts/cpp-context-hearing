package uk.gov.moj.cpp.hearing.event.listener;

import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.core.courts.CourtApplicationParty;
import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.event.ApplicationDefendantsUpdatedForHearing;
import uk.gov.moj.cpp.hearing.domain.event.CaseDefendantsUpdatedForHearing;
import uk.gov.moj.cpp.hearing.mapping.CourtApplicationsSerializer;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.repository.HearingRepository;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

@ServiceComponent(EVENT_LISTENER)
public class CaseDefendantsUpdateListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(CaseDefendantsUpdateListener.class.getName());

    @Inject
    private HearingRepository hearingRepository;

    @Inject
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Inject
    private CourtApplicationsSerializer courtApplicationsSerializer;

    @Handles("hearing.case-defendants-updated-for-hearing")
    public void caseDefendantsUpdatedForHearing(final JsonEnvelope event) {
        final CaseDefendantsUpdatedForHearing caseDefendantsUpdatedForHearing = jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(), CaseDefendantsUpdatedForHearing.class);
        final UUID hearingId = caseDefendantsUpdatedForHearing.getHearingId();
        final ProsecutionCase prosecutionCase = caseDefendantsUpdatedForHearing.getProsecutionCase();
        final List<Defendant> defendantList = prosecutionCase.getDefendants();
        final Hearing hearingEntity = hearingRepository.findBy(hearingId);
        if (isNull(hearingEntity)) {
            LOGGER.info("Hearing not found for hearing id {} ", hearingId);
        } else {

            hearingEntity.getProsecutionCases().stream().filter(pc -> pc.getId().getId().equals(prosecutionCase.getId())).findFirst().ifPresent(persistentCase -> {
                persistentCase.setCaseStatus(prosecutionCase.getCaseStatus());
                persistentCase.getDefendants().forEach(defendant -> {
                    final UUID defendantId = defendant.getId().getId();
                    final Optional<Defendant> optionalDefendant = defendantList.stream().filter(def->def.getId().equals(defendantId)).findFirst();
                    if(optionalDefendant.isPresent()) {
                        defendant.setProceedingsConcluded(optionalDefendant.get().getProceedingsConcluded());
                        if(nonNull(optionalDefendant.get().getPersonDefendant()) && nonNull(optionalDefendant.get().getPersonDefendant().getDriverNumber())){
                            defendant.getPersonDefendant().setDriverNumber(optionalDefendant.get().getPersonDefendant().getDriverNumber());
                        }
                    }
                });
            });
            hearingRepository.save(hearingEntity);
        }

    }


    @Handles("hearing.application-defendants-updated-for-hearing")
    public void applicationDefendantsUpdatedForHearing(final JsonEnvelope event) {
        final ApplicationDefendantsUpdatedForHearing applicationDefendantsUpdatedForHearing = jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(), ApplicationDefendantsUpdatedForHearing.class);
        final UUID hearingId = applicationDefendantsUpdatedForHearing.getHearingId();
        final CourtApplication courtApplicationFromEvent = applicationDefendantsUpdatedForHearing.getCourtApplication();
        final Hearing hearingEntity = hearingRepository.findBy(hearingId);
        if (isNull(hearingEntity)) {
            LOGGER.info("Hearing not found for hearing id {} ", hearingId);
        } else {
            final List<CourtApplicationParty> defendantsFromEvent =  Stream.of(courtApplicationFromEvent.getApplicant(), courtApplicationFromEvent.getSubject())
                    .filter(courtApplicationParty -> nonNull(courtApplicationParty.getMasterDefendant()))
                    .filter(courtApplicationParty -> nonNull(courtApplicationParty.getMasterDefendant().getPersonDefendant()))
                    .collect(Collectors.toList());

            final List<CourtApplication> courtApplicationsFromDb = courtApplicationsSerializer.courtApplications(hearingEntity.getCourtApplicationsJson());
            courtApplicationsFromDb.stream()
                    .flatMap(courtApplication -> Stream.of(courtApplication.getApplicant(), courtApplication.getSubject()))
                    .filter(courtApplicationParty -> nonNull(courtApplicationParty.getMasterDefendant()))
                    .filter(courtApplicationParty -> nonNull(courtApplicationParty.getMasterDefendant().getPersonDefendant()))
                    .forEach(courtApplicationParty -> defendantsFromEvent.stream().filter(defendant -> defendant.getMasterDefendant().getMasterDefendantId().equals(courtApplicationParty.getMasterDefendant().getMasterDefendantId()))
                            .filter(defendant -> nonNull(defendant.getMasterDefendant().getPersonDefendant().getDriverNumber()))
                            .forEach(defendant -> courtApplicationParty.getMasterDefendant().getPersonDefendant().setDriverNumber(defendant.getMasterDefendant().getPersonDefendant().getDriverNumber())));

            hearingEntity.setCourtApplicationsJson(courtApplicationsSerializer.json(courtApplicationsFromDb));

            hearingRepository.save(hearingEntity);
        }

    }
}
