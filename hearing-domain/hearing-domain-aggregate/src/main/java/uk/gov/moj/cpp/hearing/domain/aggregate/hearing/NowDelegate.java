package uk.gov.moj.cpp.hearing.domain.aggregate.hearing;

import uk.gov.justice.core.courts.CreateNowsRequest;
import uk.gov.justice.core.courts.Target;
import uk.gov.moj.cpp.hearing.nows.events.PendingNowsRequested;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Stream;

/**
 * @deprecated - This class has been retain to support existing events in the event log.
 *
 */
@Deprecated
@SuppressWarnings({"squid:S1068", "squid:S1186", "squid:CallToDeprecatedMethod", "pmd:BeanMembersShouldSerialize"})
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
    }
}
