package uk.gov.moj.cpp.hearing.repository;

import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingApplication;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingApplicationKey;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Target;

import java.util.List;
import java.util.UUID;

import org.apache.deltaspike.data.api.AbstractEntityRepository;
import org.apache.deltaspike.data.api.Query;
import org.apache.deltaspike.data.api.QueryParam;
import org.apache.deltaspike.data.api.Repository;


@Repository(forEntity = HearingApplication.class)
public abstract class HearingApplicationRepository extends AbstractEntityRepository<HearingApplication, HearingApplicationKey> {

    @Query(value = "from HearingApplication ha where ha.id.applicationId = :applicationId")
    public abstract List<HearingApplication> findByApplicationId(@QueryParam("applicationId") final UUID applicationId);

}