package uk.gov.moj.cpp.hearing.mapping;

import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_ZONED_DATE_TIME;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.randomEnum;
import static uk.gov.moj.cpp.hearing.utils.HearingJPADataTemplate.aNewHearingJPADataTemplate;

import uk.gov.justice.core.courts.DelegatedPowers;
import uk.gov.justice.core.courts.HearingCaseNote;
import uk.gov.justice.core.courts.NoteType;
import uk.gov.justice.services.common.converter.ZonedDateTimes;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;

import java.time.ZonedDateTime;
import java.util.UUID;

import javax.json.JsonObject;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

public class HearingCaseNoteJPAMapperTest {

    private final ObjectMapper mapper = new ObjectMapperProducer().objectMapper();
    private HearingCaseNoteJPAMapper hearingCaseNoteJPAMapper = JPACompositeMappers.HEARING_CASE_NOTE_JPA_MAPPER;

    @Test
    public void testFromJPA() {
        final Hearing hearingEntity = aNewHearingJPADataTemplate().getHearing();
        final uk.gov.moj.cpp.hearing.persist.entity.ha.HearingCaseNote hearingCaseNoteEntity = hearingEntity.getHearingCaseNotes().iterator().next();
        final String note = STRING.next();
        final String noteDateTime = ZonedDateTimes.toString(PAST_ZONED_DATE_TIME.next());
        final UUID clerkId = randomUUID();
        final String clerkFirstName = STRING.next();
        final String clerkLastName = STRING.next();
        final UUID firstCaseId = randomUUID();
        final UUID secondCaseId = randomUUID();
        final NoteType noteType = randomEnum(NoteType.class).next();

        hearingCaseNoteEntity.setPayload(getEntityPayload(hearingCaseNoteEntity, hearingEntity, note,
                noteDateTime, clerkId, clerkFirstName, clerkLastName, firstCaseId, secondCaseId, noteType));

        final HearingCaseNote hearingCaseNote = hearingCaseNoteJPAMapper.fromJPA(hearingCaseNoteEntity);

        final JsonObject entityPayload = mapper.convertValue(hearingCaseNoteEntity.getPayload(), JsonObject.class);
        assertThat(hearingCaseNote.getOriginatingHearingId().toString(), is(entityPayload.getString("originatingHearingId")));
        assertThat(hearingCaseNote.getNoteType(), is(noteType));
        assertThat(hearingCaseNote.getNote(), is(note));
        assertThat(ZonedDateTimes.toString(hearingCaseNote.getNoteDateTime()), is(noteDateTime));
        final uk.gov.justice.core.courts.DelegatedPowers courtClerk = hearingCaseNote.getCourtClerk();
        assertThat(courtClerk.getUserId(), is(clerkId));
        assertThat(courtClerk.getFirstName(), is(clerkFirstName));
        assertThat(courtClerk.getLastName(), is(clerkLastName));
        assertThat(hearingCaseNote.getProsecutionCases().get(0), is(firstCaseId));
        assertThat(hearingCaseNote.getProsecutionCases().get(1), is(secondCaseId));
    }

    @Test
    public void testToJPA() {
        final Hearing hearingEntity = aNewHearingJPADataTemplate().getHearing();

        final String note = STRING.next();
        final ZonedDateTime noteDateTime = ZonedDateTime.now();
        final UUID clerkId = randomUUID();
        final String clerkFirstName = STRING.next();
        final String clerkLastName = STRING.next();
        final UUID firstCaseId = randomUUID();
        final UUID secondCaseId = randomUUID();
        final NoteType noteType = randomEnum(NoteType.class).next();
        final UUID originatingHearingId = hearingEntity.getId();
        final HearingCaseNote hearingCaseNote = HearingCaseNote.hearingCaseNote()
                .withNote(note)
                .withNoteType(noteType)
                .withNoteDateTime(noteDateTime)
                .withCourtClerk(DelegatedPowers.delegatedPowers()
                        .withUserId(clerkId)
                        .withLastName(clerkLastName)
                        .withFirstName(clerkFirstName)
                        .build())
                .withProsecutionCases(asList(firstCaseId, secondCaseId))
                .withOriginatingHearingId(originatingHearingId)
                .build();


        final uk.gov.moj.cpp.hearing.persist.entity.ha.HearingCaseNote hearingCaseNoteEntity = hearingCaseNoteJPAMapper.toJPA(hearingEntity, hearingCaseNote);

        assertThat(hearingCaseNoteEntity.getHearing().getId(), is(originatingHearingId));
        final JsonObject payload = mapper.convertValue(hearingCaseNoteEntity.getPayload(), JsonObject.class);
        assertThat(payload.getString("note"), is(note));
        assertThat(payload.getString("noteDateTime"), is(ZonedDateTimes.toString(noteDateTime)));
        assertThat(payload.getString("noteType"), is(noteType.toString()));
        assertThat(payload.getString("originatingHearingId"), is(originatingHearingId.toString()));
        assertThat((payload.getJsonArray("prosecutionCases").getString(0)), is(firstCaseId.toString()));
        final JsonObject courtClerk = payload.getJsonObject("courtClerk");
        assertThat(courtClerk.getString("userId"), is(clerkId.toString()));
        assertThat(courtClerk.getString("lastName"), is(clerkLastName));
        assertThat(courtClerk.getString("firstName"), is(clerkFirstName));
    }

    private JsonNode getEntityPayload(final uk.gov.moj.cpp.hearing.persist.entity.ha.HearingCaseNote hearingCaseNoteEntity, final Hearing hearingEntity,
                                      final String note, final String noteDateTime, final UUID clerkId, final String clerkFirstName,
                                      final String clerkLastName, final UUID firstCaseId, final UUID secondCaseId, final NoteType noteType) {
        final HearingCaseNote caseNote = HearingCaseNote.hearingCaseNote()
                .withOriginatingHearingId(hearingEntity.getId())
                .withNote(note)
                .withCourtClerk(DelegatedPowers.delegatedPowers()
                        .withUserId(clerkId)
                        .withFirstName(clerkFirstName)
                        .withLastName(clerkLastName)
                        .build())
                .withNoteDateTime(ZonedDateTimes.fromString(noteDateTime))
                .withProsecutionCases(asList(firstCaseId, secondCaseId))
                .withNoteType(noteType)
                .build();

        return mapper.valueToTree(caseNote);
    }
}