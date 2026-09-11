package uk.gov.moj.cpp.hearing.persist.entity.ha;

import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;

class PtphDetailTest {

    @Test
    void holdsFields() {
        final UUID hearingId = randomUUID();
        final PtphDetail entity = new PtphDetail();
        entity.setHearingId(hearingId);
        entity.setTier("TIER_2");
        entity.setListType("TYPE_1_FIXED");
        entity.setKeyReason("reason");
        entity.setFinalised(true);

        assertThat(entity.getHearingId(), is(hearingId));
        assertThat(entity.getTier(), is("TIER_2"));
        assertThat(entity.getListType(), is("TYPE_1_FIXED"));
        assertThat(entity.getKeyReason(), is("reason"));
        assertThat(entity.isFinalised(), is(true));
    }
}
