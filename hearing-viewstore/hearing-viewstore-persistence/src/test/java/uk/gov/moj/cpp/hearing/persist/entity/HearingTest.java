package uk.gov.moj.cpp.hearing.persist.entity;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.INTEGER;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_LOCAL_DATE;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_ZONED_DATE_TIME;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.UUID;

import org.junit.Test;

public class HearingTest {

    private static final UUID HEARING_ID = randomUUID();
    private static final LocalDate START_DATE = PAST_LOCAL_DATE.next();
    private static final LocalTime START_TIME = LocalTime.now();
    private static final Integer DURATION = INTEGER.next();
    private static final String ROOM_NAME = STRING.next();
    private static final String HEARING_TYPE = STRING.next();
    private static final String COURT_CENTRE_NAME = STRING.next();
    private static final ZonedDateTime STARTED_AT = PAST_ZONED_DATE_TIME.next();
    private static final ZonedDateTime ENDED_AT = STARTED_AT.plusHours(10);

    @Test
    public void shouldHaveANoArgsConstructor() {
        assertThat(Hearing.class, hasValidBeanConstructor());
    }

    @Test
    public void shouldCreateNewObjectWithSameValuesIfBuilderDoesNotOverwriteAnyFields() {
        final Hearing hearing = getHearing();

        final Hearing actualHearing = hearing.builder().build();

        assertThat(actualHearing, is(not(equalTo(hearing))));
        assertThat(actualHearing.getHearingId(), is(hearing.getHearingId()));
        assertThat(actualHearing.getStartdate(), is(hearing.getStartdate()));
        assertThat(actualHearing.getStartTime(), is(hearing.getStartTime()));
        assertThat(actualHearing.getDuration(), is(hearing.getDuration()));
        assertThat(actualHearing.getRoomName(), is(hearing.getRoomName()));
        assertThat(actualHearing.getHearingType(), is(hearing.getHearingType()));
        assertThat(actualHearing.getCourtCentreName(), is(hearing.getCourtCentreName()));
        assertThat(actualHearing.getStartedAt(), is(hearing.getStartedAt()));
        assertThat(actualHearing.getEndedAt(), is(hearing.getEndedAt()));
    }

    @Test
    public void shouldBeAbleToOverwriteFieldsFromBuilder() {
        final Hearing hearing = getHearing();

        final Hearing updatedHearing = hearing.builder()
                .withStartdate(PAST_LOCAL_DATE.next())
                .withStartTime(LocalTime.now().plusMinutes(1))
                .withDuration(INTEGER.next())
                .withRoomName(STRING.next())
                .withHearingType(STRING.next())
                .withCourtCentreName(STRING.next())
                .withStartedAt(PAST_ZONED_DATE_TIME.next())
                .withEndedAt(PAST_ZONED_DATE_TIME.next())
                .build();

        assertThat(updatedHearing, is(not(equalTo(hearing))));
        assertThat(updatedHearing.getHearingId(), is(hearing.getHearingId()));
        assertThat(updatedHearing.getStartdate(), is(not(hearing.getStartdate())));
        assertThat(updatedHearing.getStartTime(), is(not(hearing.getStartTime())));
        assertThat(updatedHearing.getDuration(), is(not(hearing.getDuration())));
        assertThat(updatedHearing.getRoomName(), is(not(hearing.getRoomName())));
        assertThat(updatedHearing.getHearingType(), is(not(hearing.getHearingType())));
        assertThat(updatedHearing.getCourtCentreName(), is(not(hearing.getCourtCentreName())));
        assertThat(updatedHearing.getStartedAt(), is(not(hearing.getStartedAt())));
        assertThat(updatedHearing.getEndedAt(), is(not(hearing.getEndedAt())));
    }

    private Hearing getHearing() {
        return new Hearing(HEARING_ID, START_DATE, START_TIME, DURATION, ROOM_NAME, HEARING_TYPE,
                COURT_CENTRE_NAME, STARTED_AT, ENDED_AT);
    }

}