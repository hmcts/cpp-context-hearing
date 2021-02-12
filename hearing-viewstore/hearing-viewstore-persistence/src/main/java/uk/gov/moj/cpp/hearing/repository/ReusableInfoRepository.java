package uk.gov.moj.cpp.hearing.repository;

import uk.gov.moj.cpp.hearing.persist.entity.ha.ReusableInfo;

import java.util.List;
import java.util.UUID;

import org.apache.deltaspike.data.api.AbstractEntityRepository;
import org.apache.deltaspike.data.api.Query;
import org.apache.deltaspike.data.api.QueryParam;
import org.apache.deltaspike.data.api.Repository;


@Repository
public abstract class ReusableInfoRepository extends AbstractEntityRepository<ReusableInfo, UUID> {

    @Query(value = "SELECT reusableinfo FROM ReusableInfo reusableInfo " +
            "WHERE reusableinfo.masterDefendantId IN (:masterDefendantIdList) ")
    public abstract List<ReusableInfo> findReusableInfoByMasterDefendantIds(@QueryParam("masterDefendantIdList") List<UUID> masterDefendantIdList);


}