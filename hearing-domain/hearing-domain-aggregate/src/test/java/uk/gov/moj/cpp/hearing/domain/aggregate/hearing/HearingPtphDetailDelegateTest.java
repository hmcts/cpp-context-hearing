package uk.gov.moj.cpp.hearing.domain.aggregate.hearing;

import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import uk.gov.moj.cpp.hearing.domain.event.PtphDetailDeleted;
import uk.gov.moj.cpp.hearing.domain.event.PtphDetailFinalised;
import uk.gov.moj.cpp.hearing.domain.event.PtphDetailSaved;

import java.util.UUID;

import org.junit.jupiter.api.Test;

class HearingPtphDetailDelegateTest {

    private final HearingAggregateMomento momento = new HearingAggregateMomento();
    private final HearingPtphDetailDelegate delegate = new HearingPtphDetailDelegate(momento);

    @Test
    void saveStoresStateOnMomento() {
        final UUID hearingId = randomUUID();
        delegate.savePtphDetail(new PtphDetailSaved(hearingId, "TIER_2", "TYPE_1_FIXED", "reason"))
                .forEach(e -> delegate.handlePtphDetailSaved((PtphDetailSaved) e));

        assertThat(momento.getTier(), is("TIER_2"));
        assertThat(momento.getListType(), is("TYPE_1_FIXED"));
        assertThat(momento.getPtphDetailKeyReason(), is("reason"));
    }

    /**
     * The state rules that used to throw from here now live in {@code HearingAggregate}, which
     * emits {@code HearingChangeIgnored} instead - a throw would dead-letter the hearing's command
     * queue. What is pinned here is that the delegate itself never throws for any state, so the
     * rules cannot creep back down into it.
     */
    @Test
    void neverThrowsWhateverStateTheMomentoIsIn() {
        momento.setTier("TIER_2");
        momento.setListType("TYPE_2_FLEXIBLE");
        momento.setPtphDetailFinalised(true);

        assertDoesNotThrow(() -> delegate.savePtphDetail(new PtphDetailSaved(randomUUID(), "TIER_3", null, null)).count());
        assertDoesNotThrow(() -> delegate.finalisePtphDetail(new PtphDetailFinalised(randomUUID())).count());
        assertDoesNotThrow(() -> delegate.deletePtphDetail(new PtphDetailDeleted(randomUUID())).count());

        momento.setTier(null);
        momento.setListType(null);
        momento.setPtphDetailFinalised(false);

        assertDoesNotThrow(() -> delegate.savePtphDetail(new PtphDetailSaved(randomUUID(), "TIER_3", "TYPE_1_FIXED", null)).count());
        assertDoesNotThrow(() -> delegate.finalisePtphDetail(new PtphDetailFinalised(randomUUID())).count());
        assertDoesNotThrow(() -> delegate.deletePtphDetail(new PtphDetailDeleted(randomUUID())).count());
    }

    @Test
    void finaliseSetsFlagWhenBothPresent() {
        momento.setTier("TIER_2");
        momento.setListType("TYPE_2_FLEXIBLE");

        delegate.finalisePtphDetail(new PtphDetailFinalised(randomUUID()))
                .forEach(e -> delegate.handlePtphDetailFinalised((PtphDetailFinalised) e));

        assertThat(momento.isPtphDetailFinalised(), is(true));
    }

    @Test
    void deleteClearsAllState() {
        momento.setTier("TIER_2");
        momento.setListType("TYPE_1_FIXED");
        momento.setPtphDetailKeyReason("reason");
        momento.setPtphDetailFinalised(true);

        delegate.deletePtphDetail(new PtphDetailDeleted(randomUUID()))
                .forEach(e -> delegate.handlePtphDetailDeleted((PtphDetailDeleted) e));

        assertThat(momento.getTier(), is((Object) null));
        assertThat(momento.getListType(), is((Object) null));
        assertThat(momento.getPtphDetailKeyReason(), is((Object) null));
        assertThat(momento.isPtphDetailFinalised(), is(false));
    }
}
