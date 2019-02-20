package uk.gov.moj.cpp.hearing.domain.aggregate.hearing;

import uk.gov.justice.core.courts.CreateNowsRequest;
import uk.gov.moj.cpp.hearing.nows.events.EnforcementError;
import uk.gov.moj.cpp.hearing.nows.events.NowsRequested;
import uk.gov.moj.cpp.hearing.nows.events.PendingNowsRequested;

import java.io.Serializable;
import java.util.UUID;
import java.util.stream.Stream;

public class NowDelegate implements Serializable {

    private static final long serialVersionUID = 1L;

    private final HearingAggregateMomento momento;

    public NowDelegate(final HearingAggregateMomento momento) {
        this.momento = momento;
    }

    public Stream<Object> registerPendingNowsRequest(final CreateNowsRequest nowsRequest) {
        return Stream.of(new PendingNowsRequested(nowsRequest));
    }

    public void handlePendingNowsRequested(final PendingNowsRequested pendingNowsRequested) {
        final UUID hearingId = pendingNowsRequested.getCreateNowsRequest().getHearing().getId();
        momento.getHearingNowsMapper().putIfAbsent(hearingId, pendingNowsRequested.getCreateNowsRequest());
    }

    public Stream<Object> applyAccountNumber(final UUID hearingId, final UUID requestId, final String accountNumber) {
        final CreateNowsRequest nowsRequest = momento.getHearingNowsMapper().get(hearingId);

        if (nowsRequest == null) {
            throw new IllegalStateException();
        }

        return Stream.of(new NowsRequested(requestId, nowsRequest, accountNumber));
    }

    public Stream<Object> recordEnforcementError(final UUID requestId, final String errorCode, final String errorMessage) {
        return Stream.of(new EnforcementError(requestId, errorCode, errorMessage));
    }
}
