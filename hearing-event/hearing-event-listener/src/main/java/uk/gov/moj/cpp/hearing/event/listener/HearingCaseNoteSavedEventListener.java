package uk.gov.moj.cpp.hearing.event.listener;

import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

import uk.gov.justice.core.courts.DelegatedPowers;
import uk.gov.justice.core.courts.HearingCaseNote;
import uk.gov.justice.core.courts.NoteType;
import uk.gov.justice.services.common.converter.ZonedDateTimes;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.event.HearingCaseNoteSaved;
import uk.gov.moj.cpp.hearing.mapping.HearingCaseNoteJPAMapper;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.repository.HearingCaseNoteRepository;
import uk.gov.moj.cpp.hearing.repository.HearingRepository;

import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServiceComponent(EVENT_LISTENER)
public class HearingCaseNoteSavedEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(HearingCaseNoteSavedEventListener.class.getName());

    @Inject
    private HearingRepository hearingRepository;

    @Inject
    private HearingCaseNoteRepository hearingCaseNoteRepository;

    @Inject
    private HearingCaseNoteJPAMapper hearingCaseNoteJPAMapper;

    @Transactional
    @Handles("hearing.hearing-case-note-saved")
    public void saveHearingCaseNoteListener(final JsonEnvelope envelope) {

        final JsonObject payloadAsJsonObject = envelope.payloadAsJsonObject();
        final JsonObject caseNote = payloadAsJsonObject.getJsonObject("hearingCaseNote");

        final JsonObject courtClerk = caseNote.getJsonObject("courtClerk");

        final HearingCaseNoteSaved caseNoteSaved = HearingCaseNoteSaved.hearingCaseNoteSaved()
                .withHearingCaseNote(HearingCaseNote.hearingCaseNote()
                        .withNote(caseNote.getString("note"))
                        .withNoteType(NoteType.valueOf(caseNote.getString("noteType")))
                        .withNoteDateTime(ZonedDateTimes.fromString(caseNote.getString("noteDateTime")))
                        .withProsecutionCases(caseNote
                                .getJsonArray("prosecutionCases")
                                .stream()
                                .map(e -> UUID.fromString(((JsonString) e).getString()))
                                .collect(Collectors.toList()))
                        .withOriginatingHearingId(UUID.fromString(caseNote.getString("originatingHearingId")))
                        .withCourtClerk(DelegatedPowers.delegatedPowers()
                                .withUserId(UUID.fromString(courtClerk.getString("userId")))
                                .withFirstName(courtClerk.getString("firstName"))
                                .withLastName(courtClerk.getString("lastName"))
                                .build())
                        .build())
                .build();

        final HearingCaseNote hearingCaseNote = caseNoteSaved.getHearingCaseNote();

        final Hearing hearing = hearingRepository.findBy(hearingCaseNote.getOriginatingHearingId());


        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearingId {} , caseNote {}", hearingCaseNote.getOriginatingHearingId(), caseNoteSaved.getHearingCaseNote().getId());
        }

        if (hearing == null) {
            LOGGER.error("Hearing not found");
        } else {
            final uk.gov.moj.cpp.hearing.persist.entity.ha.HearingCaseNote hearingCaseNoteEntity = hearingCaseNoteJPAMapper.toJPA(hearing, hearingCaseNote);
            hearingCaseNoteEntity.setId(new HearingSnapshotKey(UUID.randomUUID(), hearing.getId()));
            hearingCaseNoteRepository.save(hearingCaseNoteEntity);
        }

    }
}
