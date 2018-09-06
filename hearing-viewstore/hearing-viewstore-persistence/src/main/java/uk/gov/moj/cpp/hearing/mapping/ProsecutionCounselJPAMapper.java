package uk.gov.moj.cpp.hearing.mapping;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;

import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCounsel;

@ApplicationScoped // Will be covered by GGPE-5825 story
public class ProsecutionCounselJPAMapper {

    private ProsecutionCounsel toJPA(final Hearing hearing, final uk.gov.justice.json.schemas.core.ProsecutionCounsel pojo) {
        if (null == pojo) {
            return null;
        }
        final ProsecutionCounsel prosecutionCounsel = new ProsecutionCounsel();
        prosecutionCounsel.setHearingId(hearing.getId());
        return prosecutionCounsel;
    }

    private uk.gov.justice.json.schemas.core.ProsecutionCounsel fromJPA(final ProsecutionCounsel entity) {
        if (null == entity) {
            return null;
        }
        return uk.gov.justice.json.schemas.core.ProsecutionCounsel.prosecutionCounsel()
                .build();
    }

    public List<ProsecutionCounsel> toJPA(Hearing hearing,
            List<uk.gov.justice.json.schemas.core.ProsecutionCounsel> pojos) {
        if (null == pojos) {
            return new ArrayList<>();
        }
        return pojos.stream().map(pojo -> toJPA(hearing, pojo)).collect(Collectors.toList());
    }

    public List<uk.gov.justice.json.schemas.core.ProsecutionCounsel> fromJPA(
            List<ProsecutionCounsel> entities) {
        if (null == entities) {
            return new ArrayList<>();
        }
        return entities.stream().map(this::fromJPA).collect(Collectors.toList());
    }
}