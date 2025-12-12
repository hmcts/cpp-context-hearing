package uk.gov.moj.cpp.hearing.repository;

import org.apache.deltaspike.data.api.EntityRepository;
import org.apache.deltaspike.data.api.Repository;
import uk.gov.moj.cpp.hearing.persist.entity.sessiontime.SessionTime;

import java.util.UUID;

@Repository
public interface SessionTimeRepository extends EntityRepository<SessionTime, UUID> {

}
