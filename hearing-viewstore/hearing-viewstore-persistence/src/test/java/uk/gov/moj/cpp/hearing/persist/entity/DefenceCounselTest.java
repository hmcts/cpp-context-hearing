package uk.gov.moj.cpp.hearing.persist.entity;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;

public class DefenceCounselTest {

    @Test
    public void shouldHaveANoArgsConstructor() {
        assertThat(DefenceCounsel.class, hasValidBeanConstructor());
    }

}