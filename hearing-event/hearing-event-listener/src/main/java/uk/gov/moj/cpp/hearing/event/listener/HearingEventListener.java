package uk.gov.moj.cpp.hearing.event.listener;

import static java.util.UUID.fromString;
import static uk.gov.justice.services.common.converter.ZonedDateTimes.fromJsonString;
import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.event.CaseAssociated;
import uk.gov.moj.cpp.hearing.domain.event.CourtAssigned;
import uk.gov.moj.cpp.hearing.domain.event.HearingEnded;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventDefinitionsCreated;
import uk.gov.moj.cpp.hearing.domain.event.HearingInitiated;
import uk.gov.moj.cpp.hearing.domain.event.HearingStarted;
import uk.gov.moj.cpp.hearing.domain.event.ProsecutionCounselAdded;
import uk.gov.moj.cpp.hearing.domain.event.RoomBooked;
import uk.gov.moj.cpp.hearing.event.listener.converter.HearingEventDefinitionsConverter;
import uk.gov.moj.cpp.hearing.event.listener.converter.HearingEventsToHearingConverter;
import uk.gov.moj.cpp.hearing.persist.HearingCaseRepository;
import uk.gov.moj.cpp.hearing.persist.HearingDefinitionsRepository;
import uk.gov.moj.cpp.hearing.persist.HearingEventRepository;
import uk.gov.moj.cpp.hearing.persist.HearingRepository;
import uk.gov.moj.cpp.hearing.persist.ProsecutionCounselRepository;
import uk.gov.moj.cpp.hearing.persist.entity.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.HearingCase;
import uk.gov.moj.cpp.hearing.persist.entity.HearingEvent;
import uk.gov.moj.cpp.hearing.persist.entity.ProsecutionCounsel;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;
import javax.json.JsonObject;
import javax.transaction.Transactional;

@ServiceComponent(EVENT_LISTENER)
public class HearingEventListener {

    private static final String HEARING_EVENT_ID_FIELD = "id";
    private static final String HEARING_ID_FIELD = "hearingId";
    private static final String RECORDED_LABEL_FIELD = "recordedLabel";
    private static final String TIMESTAMP_FIELD = "timestamp";

    @Inject
    private JsonObjectToObjectConverter jsonObjectConverter;

    @Inject
    private HearingEventsToHearingConverter converter;

    @Inject
    private HearingEventDefinitionsConverter hearingEventDefinitionsConverter;

    @Inject
    private HearingRepository hearingRepository;

    @Inject
    private HearingDefinitionsRepository hearingDefinitionsRepository;

    @Inject
    private HearingCaseRepository hearingCaseRepository;

    @Inject
    private ProsecutionCounselRepository prosecutionCounselRepository;

    @Inject
    private HearingEventRepository hearingEventRepository;

    @Transactional
    @Handles("hearing.hearing-initiated")
    public void hearingInitiated(final JsonEnvelope event) {
        final HearingInitiated hearingInitiated = jsonObjectConverter.convert(event.payloadAsJsonObject(), HearingInitiated.class);
        final Hearing hearing = converter.convert(hearingInitiated);
        Optional<Hearing> storedHearing = hearingRepository.getByHearingId(hearing.getHearingId());

        storedHearing.ifPresent(h ->
                h = h.builder().withStartdate(hearing.getStartdate())
                .withStartTime(hearing.getStartTime())
                .withDuration(hearing.getDuration())
                .withHearingType(hearing.getHearingType())
                .build());
        hearingRepository.save(storedHearing.orElse(hearing));
    }

    @Transactional
    @Handles("hearing.court-assigned")
    public void courtAssigned(final JsonEnvelope event) {
        CourtAssigned courtAssigned = jsonObjectConverter.convert(event.payloadAsJsonObject(), CourtAssigned.class);
        Hearing hearing = converter.convert(courtAssigned);
        Optional<Hearing> storedHearing = hearingRepository.getByHearingId(hearing.getHearingId());

        storedHearing.ifPresent(h -> h.builder().withCourtCentreName(hearing.getCourtCentreName()).build());
        hearingRepository.save(storedHearing.orElse(hearing));
    }

