package uk.gov.moj.cpp.hearing.domain.aggregate.hearing;

import uk.gov.justice.core.courts.CreateNowsRequest;
import uk.gov.justice.core.courts.Target;
import uk.gov.moj.cpp.hearing.nows.events.EnforcementError;
import uk.gov.moj.cpp.hearing.nows.events.NowsRequested;
import uk.gov.moj.cpp.hearing.nows.events.PendingNowsRequested;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

public class NowDelegate implements Serializable {

    private static final long serialVersionUID = 1L;

    private final HearingAggregateMomento momento;

    public NowDelegate(final HearingAggregateMomento momento) {
        this.momento = momento;
    }

    public Stream<Object> registerPendingNowsRequest(final CreateNowsRequest nowsRequest, final List<Target> targets) {
        return Stream.of(new PendingNowsRequested(nowsRequest, targets));
    }

    public void handlePendingNowsRequested(final PendingNowsRequested pendingNowsRequested) {
        momento.getHearingNowsMapper().add(pendingNowsRequested);
    }

    public Stream<Object> applyAccountNumber(final UUID requestId, final String accountNumber) {
        final PendingNowsRequested pendingNowsRequested = momento.getHearingNowsMapper().stream()
                .filter(pendingNowsRequested1 -> pendingNowsRequested1.getCreateNowsRequest().getNows().stream().anyMatch(now -> now.getId().equals(requestId)))
                .findFirst()
                .orElse(null);

        if (pendingNowsRequested == null) {
            throw new IllegalStateException();
        }

        return Stream.of(new NowsRequested(requestId, pendingNowsRequested.getCreateNowsRequest(), accountNumber, pendingNowsRequested.getTargets()));
    }

    public Stream<Object> recordEnforcementError(final UUID requestId, final String errorCode, final String errorMessage) {
        return Stream.of(new EnforcementError(requestId, errorCode, errorMessage));
    }
}
