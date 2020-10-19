package uk.gov.moj.cpp.hearing.event.listener;

import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.moj.cpp.hearing.domain.event.HearingDaysWithoutCourtCentreCorrected;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingDay;
import uk.gov.moj.cpp.hearing.repository.HearingRepository;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

@ServiceComponent(EVENT_LISTENER)
public class HearingDaysWithoutCourtCenterCorrectedEventListener {

    @Inject
    private HearingRepository hearingRepository;


    @Handles("hearing.event.hearing-days-without-court-centre-corrected")
    public void hearingDaysWithoutCourtCenterCorrected(final Envelope<HearingDaysWithoutCourtCentreCorrected> event) {

        final HearingDaysWithoutCourtCentreCorrected hearingCorrectionInfo = event.payload();

        final Hearing hearing = hearingRepository.findBy(hearingCorrectionInfo.getId());
        if (hearing == null) {
            return;
        }

        final Set<HearingDay> hearingDays = hearing.getHearingDays();
        if (isEmpty(hearingDays)) {
            return;
        }

        final List<HearingDay> hearingDaysToUpdate = hearingDays.stream().filter(
                d -> (d.getCourtCentreId() == null || d.getCourtRoomId() == null)
        ).collect(Collectors.toList());

        if (isEmpty(hearingDaysToUpdate)) {
            return;
        }

        final uk.gov.justice.core.courts.HearingDay correctInfo = hearingCorrectionInfo.getHearingDays().get(0);

        hearingDaysToUpdate.forEach(d -> {
            if (d.getCourtCentreId() == null) {
                d.setCourtCentreId(correctInfo.getCourtCentreId());
            }

            if (d.getCourtRoomId() == null) {
                d.setCourtRoomId(correctInfo.getCourtRoomId());
            }
        });

        hearingRepository.save(hearing);
    }
}