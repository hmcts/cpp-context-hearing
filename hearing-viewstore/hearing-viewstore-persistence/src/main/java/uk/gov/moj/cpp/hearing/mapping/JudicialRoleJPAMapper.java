package uk.gov.moj.cpp.hearing.mapping;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;

import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.persist.entity.ha.JudicialRole;

@ApplicationScoped
public class JudicialRoleJPAMapper {

    JudicialRole toJPA(final Hearing hearing, uk.gov.justice.json.schemas.core.JudicialRole pojo) {
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
        judicialRole.setJudicialRoleType(pojo.getJudicialRoleType());
        judicialRole.setBenchChairman(pojo.getIsBenchChairman());
        judicialRole.setDeputy(pojo.getIsDeputy());
        judicialRole.setHearing(hearing);
        return judicialRole;
    }

    uk.gov.justice.json.schemas.core.JudicialRole fromJPA(final JudicialRole entity) {
        if (null == entity) {
            return null;
        }
        return uk.gov.justice.json.schemas.core.JudicialRole.judicialRole()
                .withTitle(entity.getTitle())
                .withFirstName(entity.getFirstName())
                .withMiddleName(entity.getMiddleName())
                .withLastName(entity.getLastName())
                .withJudicialId(entity.getJudicialId())
                .withJudicialRoleType(entity.getJudicialRoleType())
                .withIsBenchChairman(entity.getBenchChairman())
                .withIsDeputy(entity.getDeputy())
                .build();
    }

    public List<JudicialRole> toJPA(Hearing hearing, List<uk.gov.justice.json.schemas.core.JudicialRole> pojos) {
        if (null == pojos) {
            return new ArrayList<>();
        }
        return pojos.stream().map(pojo -> toJPA(hearing, pojo)).collect(Collectors.toList());
    }

    public List<uk.gov.justice.json.schemas.core.JudicialRole> fromJPA(List<JudicialRole> entities) {
        if (null == entities) {
            return new ArrayList<>();
        }
        return entities.stream().map(this::fromJPA).collect(Collectors.toList());
    }
}
