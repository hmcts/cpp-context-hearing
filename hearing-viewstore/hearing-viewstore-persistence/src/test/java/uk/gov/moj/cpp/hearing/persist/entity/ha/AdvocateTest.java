package uk.gov.moj.cpp.hearing.persist.entity.ha;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static uk.gov.moj.cpp.hearing.utils.TestUtils.hasInnerStaticClass;
import static uk.gov.moj.cpp.hearing.utils.TestUtils.hasParameterizedConstructor;

import org.junit.Before;
import org.junit.Test;

public class AdvocateTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void shouldHaveANoArgsConstructor() {
        assertThat(Advocate.class, hasValidBeanConstructor());
    }

    @Test
    public void shouldHaveABuilderInnerClass() {
        assertTrue(hasInnerStaticClass(Advocate.class, "AdvocateBuilder"));
    }
    
    @Test
    public void shouldHaveAParameterizedConstructor() {
        assertTrue(hasParameterizedConstructor(Advocate.class, Advocate.AdvocateBuilder.class));
    }
    
}
