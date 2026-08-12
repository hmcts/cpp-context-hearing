package uk.gov.moj.cpp.hearing.command;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

/**
 * The two codes are the business identifiers shown to users ("1F" / "2F"), and the constant
 * names travel on the wire as the {@code listType} string in every PTPH event and payload.
 * Both are pinned here so neither can be changed silently.
 */
class ListTypeTest {

    @Test
    void shouldExposeTheFixedDateCode() {
        assertThat(ListType.TYPE_1_FIXED.getCode(), is("1F"));
    }

    @Test
    void shouldExposeTheFlexibleCode() {
        assertThat(ListType.TYPE_2_FLEXIBLE.getCode(), is("2F"));
    }

    @Test
    void shouldOfferExactlyTheTwoListTypes() {
        assertThat(ListType.values().length, is(2));
    }

    @Test
    void shouldResolveFromTheNameCarriedOnTheWire() {
        assertThat(ListType.valueOf("TYPE_1_FIXED"), is(ListType.TYPE_1_FIXED));
        assertThat(ListType.valueOf("TYPE_2_FLEXIBLE"), is(ListType.TYPE_2_FLEXIBLE));
    }
}
