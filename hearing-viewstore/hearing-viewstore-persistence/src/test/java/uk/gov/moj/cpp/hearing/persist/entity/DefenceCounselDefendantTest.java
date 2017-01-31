package uk.gov.moj.cpp.hearing.persist.entity;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;

public class DefenceCounselDefendantTest {

    @Test
    public void shouldHaveANoArgsConstructor() {
        assertThat(DefenceCounselDefendant.class, hasValidBeanConstructor());
    }

}