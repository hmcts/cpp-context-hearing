package uk.gov.moj.cpp.hearing.persist;


import uk.gov.moj.cpp.hearing.persist.entity.HearingEvent;

import java.util.UUID;

import org.apache.deltaspike.data.api.EntityRepository;
import org.apache.deltaspike.data.api.Repository;

@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
@Repository
public interface HearingEventRepository extends EntityRepository<HearingEvent, UUID> {

    HearingEvent findById(final UUID hearingEventId);
}