    @Transactional
    @Handles("hearing.room-booked")
    public void roomBooked(final JsonEnvelope event) {
        RoomBooked roomBooked = jsonObjectConverter.convert(event.payloadAsJsonObject(), RoomBooked.class);
        Hearing hearing = converter.convert(roomBooked);
        Optional<Hearing> storedHearing = hearingRepository.getByHearingId(hearing.getHearingId());

        storedHearing.ifPresent(h -> {
            h.builder().withRoomName(hearing.getRoomName()).build();
        });
        hearingRepository.save(storedHearing.orElse(hearing));
    }

    @Transactional
    @Handles("hearing.started")
    public void hearingStarted(final JsonEnvelope event) {
        HearingStarted hearingStarted = jsonObjectConverter.convert(event.payloadAsJsonObject(), HearingStarted.class);
        Hearing hearing = converter.convert(hearingStarted);
        Optional<Hearing> storedHearing = hearingRepository.getByHearingId(hearing.getHearingId());

        storedHearing.ifPresent(h -> h.builder().withStartedAt(hearing.getStartedAt()).build());
        hearingRepository.save(storedHearing.orElse(hearing));
    }

    @Transactional
    @Handles("hearing.ended")
    public void hearingEnded(final JsonEnvelope event) {
        HearingEnded hearingEnded = jsonObjectConverter.convert(event.payloadAsJsonObject(), HearingEnded.class);
        Hearing hearing = converter.convert(hearingEnded);
        Optional<Hearing> storedHearing = hearingRepository.getByHearingId(hearing.getHearingId());

        storedHearing.ifPresent(h -> h.builder().withEndedAt(hearing.getEndedAt()).build());
        hearingRepository.save(storedHearing.orElse(hearing));
    }

    @Transactional
    @Handles("hearing.prosecution-counsel-added")
    public void prosecutionCounselAdded(final JsonEnvelope event) {
        final ProsecutionCounselAdded prosecutionCounselAdded =
                jsonObjectConverter.convert(event.payloadAsJsonObject(), ProsecutionCounselAdded.class);
        final ProsecutionCounsel prosecutionCounsel = converter.convert(prosecutionCounselAdded);
        prosecutionCounselRepository.save(prosecutionCounsel);
    }

    @Transactional
    @Handles("hearing.case-associated")
    public void caseAssociated(final JsonEnvelope event) {
        CaseAssociated caseAssociated = jsonObjectConverter.convert(event.payloadAsJsonObject(), CaseAssociated.class);
        HearingCase hearing = converter.convert(caseAssociated);
        List<HearingCase> storedHearingCases = hearingCaseRepository.findByHearingId(hearing.getHearingId());
        if (storedHearingCases.stream().map(HearingCase::getCaseId)
                .noneMatch(caseId -> caseId.equals(caseAssociated.getCaseId()))) {
            hearingCaseRepository.save(hearing);
        }
    }

    @Transactional
    @Handles("hearing.hearing-event-definitions-created")
    public void hearingEventDefinitionsCreated(final JsonEnvelope event) {
        hearingDefinitionsRepository.deleteAll();
        hearingEventDefinitionsConverter
                .convert(jsonObjectConverter.convert(event.payloadAsJsonObject(), HearingEventDefinitionsCreated.class))
                .forEach(hearingDefinitionsRepository::save);
    }

    @Handles("hearing.hearing-event-logged")
    public void hearingEventLogged(final JsonEnvelope event) {
        final JsonObject payload = event.payloadAsJsonObject();

        final UUID id = fromString(payload.getString(HEARING_EVENT_ID_FIELD));
        final UUID hearingId = fromString(payload.getString(HEARING_ID_FIELD));
        final String recordedLabel = payload.getString(RECORDED_LABEL_FIELD);
        final ZonedDateTime timestamp = fromJsonString(payload.getJsonString(TIMESTAMP_FIELD));

        hearingEventRepository.save(new HearingEvent(id, hearingId, recordedLabel, timestamp));
    }

}
