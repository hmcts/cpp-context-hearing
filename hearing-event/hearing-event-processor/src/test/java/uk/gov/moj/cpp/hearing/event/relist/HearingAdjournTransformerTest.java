package uk.gov.moj.cpp.hearing.event.relist;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.Sets;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class HearingAdjournTransformerTest {

    HearingAdjournTransformer testObj = new HearingAdjournTransformer();

    @Test
    public void convertDurationIntoMinutes_when_duration_is_empty() {
        int result = testObj.convertDurationIntoMinutes(Sets.newHashSet());
        assertEquals(0, result);
    }

    @Test
    public void convertDurationIntoMinutes_when_duration_has_only_weeks() {
        int result = testObj.convertDurationIntoMinutes(Sets.newHashSet("3 weeks"));
        assertEquals(5400, result);
    }

    @Test
    public void convertDurationIntoMinutes_when_duration_has_only_days() {
        int result = testObj.convertDurationIntoMinutes(Sets.newHashSet("2 days"));
        assertEquals(720, result);
    }

    @Test
    public void convertDurationIntoMinutes_when_duration_has_only_hours() {
        int result = testObj.convertDurationIntoMinutes(Sets.newHashSet("9 hours"));
        assertEquals(540, result);
    }

    @Test
    public void convertDurationIntoMinutes_when_duration_has_only_minutes() {
        int result = testObj.convertDurationIntoMinutes(Sets.newHashSet("9 minutes"));
        assertEquals(9, result);
    }

    @Test
    public void convertDurationIntoMinutes_when_duration_has_everything() {
        int result = testObj.convertDurationIntoMinutes(Sets.newHashSet("6 weeks, 2 days, 1 hours, 3 minutes"));
        assertEquals(11583, result);
    }
}