package uk.gov.moj.cpp.hearing.event.listener;

import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;
import static uk.gov.moj.cpp.hearing.Utilities.with;

import uk.gov.justice.json.schemas.core.JudicialRoleType;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.event.HearingDetailChanged;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingDay;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingType;
import uk.gov.moj.cpp.hearing.persist.entity.ha.JudicialRole;
import uk.gov.moj.cpp.hearing.repository.HearingRepository;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.UUID;

@ServiceComponent(EVENT_LISTENER)
public class ChangeHearingDetailEventListener {

    @Inject
    JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Inject
    private HearingRepository hearingRepository;

    @Transactional
    @Handles("hearing.event.detail-changed")
    public void hearingDetailChanged(final JsonEnvelope event) {

        final HearingDetailChanged hearingDetailChanged = jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(), HearingDetailChanged.class);

        final Hearing hearing = hearingRepository.findBy(hearingDetailChanged.getId());

        final HearingType HearingType = new HearingType();
        HearingType.setDescription(hearingDetailChanged.getType());

        hearing.setHearingType(with(new HearingType(), type -> type.setDescription(hearingDetailChanged.getType())));

        hearing.setCourtCentre(with(hearing.getCourtCentre(), courtCentre -> {
            courtCentre.setRoomId(hearingDetailChanged.getCourtRoomId());
            courtCentre.setRoomName(hearingDetailChanged.getCourtRoomName());
        }));

        //TODO - this relies on there only being one judge ever.
        hearing.getJudicialRoles().removeIf(j -> j.getJudicialRoleType() == JudicialRoleType.DISTRICT_JUDGE || j.getJudicialRoleType() == JudicialRoleType.CIRCUIT_JUDGE);

        //TODO - this needs to be updated to be inline with new schemas
        hearing.getJudicialRoles().add(with(new JudicialRole(), judicialRole -> {
            judicialRole.setId(new HearingSnapshotKey(hearingDetailChanged.getJudge().getId(), hearingDetailChanged.getId()));
            judicialRole.setJudicialId(hearingDetailChanged.getJudge().getId());
            judicialRole.setFirstName(hearingDetailChanged.getJudge().getFirstName());
            judicialRole.setLastName(hearingDetailChanged.getJudge().getLastName());
            judicialRole.setTitle(hearingDetailChanged.getJudge().getTitle());
            judicialRole.setJudicialRoleType(JudicialRoleType.DISTRICT_JUDGE); //TODO - this is not correct
        }));

        hearing.getHearingDays().clear();
        hearingDetailChanged.getHearingDays().stream()
                .map(date -> with(new HearingDay(), day -> {
                    day.setId(new HearingSnapshotKey(UUID.randomUUID(), hearing.getId()));
                    day.setSittingDay(date);
                }))
                .forEach(day -> hearing.getHearingDays().add(day));

        hearingRepository.save(hearing);
    }
}
