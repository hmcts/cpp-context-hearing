package uk.gov.moj.cpp.hearing.mapping;

import uk.gov.justice.core.courts.JudicialRoleType;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.persist.entity.ha.JudicialRole;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class JudicialRoleJPAMapper {

    JudicialRole toJPA(final Hearing hearing, uk.gov.justice.core.courts.JudicialRole pojo) {
        if (null == pojo) {
            return null;
        }
        final JudicialRole judicialRole = new JudicialRole();
        judicialRole.setId(new HearingSnapshotKey(UUID.randomUUID(), hearing.getId()));
        judicialRole.setTitle(pojo.getTitle());
        judicialRole.setFirstName(pojo.getFirstName());
        judicialRole.setMiddleName(pojo.getMiddleName());
        judicialRole.setLastName(pojo.getLastName());
        judicialRole.setJudicialId(pojo.getJudicialId());
        judicialRole.setJudicialRoleType(pojo.getJudicialRoleType().getJudiciaryType());
        judicialRole.setBenchChairman(pojo.getIsBenchChairman());
        judicialRole.setDeputy(pojo.getIsDeputy());
        judicialRole.setHearing(hearing);
        judicialRole.setUserId(pojo.getUserId());
        return judicialRole;
    }

    uk.gov.justice.core.courts.JudicialRole fromJPA(final JudicialRole entity) {
        if (null == entity) {
            return null;
        }
        return uk.gov.justice.core.courts.JudicialRole.judicialRole()
                .withTitle(entity.getTitle())
                .withFirstName(entity.getFirstName())
                .withMiddleName(entity.getMiddleName())
                .withLastName(entity.getLastName())
                .withJudicialId(entity.getJudicialId())
                .withJudicialRoleType(JudicialRoleType.judicialRoleType()
                        .withJudiciaryType(entity.getJudicialRoleType())
                        .build())
                .withIsBenchChairman(entity.getBenchChairman())
                .withIsDeputy(entity.getDeputy())
                .withUserId(entity.getUserId())
                .build();
    }

    public Set<JudicialRole> toJPA(Hearing hearing, List<uk.gov.justice.core.courts.JudicialRole> pojos) {
        if (null == pojos) {
            return new HashSet<>();
        }
        return pojos.stream().map(pojo -> toJPA(hearing, pojo)).collect(Collectors.toSet());
    }

    public List<uk.gov.justice.core.courts.JudicialRole> fromJPA(Set<JudicialRole> entities) {
        if (null == entities) {
            return new ArrayList<>();
        }
        return entities.stream().map(this::fromJPA).collect(Collectors.toList());
    }
}
