package uk.gov.moj.cpp.hearing.domain.transformation.judicialresult.domain;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.moj.cpp.hearing.domain.transformation.judicialresult.domain.EventToTransform.APPLICATION_DETAIL_CHANGED;
import static uk.gov.moj.cpp.hearing.domain.transformation.judicialresult.domain.EventToTransform.CASE_DEFENDANTS_UPDATED;
import static uk.gov.moj.cpp.hearing.domain.transformation.judicialresult.domain.EventToTransform.CASE_DEFENDANTS_UPDATED_FOR_HEARING;
import static uk.gov.moj.cpp.hearing.domain.transformation.judicialresult.domain.EventToTransform.HEARING_EXTENDED;
import static uk.gov.moj.cpp.hearing.domain.transformation.judicialresult.domain.EventToTransform.HEARING_INITIATED;
import static uk.gov.moj.cpp.hearing.domain.transformation.judicialresult.domain.EventToTransform.NOWS_REQUESTED;
import static uk.gov.moj.cpp.hearing.domain.transformation.judicialresult.domain.EventToTransform.PENDING_NOWS_REQUESTED;
import static uk.gov.moj.cpp.hearing.domain.transformation.judicialresult.domain.EventToTransform.RESULTS_SHARED;
import static uk.gov.moj.cpp.hearing.domain.transformation.judicialresult.domain.EventToTransform.isEventToTransform;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(DataProviderRunner.class)
public class EventToTransformTest {

    @DataProvider
    public static Object[][] validEventToMatch() {
        return new Object[][]{
                {RESULTS_SHARED.getEventName()},
                {PENDING_NOWS_REQUESTED.getEventName()},
                {NOWS_REQUESTED.getEventName()},
                {HEARING_INITIATED.getEventName()},
                {HEARING_EXTENDED.getEventName()},
                {APPLICATION_DETAIL_CHANGED.getEventName()},
                {CASE_DEFENDANTS_UPDATED_FOR_HEARING.getEventName()},
                {CASE_DEFENDANTS_UPDATED.getEventName()}
        };
    }

    @Test
    @UseDataProvider("validEventToMatch")
    public void shouldReturnTrueIfEventNameIsAMatch(final String eventName) {
        assertThat(isEventToTransform(eventName), is(true));
    }

    @Test
    public void shouldReturnFalseIfEventNameIsNotAMatch() {
        assertThat(isEventToTransform(STRING.next()), is(false));
    }
}