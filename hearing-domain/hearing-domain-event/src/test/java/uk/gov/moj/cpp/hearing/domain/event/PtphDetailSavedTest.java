package uk.gov.moj.cpp.hearing.domain.event;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static java.util.UUID.randomUUID;

import java.util.UUID;

import org.junit.jupiter.api.Test;

class PtphDetailSavedTest {

    @Test
    void shouldHoldSavedFields() {
        final UUID hearingId = randomUUID();
        final PtphDetailSaved event = new PtphDetailSaved(hearingId, "TIER_2", "TYPE_1_FIXED", "fixed reason");

        assertThat(event.getHearingId(), is(hearingId));
        assertThat(event.getTier(), is("TIER_2"));
        assertThat(event.getListType(), is("TYPE_1_FIXED"));
        assertThat(event.getKeyReason(), is("fixed reason"));
    }
}
