package uk.gov.moj.cpp.hearing.persist.entity.ha;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static java.time.ZonedDateTime.parse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static uk.gov.moj.cpp.hearing.utils.TestUtils.hasInnerStaticClass;
import static uk.gov.moj.cpp.hearing.utils.TestUtils.hasParameterizedConstructor;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

public class OffenceTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void shouldHaveANoArgsConstructor() {
        assertThat(Offence.class, hasValidBeanConstructor());
    }

    @Test
    public void shouldHaveABuilderInnerClass() {
        assertTrue(hasInnerStaticClass(Offence.class, "Builder"));
    }
    
    @Test
    public void shouldHaveAParameterizedConstructor() {
        assertTrue(hasParameterizedConstructor(Offence.class, Offence.Builder.class));
    }

    public static Offence buildOffence1(final Hearing hearing, final Defendant defendant,
                                        final LegalCase legalCase) {
        return Offence.builder()
                .withId(new HearingSnapshotKey(UUID.fromString("4b1318e4-1517-4e4f-a89d-6af0eafa5058"), hearing.getId()))
                .withDefendant(defendant)
                .withCase(legalCase)
                .withCode("UNKNOWN")
                .withCount(1)
                .withWording("on 01/08/2009 at the County public house, unlawfully and maliciously wounded, John Smith")
                .withTitle("Wound / inflict grievous bodily harm without intent")
                .withLegislation("Contrary to section 20 of the Offences Against the Person Act 1861.")
                .withStartDate(parse("2018-02-21T00:00:00Z").toLocalDate())
                .withEndDate(parse("2018-02-22T00:00:00Z").toLocalDate())
                .withConvictionDate(parse("2018-02-22T00:00:00Z").toLocalDate())
                .withPleaDate(parse("2016-06-08T00:00:00Z").toLocalDate())
                .withPleaValue("GUILTY")
                .withVerdictCode("A1")
                .withVerdictCategory("GUILTY")
                .withVerdictDescription("Guilty By Jury On Judges Direction")
                .withVerdictDate(parse("2018-02-21T00:00:00Z").toLocalDate())
                .withNumberOfJurors(10)
                .withNumberOfSplitJurors(2)
                .withUnanimous(false)
                .build();
    }
}
