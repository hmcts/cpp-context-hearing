package uk.gov.moj.cpp.hearing.mapping;

import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCase;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class ProsecutionCaseJPAMapper {

    private ProsecutionCaseIdentifierJPAMapper prosecutionCaseIdentifierJPAMapper;
    private DefendantJPAMapper defendantJPAMapper;

    @Inject
    public ProsecutionCaseJPAMapper(final ProsecutionCaseIdentifierJPAMapper prosecutionCaseIdentifierJPAMapper,
                                    final DefendantJPAMapper defendantJPAMapper) {
        this.prosecutionCaseIdentifierJPAMapper = prosecutionCaseIdentifierJPAMapper;
        this.defendantJPAMapper = defendantJPAMapper;
    }

    //to keep cdi tester jhappy
    public ProsecutionCaseJPAMapper() {
    }

    ProsecutionCase toJPA(final Hearing hearing, final uk.gov.justice.core.courts.ProsecutionCase pojo) {
        if (null == pojo) {
            return null;
        }
        final ProsecutionCase prosecutionCase = new ProsecutionCase();
        prosecutionCase.setId(new HearingSnapshotKey(pojo.getId(), hearing.getId()));
        prosecutionCase.setProsecutionCaseIdentifier(prosecutionCaseIdentifierJPAMapper.toJPA(pojo.getProsecutionCaseIdentifier()));
        prosecutionCase.setOriginatingOrganisation(pojo.getOriginatingOrganisation());
        prosecutionCase.setInitiationCode(pojo.getInitiationCode());
        prosecutionCase.setCaseStatus(pojo.getCaseStatus());
        prosecutionCase.setStatementOfFacts(pojo.getStatementOfFacts());
        prosecutionCase.setStatementOfFactsWelsh(pojo.getStatementOfFactsWelsh());
        prosecutionCase.setDefendants(defendantJPAMapper.toJPA(hearing, prosecutionCase, pojo.getDefendants()));
        return prosecutionCase;
    }

    uk.gov.justice.core.courts.ProsecutionCase fromJPA(final ProsecutionCase entity) {
        if (null == entity) {
            return null;
        }
        return uk.gov.justice.core.courts.ProsecutionCase.prosecutionCase()
                .withId(entity.getId().getId())
                .withProsecutionCaseIdentifier(prosecutionCaseIdentifierJPAMapper.fromJPA(entity.getProsecutionCaseIdentifier()))
                .withOriginatingOrganisation(entity.getOriginatingOrganisation())
                .withInitiationCode(entity.getInitiationCode())
                .withCaseStatus(entity.getCaseStatus())
                .withStatementOfFacts(entity.getStatementOfFacts())
                .withStatementOfFactsWelsh(entity.getStatementOfFactsWelsh())
                .withDefendants(defendantJPAMapper.fromJPA(entity.getDefendants()))
                .build();
    }

    public Set<ProsecutionCase> toJPA(Hearing hearing, List<uk.gov.justice.core.courts.ProsecutionCase> pojos) {
        if (null == pojos) {
            return new HashSet<>();
        }
        return pojos.stream().map(pojo -> toJPA(hearing, pojo)).collect(Collectors.toSet());
    }

    public List<uk.gov.justice.core.courts.ProsecutionCase> fromJPA(Set<ProsecutionCase> entities) {
        if (null == entities) {
            return new ArrayList<>();
        }
        return entities.stream().map(this::fromJPA).collect(Collectors.toList());
    }

}