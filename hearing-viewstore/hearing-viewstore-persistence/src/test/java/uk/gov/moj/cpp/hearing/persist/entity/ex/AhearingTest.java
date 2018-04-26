package uk.gov.moj.cpp.hearing.persist.entity.ex;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static uk.gov.moj.cpp.hearing.utils.TestUtils.hasInnerStaticClass;
import static uk.gov.moj.cpp.hearing.utils.TestUtils.hasParameterizedConstructor;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

public class AhearingTest {
    
    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void shouldHaveANoArgsConstructor() {
        assertThat(Ahearing.class, hasValidBeanConstructor());
    }

    @Test
    public void shouldHaveABuilderInnerClass() {
        assertTrue(hasInnerStaticClass(Ahearing.class, "Builder"));
    }
    
    @Test
    public void shouldHaveAParameterizedConstructor() {
        assertTrue(hasParameterizedConstructor(Ahearing.class, Ahearing.Builder.class));
    }

    public static Ahearing buildHearing1(final UUID hearingId, final ZonedDateTime startDateTime) {
        Ahearing ahearing = new Ahearing.Builder()
                .withId(hearingId)
                .withHearingType("TRIAL")
                .withStartDateTime(startDateTime)
                .withCourtCentreId(UUID.fromString("e8821a38-546d-4b56-9992-ebdd772a561f"))
                .withCourtCentreName("Liverpool Crown Court")
                .withRoomId(UUID.fromString("e7721a38-546d-4b56-9992-ebdd772a561b"))
                .withRoomName("3-1")
                .withHearingDays(Arrays.asList(AhearingDate.builder()
                                .withDate(startDateTime)
                                .withId(new HearingSnapshotKey(UUID.randomUUID(), hearingId))
                                .build()))
                .build();

        return ahearing;
    }
}