package uk.gov.moj.cpp.hearing.persist.entity.ha;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static uk.gov.moj.cpp.hearing.utils.TestUtils.hasInnerStaticClass;
import static uk.gov.moj.cpp.hearing.utils.TestUtils.hasParameterizedConstructor;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

public class ProsecutionAdvocateTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void shouldHaveANoArgsConstructor() {
        assertThat(ProsecutionAdvocate.class, hasValidBeanConstructor());
    }

    @Test
    public void shouldHaveABuilderInnerClass() {
        assertTrue(hasInnerStaticClass(ProsecutionAdvocate.class, "ProsecutionAdvocateAdvocateBuilder"));
    }
    
    @Test
    public void shouldHaveAParameterizedConstructor() {
        assertTrue(hasParameterizedConstructor(ProsecutionAdvocate.class, ProsecutionAdvocate.ProsecutionAdvocateAdvocateBuilder.class));
    }
    
    public static ProsecutionAdvocate buildProsecutionAdvocate(final Hearing hearing) {
        return ProsecutionAdvocate.builder()
                .withId(new HearingSnapshotKey(UUID.fromString("841164f6-13bc-46ff-8634-63cf9ae85d36"), hearing.getId()))
                .withPersonId(UUID.fromString("35f4d841-a0eb-4a32-b75c-91d241bf83d3"))
                .withFirstName("Brian J.")
                .withLastName("Fox")
                .withTitle("MR")
                .withStatus("QC")
                .build();
    }
}
