package uk.gov.moj.cpp.hearing.command;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

/**
 * Tier names travel on the wire as the {@code tier} string in every PTPH event and payload, and
 * the listing context stores them verbatim, so the set and spelling are a cross-context contract.
 */
class TierTest {

    @Test
    void shouldOfferSevenTiers() {
        assertThat(Tier.values().length, is(7));
    }

    @Test
    void shouldResolveEachTierFromTheNameCarriedOnTheWire() {
        for (int tier = 1; tier <= 7; tier++) {
            final String name = "TIER_" + tier;
            assertThat(Tier.valueOf(name).name(), is(name));
        }
    }
}
