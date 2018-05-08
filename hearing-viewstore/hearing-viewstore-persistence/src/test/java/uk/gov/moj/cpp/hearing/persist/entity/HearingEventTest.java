package uk.gov.moj.cpp.hearing.persist.entity;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static java.util.UUID.randomUUID;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.BOOLEAN;
import static org.hamcrest.beans.SamePropertyValuesAs.samePropertyValuesAs;
import static org.hamcrest.core.IsSame.sameInstance;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_ZONED_DATE_TIME;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;


import java.time.ZonedDateTime;
import java.util.UUID;

import org.junit.Test;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingEvent;

public class HearingEventTest {

    @Test
    public void shouldHaveANoArgsConstructor() {
        assertThat(HearingEvent.class, hasValidBeanConstructor());
    }
}