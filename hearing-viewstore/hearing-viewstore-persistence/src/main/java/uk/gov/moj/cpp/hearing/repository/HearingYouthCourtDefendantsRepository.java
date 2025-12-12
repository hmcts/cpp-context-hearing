package uk.gov.moj.cpp.hearing.repository;

import org.apache.deltaspike.data.api.AbstractEntityRepository;
import org.apache.deltaspike.data.api.Query;
import org.apache.deltaspike.data.api.QueryParam;
import org.apache.deltaspike.data.api.Repository;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingYouthCourDefendantsKey;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingYouthCourtDefendants;

import java.util.List;
import java.util.UUID;


@Repository(forEntity = HearingYouthCourtDefendants.class)
public abstract class HearingYouthCourtDefendantsRepository extends AbstractEntityRepository<HearingYouthCourtDefendants, HearingYouthCourDefendantsKey> {

    @Query(value = "from HearingYouthCourtDefendants h where h.id.hearingId = :hearingId ")
    public abstract List<HearingYouthCourtDefendants> findAllByHearingId(@QueryParam("hearingId") final UUID hearingId);

}