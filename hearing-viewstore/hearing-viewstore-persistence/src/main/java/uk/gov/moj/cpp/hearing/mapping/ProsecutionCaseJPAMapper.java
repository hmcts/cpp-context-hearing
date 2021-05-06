package uk.gov.moj.cpp.hearing.mapping;

import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCase;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class ProsecutionCaseJPAMapper {

    private static final String CASE_STATUS_EJECTED = "EJECTED";
    private ProsecutionCaseIdentifierJPAMapper prosecutionCaseIdentifierJPAMapper;
    private CpsProsecutorJPAMapper cpsProsecutorJPAMapper;
    private DefendantJPAMapper defendantJPAMapper;
    private CaseMarkerJPAMapper caseMarkerJPAMapper;

    @Inject
    public ProsecutionCaseJPAMapper(final ProsecutionCaseIdentifierJPAMapper prosecutionCaseIdentifierJPAMapper,
                                    final DefendantJPAMapper defendantJPAMapper,
                                    final CaseMarkerJPAMapper caseMarkerJPAMapper,
                                    final CpsProsecutorJPAMapper cpsProsecutorJPAMapper) {
        this.prosecutionCaseIdentifierJPAMapper = prosecutionCaseIdentifierJPAMapper;
        this.defendantJPAMapper = defendantJPAMapper;
        this.caseMarkerJPAMapper = caseMarkerJPAMapper;
        this.cpsProsecutorJPAMapper = cpsProsecutorJPAMapper;
    }

    //to keep cdi tester jhappy
    public ProsecutionCaseJPAMapper() {
    }

    public ProsecutionCase toJPA(final Hearing hearing, final uk.gov.justice.core.courts.ProsecutionCase pojo) {
        if (null == pojo) {
            return null;
        }
        final ProsecutionCase prosecutionCase = new ProsecutionCase();
        prosecutionCase.setId(new HearingSnapshotKey(pojo.getId(), hearing.getId()));

        if (Objects.nonNull(pojo.getProsecutor())) {
            prosecutionCase.setCpsProsecutor(cpsProsecutorJPAMapper.toJPA(pojo.getProsecutor()));
        }
        prosecutionCase.setProsecutionCaseIdentifier(prosecutionCaseIdentifierJPAMapper.toJPA(pojo.getProsecutionCaseIdentifier()));

        prosecutionCase.setOriginatingOrganisation(pojo.getOriginatingOrganisation());
        prosecutionCase.setInitiationCode(pojo.getInitiationCode());
        prosecutionCase.setCaseStatus(pojo.getCaseStatus());
        prosecutionCase.setMarkers(caseMarkerJPAMapper.toJPA(hearing.getId(), prosecutionCase, pojo.getCaseMarkers()));
        prosecutionCase.setStatementOfFacts(pojo.getStatementOfFacts());
        prosecutionCase.setStatementOfFactsWelsh(pojo.getStatementOfFactsWelsh());
        prosecutionCase.setDefendants(defendantJPAMapper.toJPA(hearing, prosecutionCase, pojo.getDefendants()));
        return prosecutionCase;
    }

    uk.gov.justice.core.courts.ProsecutionCase fromJPA(final ProsecutionCase entity) {
        if (null == entity) {
            return null;
        }

        final uk.gov.justice.core.courts.ProsecutionCase.Builder prosecutionCase = uk.gov.justice.core.courts.ProsecutionCase.prosecutionCase()
                .withId(entity.getId().getId())
                .withOriginatingOrganisation(entity.getOriginatingOrganisation())
                .withInitiationCode(entity.getInitiationCode())
                .withCaseStatus(entity.getCaseStatus())
                .withStatementOfFacts(entity.getStatementOfFacts())
                .withStatementOfFactsWelsh(entity.getStatementOfFactsWelsh())
                .withDefendants(defendantJPAMapper.fromJPA(entity.getDefendants()))
                .withCaseMarkers(caseMarkerJPAMapper.fromJPA(entity.getMarkers()));
        if (Objects.nonNull(entity.getCpsProsecutor())) {
            prosecutionCase.withProsecutor(cpsProsecutorJPAMapper.fromJPA(entity.getCpsProsecutor()));
        }

        prosecutionCase.withProsecutionCaseIdentifier(prosecutionCaseIdentifierJPAMapper.fromJPA(entity.getProsecutionCaseIdentifier()));
        return prosecutionCase.build();
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
        return entities.stream().filter(pc -> !CASE_STATUS_EJECTED.equals(pc.getCaseStatus())).map(this::fromJPA).collect(Collectors.toList());
    }

}