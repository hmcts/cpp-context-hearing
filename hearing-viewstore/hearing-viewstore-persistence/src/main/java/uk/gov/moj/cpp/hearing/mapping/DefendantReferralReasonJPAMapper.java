package uk.gov.moj.cpp.hearing.mapping;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;

import uk.gov.justice.json.schemas.core.ReferralReason;
import uk.gov.moj.cpp.hearing.persist.entity.ha.DefendantReferralReason;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;

@ApplicationScoped
public class DefendantReferralReasonJPAMapper {

    DefendantReferralReason toJPA(final Hearing hearing, final ReferralReason pojo) {
        if (null == pojo) {
            return null;
        }
        final DefendantReferralReason defendantReferralReason = new DefendantReferralReason();
        defendantReferralReason.setDefendantId(pojo.getDefendantId());
        defendantReferralReason.setDescription(pojo.getDescription());
        defendantReferralReason.setHearing(hearing);
        defendantReferralReason.setId(new HearingSnapshotKey(pojo.getId(), hearing.getId()));
        return defendantReferralReason;
    }

    ReferralReason fromJPA(final DefendantReferralReason entity) {
        if (null == entity) {
            return null;
        }
        return uk.gov.justice.json.schemas.core.ReferralReason.referralReason()
                .withDefendantId(entity.getDefendantId())
                .withDescription(entity.getDescription())
                .withId(entity.getId().getId())
                .build();
    }

    public List<DefendantReferralReason> toJPA(Hearing hearing, List<ReferralReason> pojos) {
        if (null == pojos) {
            return new ArrayList<>();
        }
        return pojos.stream().map(pojo -> toJPA(hearing, pojo)).collect(Collectors.toList());
    }

    public List<ReferralReason> fromJPA(List<DefendantReferralReason> entities) {
        if (null == entities) {
            return new ArrayList<>();
        }
        return entities.stream().map(this::fromJPA).collect(Collectors.toList());
    }
}