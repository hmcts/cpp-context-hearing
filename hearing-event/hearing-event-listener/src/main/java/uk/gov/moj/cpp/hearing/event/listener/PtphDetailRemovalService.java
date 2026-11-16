package uk.gov.moj.cpp.hearing.event.listener;

import uk.gov.moj.cpp.hearing.persist.entity.ha.PtphDetail;
import uk.gov.moj.cpp.hearing.repository.PtphDetailRepository;

import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Removes a hearing's tier and list type from the view store.
 *
 * <p>LPT-2400–2404: {@code ha_ptph_detail} is keyed by hearing id but has no foreign key to the
 * hearing table, so nothing removes it when the hearing row goes. Left behind, the row is orphaned
 * and {@code hearing.get-ptph-detail} keeps answering for a hearing that no longer exists — which
 * the listing context would then inherit onto a next hearing.
 *
 * <p>Shared rather than duplicated because a hearing is removed from the view store in several
 * listeners — deleted, deleted-bdf, court-application-deleted, marked-as-duplicate and
 * unallocated. Call this immediately after removing the hearing row, never on its own.
 */
// Field injection (java:S6813) to match every other CDI bean and listener in this context;
// constructor injection here would also need a second no-arg constructor to keep this normal-scoped
// bean proxyable.
@SuppressWarnings("java:S6813")
@ApplicationScoped
public class PtphDetailRemovalService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PtphDetailRemovalService.class);

    @Inject
    private PtphDetailRepository ptphDetailRepository;

    public void removeFor(final UUID hearingId) {
        final PtphDetail ptphDetail = ptphDetailRepository.findBy(hearingId);
        if (ptphDetail != null) {
            LOGGER.info("Removing ptph detail for removed hearing {}", hearingId);
            ptphDetailRepository.removeAndFlush(ptphDetail);
        }
    }
}
