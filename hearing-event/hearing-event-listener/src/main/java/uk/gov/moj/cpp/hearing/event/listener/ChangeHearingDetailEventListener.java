package uk.gov.moj.cpp.hearing.event.listener;

import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;
import static uk.gov.moj.cpp.hearing.Utilities.with;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.event.HearingDetailChanged;
import uk.gov.moj.cpp.hearing.mapping.HearingDayJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.JudicialRoleJPAMapper;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.repository.HearingRepository;
import javax.inject.Inject;
import javax.transaction.Transactional;

@ServiceComponent(EVENT_LISTENER)
public class ChangeHearingDetailEventListener {

    @Inject
    JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Inject
    private HearingRepository hearingRepository;

    @Inject
    private JudicialRoleJPAMapper judicialRoleJPAMapper;

    @Inject
    private HearingDayJPAMapper hearingDayJPAMapper;

    @Transactional
    @Handles("hearing.event.detail-changed")
    public void hearingDetailChanged(final JsonEnvelope event) {

        final HearingDetailChanged hearingDetailChanged = jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(), HearingDetailChanged.class);

        final Hearing hearing = hearingRepository.findBy(hearingDetailChanged.getId());

        hearing.setHearingType(with(hearing.getHearingType(), type -> {
            type.setId(hearingDetailChanged.getType().getId());
            type.setDescription(hearingDetailChanged.getType().getDescription());
        }));

        hearing.setJurisdictionType(hearingDetailChanged.getJurisdictionType());
        hearing.setReportingRestrictionReason(hearingDetailChanged.getReportingRestrictionReason());
        hearing.setHearingLanguage(hearingDetailChanged.getHearingLanguage());

        hearing.getHearingDays().clear();
        hearing.getHearingDays().addAll(hearingDayJPAMapper.toJPA(hearing, hearingDetailChanged.getHearingDays()));

        hearing.setCourtCentre(with(hearing.getCourtCentre(), courtCentre -> {
            courtCentre.setId(hearingDetailChanged.getCourtCentre().getId());
            courtCentre.setName(hearingDetailChanged.getCourtCentre().getName());
            courtCentre.setRoomId(hearingDetailChanged.getCourtCentre().getRoomId());
            courtCentre.setRoomName(hearingDetailChanged.getCourtCentre().getRoomName());
            courtCentre.setWelshName(hearingDetailChanged.getCourtCentre().getWelshName());
            courtCentre.setWelshRoomName(hearingDetailChanged.getCourtCentre().getWelshRoomName());
        }));

        hearing.getJudicialRoles().clear();
        hearing.getJudicialRoles().addAll(judicialRoleJPAMapper.toJPA(hearing, hearingDetailChanged.getJudiciary()));

        hearingRepository.save(hearing);
    }
}
