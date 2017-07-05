package uk.gov.moj.cpp.hearing.persist.entity;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static java.util.UUID.randomUUID;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_ZONED_DATE_TIME;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;


import java.time.ZonedDateTime;
import java.util.UUID;

import org.junit.Test;

public class HearingEventTest {

    private static final UUID HEARING_ID = randomUUID();

    private static final UUID HEARING_EVENT_ID = randomUUID();
    private static final String RECORDED_LABEL = STRING.next();
    private static final ZonedDateTime EVENT_TIME = PAST_ZONED_DATE_TIME.next();
    private static final ZonedDateTime LAST_MODIFIED_TIME = PAST_ZONED_DATE_TIME.next();

    @Test
    public void shouldHaveANoArgsConstructor() {
        assertThat(HearingEvent.class, hasValidBeanConstructor());
    }

    @Test
    public void shouldCreateNewObjectWithSameValuesIfBuilderDoesNotOverwriteAnyFields() {
        final HearingEvent hearingEvent = getHearingEvent();

        final HearingEvent actualHearingEvent = hearingEvent.builder().build();

        assertThat(actualHearingEvent, is(not(equalTo(hearingEvent))));

        assertThat(actualHearingEvent.getHearingId(), is(hearingEvent.getHearingId()));
        assertThat(actualHearingEvent.getId(), is(hearingEvent.getId()));
        assertThat(actualHearingEvent.getRecordedLabel(), is(hearingEvent.getRecordedLabel()));
        assertThat(actualHearingEvent.getEventTime(), is(hearingEvent.getEventTime()));
        assertThat(actualHearingEvent.getLastModifiedTime(), is(hearingEvent.getLastModifiedTime()));
        assertThat(actualHearingEvent.isDeleted(), is(hearingEvent.isDeleted()));
    }

    @Test
    public void shouldBeAbleToOverwriteFieldsFromBuilder() {
        final HearingEvent hearingEvent = getHearingEvent();

        final HearingEvent updatedHearingEvent = hearingEvent.builder()
                .withId(randomUUID())
                .withEventTime(PAST_ZONED_DATE_TIME.next())
                .withLastModifiedTime(PAST_ZONED_DATE_TIME.next())
                .delete()
                .build();

        assertThat(updatedHearingEvent, is(not(equalTo(hearingEvent))));

        assertThat(updatedHearingEvent.getHearingId(), is(hearingEvent.getHearingId()));
        assertThat(updatedHearingEvent.getId(), is(not(hearingEvent.getId())));
        assertThat(updatedHearingEvent.getRecordedLabel(), is(hearingEvent.getRecordedLabel()));
        assertThat(updatedHearingEvent.getEventTime(), is(not(hearingEvent.getEventTime())));
        assertThat(updatedHearingEvent.getLastModifiedTime(), is(not(hearingEvent.getLastModifiedTime())));
        assertThat(updatedHearingEvent.isDeleted(), is(not(hearingEvent.isDeleted())));
    }

    private HearingEvent getHearingEvent() {
        return new HearingEvent(HEARING_EVENT_ID, HEARING_ID, RECORDED_LABEL, EVENT_TIME, LAST_MODIFIED_TIME);
    }
}