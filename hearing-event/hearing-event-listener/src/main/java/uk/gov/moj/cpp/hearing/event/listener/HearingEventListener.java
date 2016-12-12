package uk.gov.moj.cpp.hearing.event.listener;

import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.event.CaseAssociated;
import uk.gov.moj.cpp.hearing.domain.event.CourtAssigned;
import uk.gov.moj.cpp.hearing.domain.event.HearingEnded;
import uk.gov.moj.cpp.hearing.domain.event.HearingInitiated;
import uk.gov.moj.cpp.hearing.domain.event.HearingStarted;
import uk.gov.moj.cpp.hearing.domain.event.RoomBooked;
import uk.gov.moj.cpp.hearing.event.listener.converter.HearingEventsToHearingConverter;
import uk.gov.moj.cpp.hearing.persist.HearingCaseRepository;
import uk.gov.moj.cpp.hearing.persist.HearingRepository;
import uk.gov.moj.cpp.hearing.persist.entity.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.HearingCase;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.transaction.Transactional;

@ServiceComponent(EVENT_LISTENER)
public class HearingEventListener {

    @Inject
    private JsonObjectToObjectConverter jsonObjectConverter;

    @Inject
    private HearingEventsToHearingConverter converter;

    @Inject
    private HearingRepository hearingRepository;

    @Inject
    private HearingCaseRepository hearingCaseRepository;

    @Transactional
    @Handles("hearing.hearing-initiated")
    public void hearingInitiated(final JsonEnvelope event) {
        HearingInitiated hearingInitiated = jsonObjectConverter.convert(event.payloadAsJsonObject(), HearingInitiated.class);
        Hearing hearing = converter.convert(hearingInitiated);
        Optional<Hearing> storedHearing = hearingRepository.getByHearingId(hearing.geHearingId());

        storedHearing.ifPresent(h -> {
            h.setStartdate(hearing.getStartdate());
            h.setStartTime(hearing.getStartTime());
            h.setDuration(hearing.getDuration());
            h.setHearingType(hearing.getHearingType());
        });
        hearingRepository.save(storedHearing.orElse(hearing));
    }

    @Transactional
    @Handles("hearing.court-assigned")
    public void courtAssigned(final JsonEnvelope event) {
        CourtAssigned courtAssigned = jsonObjectConverter.convert(event.payloadAsJsonObject(), CourtAssigned.class);
        Hearing hearing = converter.convert(courtAssigned);
        Optional<Hearing> storedHearing = hearingRepository.getByHearingId(hearing.geHearingId());

        storedHearing.ifPresent(h -> {
            h.setCourtCentreName(hearing.getCourtCentreName());
        });
        hearingRepository.save(storedHearing.orElse(hearing));
    }

    @Transactional
    @Handles("hearing.room-booked")
    public void roomBooked(final JsonEnvelope event) {
        RoomBooked roomBooked = jsonObjectConverter.convert(event.payloadAsJsonObject(), RoomBooked.class);
        Hearing hearing = converter.convert(roomBooked);
        Optional<Hearing> storedHearing = hearingRepository.getByHearingId(hearing.geHearingId());

        storedHearing.ifPresent(h -> {
            h.setRoomName(hearing.getRoomName());
        });
        hearingRepository.save(storedHearing.orElse(hearing));
    }

    @Transactional
    @Handles("hearing.started")
    public void hearingStarted(final JsonEnvelope event) {
        HearingStarted hearingStarted = jsonObjectConverter.convert(event.payloadAsJsonObject(), HearingStarted.class);
        Hearing hearing = converter.convert(hearingStarted);
        Optional<Hearing> storedHearing = hearingRepository.getByHearingId(hearing.geHearingId());

        storedHearing.ifPresent(h -> {
            h.setStartedAt(hearing.getStartedAt());
        });
        hearingRepository.save(storedHearing.orElse(hearing));
    }

    @Transactional
    @Handles("hearing.ended")
    public void hearingEnded(final JsonEnvelope event) {
        HearingEnded hearingEnded = jsonObjectConverter.convert(event.payloadAsJsonObject(), HearingEnded.class);
        Hearing hearing = converter.convert(hearingEnded);
        Optional<Hearing> storedHearing = hearingRepository.getByHearingId(hearing.geHearingId());

        storedHearing.ifPresent(h -> {
            h.setEndedAt(hearing.getEndedAt());
        });
        hearingRepository.save(storedHearing.orElse(hearing));
    }

    @Transactional
    @Handles("hearing.prosecution-counsel-added")
    public void prosecutionCounselAdded(final JsonEnvelope event) {
       //TODO should be implemented
    }
    @Transactional
    @Handles("hearing.case-associated")
    public void caseAssociated(final JsonEnvelope event) {
        CaseAssociated caseAssociated = jsonObjectConverter.convert(event.payloadAsJsonObject(), CaseAssociated.class);
        HearingCase hearing = converter.convert(caseAssociated);
        List<HearingCase> storedHearingCases = hearingCaseRepository.findByHearingId(hearing.getHearingId());
        if (!storedHearingCases.stream().map(hearingCase -> hearingCase.getCaseId())
                .filter(caseId -> caseId.equals(caseAssociated.getCaseId()))
                .findFirst()
                .isPresent()) {
            hearingCaseRepository.save(hearing);
        }
    }
}
