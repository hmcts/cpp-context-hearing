package uk.gov.moj.cpp.hearing.mapping;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

public class DefendantAliasesJPAMapperTest {

    private DefendantAliasesJPAMapper defendantAliasesJPAMapper = new DefendantAliasesJPAMapper();

    @Test
    public void testFromJPA() {
        ;
        assertThat(defendantAliasesJPAMapper.fromJPA("A, B, C"), is(Arrays.asList("A", "B", "C")));
    }

    @Test
    public void testToJPA() {
        assertThat(defendantAliasesJPAMapper.toJPA(Arrays.asList("A", "B", "C")), is("A, B, C"));
    }
}