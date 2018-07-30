package uk.gov.moj.cpp.hearing.event.listener;

import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.event.HearingDetailChanged;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingDate;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Judge;
import uk.gov.moj.cpp.hearing.repository.HearingRepository;

import java.util.ArrayList;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.transaction.Transactional;

@ServiceComponent(EVENT_LISTENER)
public class ChangeHearingDetailCommandHandler {
    @Inject
    JsonObjectToObjectConverter jsonObjectToObjectConverter;
    @Inject
    private HearingRepository hearingRepository;

    @Transactional
    @Handles("hearing.event.detail-changed")
    public void hearingDetailChanged(final JsonEnvelope event) {

        final HearingDetailChanged hearingDetailChanged = jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(), HearingDetailChanged.class);

        final Hearing hearing = hearingRepository.findById(hearingDetailChanged.getId());

        final Hearing updatedHearingEntity = Hearing.builder()
                .withId(hearing.getId())
                .withHearingType(hearingDetailChanged.getType())
                .withCourtCentreId(hearing.getCourtCentreId())
                .withCourtCentreName(hearing.getCourtCentreName())
                .withRoomId(hearingDetailChanged.getCourtRoomId())
                .withRoomName(hearingDetailChanged.getCourtRoomName())
                .withHearingDays(hearingDetailChanged.getHearingDays().stream()
                        .map(zdt -> HearingDate.builder()
                                .withDateTime(zdt)
                                .withDate(zdt.toLocalDate())
                                .withId(new HearingSnapshotKey(UUID.randomUUID(), hearing.getId()))
                                .build())
                        .collect(Collectors.toList()))
                .withDefendants(hearing.getDefendants())

                .withWitnesses((hearing.getWitnesses() == null ? new ArrayList<>() : hearing.getWitnesses()))
                .withJudge(Judge.builder()
                        .withId(new HearingSnapshotKey(hearingDetailChanged.getJudge().getId(), hearing.getId()))
                        .withFirstName(hearingDetailChanged.getJudge().getFirstName())
                        .withLastName(hearingDetailChanged.getJudge().getLastName())
                        .withTitle(hearingDetailChanged.getJudge().getTitle()))
                .build();

        hearingRepository.save(updatedHearingEntity);
    }
}
