package uk.gov.moj.cpp.hearing.persist.entity.ex;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static uk.gov.moj.cpp.hearing.utils.TestUtils.hasInnerStaticClass;
import static uk.gov.moj.cpp.hearing.utils.TestUtils.hasParameterizedConstructor;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

public class JudgeTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void shouldHaveANoArgsConstructor() {
        assertThat(Judge.class, hasValidBeanConstructor());
    }

    @Test
    public void shouldHaveABuilderInnerClass() {
        assertTrue(hasInnerStaticClass(Judge.class, "Builder"));
    }
    
    @Test
    public void shouldHaveAParameterizedConstructor() {
        assertTrue(hasParameterizedConstructor(Judge.class, Judge.Builder.class));
    }

    public static Judge buildJudge(final Ahearing ahearing) {
        return new Judge.Builder()
                .withId(new HearingSnapshotKey(UUID.fromString("a38d0d5f-a26c-436b-9b5e-4dc58f28878d"), ahearing.getId()))
                .withPersonId(UUID.fromString("c8912678-213f-421a-89c8-d0dc87ac3558"))
                .withFirstName("Alan")
                .withLastName("Mathison Turing")
                .withTitle("HHJ")
                .build();
    }
}
