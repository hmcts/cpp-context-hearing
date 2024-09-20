package uk.gov.moj.cpp.hearing.mapping;

import static io.github.benas.randombeans.EnhancedRandomBuilder.aNewEnhancedRandom;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.gov.moj.cpp.hearing.mapping.HearingEventJPAMapper.*;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;

import uk.gov.justice.core.courts.HearingEvent;
import uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher;

import org.junit.jupiter.api.Test;

public class HearingEventJPAMapperTest {


    public static BeanMatcher<uk.gov.justice.core.courts.HearingEvent> whenHearingEvent(final BeanMatcher<HearingEvent> m,
                                                                                        final uk.gov.moj.cpp.hearing.persist.entity.ha.HearingEvent entity) {
        return m.with(HearingEvent::getId, is(entity.getId()))
                .with(HearingEvent::getDefenceCounselId, is(entity.getDefenceCounselId()))
                .with(HearingEvent::getEventDate, is(entity.getEventDate().toString()))
                .with(HearingEvent::getEventTime, is(entity.getEventTime()))
                .with(HearingEvent::getHearingEventDefinitionId, is(entity.getHearingEventDefinitionId()))
                .with(HearingEvent::getLastModifiedTime, is(entity.getLastModifiedTime()))
                .with(HearingEvent::getRecordedLabel, is(entity.getRecordedLabel()))
                .with(HearingEvent::getHearingId, is(entity.getHearingId()));
    }

    @Test
    public void testFromJPA() {
        final uk.gov.moj.cpp.hearing.persist.entity.ha.HearingEvent hearingEvent = aNewEnhancedRandom().nextObject(uk.gov.moj.cpp.hearing.persist.entity.ha.HearingEvent.class);
        assertThat(fromJPA(hearingEvent), whenHearingEvent(isBean(HearingEvent.class), hearingEvent));
    }
}