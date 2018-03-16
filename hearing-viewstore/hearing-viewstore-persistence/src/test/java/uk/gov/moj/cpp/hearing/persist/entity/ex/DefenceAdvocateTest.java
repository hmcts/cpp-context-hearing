package uk.gov.moj.cpp.hearing.persist.entity.ex;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static uk.gov.moj.cpp.hearing.utils.TestUtils.hasInnerStaticClass;
import static uk.gov.moj.cpp.hearing.utils.TestUtils.hasParameterizedConstructor;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

public class DefenceAdvocateTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void shouldHaveANoArgsConstructor() {
        assertThat(DefenceAdvocate.class, hasValidBeanConstructor());
    }

    @Test
    public void shouldHaveABuilderInnerClass() {
        assertTrue(hasInnerStaticClass(DefenceAdvocate.class, "Builder"));
    }
    
    @Test
    public void shouldHaveAParameterizedConstructor() {
        assertTrue(hasParameterizedConstructor(DefenceAdvocate.class, DefenceAdvocate.Builder.class));
    }

    public static DefenceAdvocate buildDefenseAdvocate1(final Ahearing ahearing) {
        return new DefenceAdvocate.Builder()
                .withId(new HearingSnapshotKey(UUID.fromString("743d333a-b270-4de6-a598-61abb64a8027"), ahearing.getId()))
                .withPersonId(UUID.fromString("effdc5c9-8c00-4ef8-abcf-9e2a79ed1daa"))
                .withFirstName("Mark")
                .withLastName("Zuckerberg")
                .withTitle("MR")
                .withStatus("QC")
                .build();
    }
    
    public static DefenceAdvocate buildDefenseAdvocate2(final Ahearing ahearing) {
        return new DefenceAdvocate.Builder()
                .withId(new HearingSnapshotKey(UUID.fromString("cdc14b89-6b4d-4e98-9641-826c355c51b8"), ahearing.getId()))
                .withPersonId(UUID.fromString("78651aea-02da-4be9-8e78-c1748ea89e0c"))
                .withFirstName("Sean")
                .withLastName("Parker")
                .withTitle("MR")
                .build();
    }

}