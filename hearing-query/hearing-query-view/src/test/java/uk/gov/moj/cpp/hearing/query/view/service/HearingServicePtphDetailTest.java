package uk.gov.moj.cpp.hearing.query.view.service;

import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import uk.gov.moj.cpp.hearing.persist.entity.ha.PtphDetail;
import uk.gov.moj.cpp.hearing.query.view.response.PtphDetailResponse;
import uk.gov.moj.cpp.hearing.repository.PtphDetailRepository;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class HearingServicePtphDetailTest {

    @Mock
    private PtphDetailRepository ptphDetailRepository;

    @InjectMocks
    private HearingService hearingService;

    @Test
    void returnsSavedRow() {
        final UUID hearingId = randomUUID();
        final PtphDetail entity = new PtphDetail();
        entity.setHearingId(hearingId);
        entity.setTier("TIER_2");
        entity.setListType("TYPE_1_FIXED");
        entity.setKeyReason("reason");
        entity.setFinalised(true);
        when(ptphDetailRepository.findBy(hearingId)).thenReturn(entity);

        final PtphDetailResponse response = hearingService.getPtphDetail(hearingId);

        assertThat(response.getTier(), is("TIER_2"));
        assertThat(response.getListType(), is("TYPE_1_FIXED"));
        assertThat(response.getKeyReason(), is("reason"));
        assertThat(response.isFinalised(), is(true));
    }

    @Test
    void returnsEmptyWhenAbsent() {
        final UUID hearingId = randomUUID();
        when(ptphDetailRepository.findBy(hearingId)).thenReturn(null);

        final PtphDetailResponse response = hearingService.getPtphDetail(hearingId);

        assertThat(response.getTier(), is((Object) null));
        assertThat(response.isFinalised(), is(false));
    }
}
