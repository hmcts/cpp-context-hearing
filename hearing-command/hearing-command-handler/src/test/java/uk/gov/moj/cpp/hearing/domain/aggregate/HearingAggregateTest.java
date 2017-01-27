package uk.gov.moj.cpp.hearing.domain.aggregate;

import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertTrue;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;

import uk.gov.moj.cpp.hearing.domain.event.DefenceCounselAdded;
import uk.gov.moj.cpp.hearing.domain.event.ProsecutionCounselAdded;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;

public class HearingAggregateTest {

    private HearingAggregate hearingAggregate;

    @Before
    public void setUP() {
        hearingAggregate = new HearingAggregate();
    }

    @Test
    public void shouldAddProsecutionCounselToAHearing() {
        final UUID hearingId = randomUUID();
        final UUID attendeeId = randomUUID();
        final UUID personId = randomUUID();
        final String status = STRING.next();

        final Stream<Object> stream = hearingAggregate.addProsecutionCounsel(hearingId, attendeeId,
                personId, status);

        final Optional<Object> optional = stream.findFirst();
        assertTrue(optional.isPresent());

        final ProsecutionCounselAdded prosecutionCounselAdded = (ProsecutionCounselAdded) optional.get();
        assertThat(prosecutionCounselAdded.getHearingId(), is(hearingId));
        assertThat(prosecutionCounselAdded.getAttendeeId(), is(attendeeId));
        assertThat(prosecutionCounselAdded.getPersonId(), is(personId));
        assertThat(prosecutionCounselAdded.getStatus(), is(status));
    }

    @Test
    public void shouldAddDefenceCounselToAHearing() {
        final UUID hearingId = randomUUID();
        final UUID attendeeId = randomUUID();
        final UUID personId = randomUUID();
        final String status = STRING.next();
        final UUID defendantId1 = randomUUID();
        final UUID defendantId2 = randomUUID();

        final Stream<Object> stream = hearingAggregate.addDefenceCounsel(hearingId, attendeeId,
                personId, asList(defendantId1, defendantId2), status);

        final Optional<Object> optional = stream.findFirst();
        assertTrue(optional.isPresent());

        final DefenceCounselAdded defenceCounselAdded = (DefenceCounselAdded) optional.get();
        assertThat(defenceCounselAdded.getHearingId(), is(hearingId));
        assertThat(defenceCounselAdded.getAttendeeId(), is(attendeeId));
        assertThat(defenceCounselAdded.getPersonId(), is(personId));
        assertThat(defenceCounselAdded.getDefendantIds(), hasItem(defendantId1));
        assertThat(defenceCounselAdded.getDefendantIds(), hasItem(defendantId2));
        assertThat(defenceCounselAdded.getDefendantIds(), hasSize(2));
        assertThat(defenceCounselAdded.getStatus(), is(status));
    }

}