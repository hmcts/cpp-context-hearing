package uk.gov.moj.cpp.hearing.persist.entity;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;

public class HearingEventDefinitionTest {

    @Test
    public void shouldHaveANoArgsConstructor() {
        assertThat(HearingEventDefinition.class, hasValidBeanConstructor());
    }

}