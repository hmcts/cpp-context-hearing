package uk.gov.moj.cpp.hearing.repository;

import uk.gov.moj.cpp.hearing.persist.entity.ha.DraftResult;

import java.util.List;
import java.util.UUID;

import org.apache.deltaspike.data.api.EntityRepository;
import org.apache.deltaspike.data.api.Query;
import org.apache.deltaspike.data.api.QueryParam;
import org.apache.deltaspike.data.api.Repository;

@Repository
public interface DraftResultRepository extends EntityRepository<DraftResult, String> {


    @Query(value = "SELECT result FROM DraftResult result " +
            "WHERE result.hearingId = :hearingId " +
            "AND (result.hearingDay = :hearingDay OR result.hearingDay IS NULL)")
    List<DraftResult> findDraftResultByFilter(@QueryParam("hearingId") final UUID hearingId,
                                              @QueryParam("hearingDay") final String hearingDay);


}
