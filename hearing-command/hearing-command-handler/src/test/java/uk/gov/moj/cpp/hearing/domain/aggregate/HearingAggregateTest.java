package uk.gov.moj.cpp.hearing.domain.aggregate;

import static java.lang.String.format;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.INTEGER;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;

import uk.gov.justice.services.test.utils.core.random.RandomGenerator;
import uk.gov.moj.cpp.hearing.domain.command.InitiateHearing;
import uk.gov.moj.cpp.hearing.domain.event.HearingInitiated;
import uk.gov.moj.cpp.hearing.domain.event.ProsecutionCounselAdded;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.junit.Before;
import org.junit.Test;

public class HearingAggregateTest {

    private HearingAggregate hearingAggregate;

    @Before
    public void setUP() {
        hearingAggregate = new HearingAggregate();
    }


    @Test
    public void shouldCreateHearing() {
        // given
        UUID hearingId = randomUUID();
        // and
        InitiateHearing initiateHearing = createHearing(hearingId);

        // when
        final Stream<Object> stream = hearingAggregate.initiateHearing(initiateHearing);


        // then
        Optional<Object> optional = stream.findFirst();
        assertThat(optional.isPresent(), is(true));
        // and
        HearingInitiated hearingInitiated = (HearingInitiated) optional.get();
        // and
        assertThat(hearingInitiated, isFrom(initiateHearing));
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

    private InitiateHearing createHearing(UUID hearingId) {
        UUID caseId = randomUUID();
        ZonedDateTime startDateOfHearing = ZonedDateTime.now();
        Integer duration = INTEGER.next();
        return new InitiateHearing(hearingId, startDateOfHearing, duration, "TRAIL");
    }

    private Matcher<HearingInitiated> isFrom(final InitiateHearing initiateHearing) {
        return new TypeSafeDiagnosingMatcher<HearingInitiated>() {

            @Override
            public void describeTo(Description description) {
                description.appendText(initiateHearing.toString());
            }

            @Override
            protected boolean matchesSafely(HearingInitiated hearingInitiated, Description description) {
                boolean returnStatus = true;

                if (!Objects.equals(initiateHearing.getHearingId(), hearingInitiated.getHearingId())) {
                    description.appendText(format("HearingId Mismatch:initiateHearing:%s, hearingInitiated%s",
                            initiateHearing.getHearingId(), hearingInitiated.getHearingId()));
                    returnStatus = false;
                }

                if (!Objects.equals(initiateHearing.getDuration(), hearingInitiated.getDuration())) {
                    description.appendText(format("Duration Mismatch:initiateHearing:%s, hearingInitiated%s",
                            initiateHearing.getDuration(), hearingInitiated.getDuration()));
                    returnStatus = false;
                }

                if (!Objects.equals(initiateHearing.getStartDateTime(), hearingInitiated.getStartDateTime())) {
                    description.appendText(format("StartDateOfHearing Mismatch:initiateHearing:%s, hearingInitiated%s",
                            initiateHearing.getStartDateTime(), hearingInitiated.getStartDateTime()));
                    returnStatus = false;
                }

                return returnStatus;
            }
        };
    }

}