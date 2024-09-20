package uk.gov.moj.cpp.hearing.domain.aggregate;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.randomEnum;

import uk.gov.justice.core.courts.DelegatedPowers;
import uk.gov.justice.core.courts.HearingCaseNote;
import uk.gov.justice.core.courts.NoteType;
import uk.gov.moj.cpp.hearing.domain.event.HearingCaseNoteSaved;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

public class HearingCaseNoteTest {
    @Test
    public void shouldRecordCaseNote() {
        final ZonedDateTime noteTime = ZonedDateTime.now();
        final List<UUID> prosecutionCases = asList(UUID.randomUUID(), UUID.randomUUID());
        final UUID originatingHearingId = UUID.randomUUID();
        final DelegatedPowers courtClerk = mock(DelegatedPowers.class);
        final NoteType noteType = randomEnum(NoteType.class).next();
        final String note = STRING.next();

        final HearingCaseNote hearingCaseNote = HearingCaseNote.hearingCaseNote()
                .withOriginatingHearingId(originatingHearingId)
                .withCourtClerk(courtClerk)
                .withNoteType(noteType)
                .withNote(note)
                .withNoteDateTime(noteTime)
                .withProsecutionCases(prosecutionCases)
                .build();

        final uk.gov.moj.cpp.hearing.domain.aggregate.HearingCaseNote aggregate = new uk.gov.moj.cpp.hearing.domain.aggregate.HearingCaseNote();

        final Stream<Object> objectStream = aggregate.saveCaseNote(hearingCaseNote);

        final List<Object> events = objectStream.collect(Collectors.toList());
        assertThat(events.size(), is(greaterThan(0)));
        final HearingCaseNoteSaved hearingCaseNoteSaved = (HearingCaseNoteSaved) events.get(0);
        final HearingCaseNote caseNote = hearingCaseNoteSaved.getHearingCaseNote();
        assertThat(caseNote.getCourtClerk(), is(courtClerk));
        assertThat(caseNote.getNote(), is(note));
        assertThat(caseNote.getNoteDateTime(), is(noteTime));
        assertThat(caseNote.getNoteType(), is(noteType));
        assertThat(caseNote.getOriginatingHearingId(), is(originatingHearingId));
        assertThat(caseNote.getProsecutionCases(), equalTo(prosecutionCases));
    }
}
