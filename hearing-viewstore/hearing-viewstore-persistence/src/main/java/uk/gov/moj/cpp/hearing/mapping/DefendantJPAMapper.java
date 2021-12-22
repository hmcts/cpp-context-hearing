package uk.gov.moj.cpp.hearing.mapping;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import uk.gov.justice.core.courts.LegalEntityDefendant;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Defendant;
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
public class DefendantJPAMapper {

    private AssociatedPersonJPAMapper associatedPersonJPAMapper;
    private OrganisationJPAMapper organisationJPAMapper;
    private OffenceJPAMapper offenceJPAMapper;
    private PersonDefendantJPAMapper personDefendantJPAMapper;
    private AssociatedDefenceOrganisationJPAMapper associatedDefenceOrganisationJPAMapper;

    @Inject
    public DefendantJPAMapper(final AssociatedPersonJPAMapper associatedPersonJPAMapper,
                              OrganisationJPAMapper organisationJPAMapper,
                              OffenceJPAMapper offenceJPAMapper,
                              PersonDefendantJPAMapper personDefendantJPAMapper,
                              final AssociatedDefenceOrganisationJPAMapper associatedDefenceOrganisationJPAMapper) {
        this.associatedPersonJPAMapper = associatedPersonJPAMapper;
        this.organisationJPAMapper = organisationJPAMapper;
        this.offenceJPAMapper = offenceJPAMapper;
        this.personDefendantJPAMapper = personDefendantJPAMapper;
        this.associatedDefenceOrganisationJPAMapper = associatedDefenceOrganisationJPAMapper;
    }

    //To keep cditester happy
    public DefendantJPAMapper() {

    }

    public Defendant toJPA(final Hearing hearing, final ProsecutionCase prosecutionCase, final uk.gov.justice.core.courts.Defendant pojo) {
        final Defendant defendant = toJPA(hearing, pojo);

        if (isNull(defendant)) {
            return null;
        }

        defendant.setProsecutionCase(prosecutionCase);
        defendant.setProsecutionCaseId(prosecutionCase.getId().getId());
        return defendant;
    }

    private Defendant toJPA(final Hearing hearing, final uk.gov.justice.core.courts.Defendant pojo) {
        if (null == pojo) {
            return null;
        }
        final Defendant defendant = new Defendant();
        defendant.setId(new HearingSnapshotKey(pojo.getId(), hearing.getId()));
        defendant.setAssociatedPersons(associatedPersonJPAMapper.toJPA(hearing, defendant, pojo.getAssociatedPersons()));
        defendant.setDefenceOrganisation(organisationJPAMapper.toJPA(pojo.getDefenceOrganisation()));
        if (null != pojo.getLegalEntityDefendant()) {
            defendant.setLegalEntityOrganisation(organisationJPAMapper.toJPA(pojo.getLegalEntityDefendant().getOrganisation()));
        }
        defendant.setMitigation(pojo.getMitigation());
        defendant.setMitigationWelsh(pojo.getMitigationWelsh());
        defendant.setNumberOfPreviousConvictionsCited(pojo.getNumberOfPreviousConvictionsCited());
        defendant.setOffences(offenceJPAMapper.toJPA(hearing, defendant.getId().getId(), pojo.getOffences()));
        defendant.setPersonDefendant(personDefendantJPAMapper.toJPA(pojo.getPersonDefendant()));
        defendant.setProsecutionAuthorityReference(pojo.getProsecutionAuthorityReference());
        defendant.setWitnessStatement(pojo.getWitnessStatement());
        defendant.setWitnessStatementWelsh(pojo.getWitnessStatementWelsh());
        defendant.setPncId(pojo.getPncId());
        defendant.setMasterDefendantId(pojo.getMasterDefendantId());
        defendant.setCourtProceedingsInitiated(pojo.getCourtProceedingsInitiated());
        if (null != pojo.getIsYouth()) {
            defendant.setIsYouth(pojo.getIsYouth());
        }
        defendant.setLegalaidStatus(pojo.getLegalAidStatus());
        if (null != pojo.getProceedingsConcluded()) {
            defendant.setProceedingsConcluded(pojo.getProceedingsConcluded());
        }
        defendant.setAssociatedDefenceOrganisation(associatedDefenceOrganisationJPAMapper.toJPA(pojo.getAssociatedDefenceOrganisation()));
        return defendant;
    }

    public Set<Defendant> toJPA(Hearing hearing, final ProsecutionCase prosecutionCase, final List<uk.gov.justice.core.courts.Defendant> pojos) {
        if (null == pojos) {
            return new HashSet<>();
        }
        return pojos.stream().map(pojo -> toJPA(hearing, prosecutionCase, pojo)).collect(Collectors.toSet());
    }

    uk.gov.justice.core.courts.Defendant fromJPA(final Defendant pojo) {
        if (null == pojo) {
            return null;
        }
        return uk.gov.justice.core.courts.Defendant.defendant()
                .withId(pojo.getId().getId())
                .withAssociatedPersons(associatedPersonJPAMapper.fromJPA(pojo.getAssociatedPersons()))
                .withDefenceOrganisation(organisationJPAMapper.fromJPA(pojo.getDefenceOrganisation()))
                .withLegalEntityDefendant(pojo.getLegalEntityOrganisation() != null ? LegalEntityDefendant.legalEntityDefendant()
                        .withOrganisation(organisationJPAMapper.fromJPA(pojo.getLegalEntityOrganisation()))
                        .build() : null)
                .withMitigation(pojo.getMitigation())
                .withMitigationWelsh(pojo.getMitigationWelsh())
                .withNumberOfPreviousConvictionsCited(pojo.getNumberOfPreviousConvictionsCited())
                .withOffences(offenceJPAMapper.fromJPA(pojo.getOffences()))
                .withPersonDefendant(personDefendantJPAMapper.fromJPA(pojo.getPersonDefendant()))
                .withProsecutionAuthorityReference(pojo.getProsecutionAuthorityReference())
                .withWitnessStatement(pojo.getWitnessStatement())
                .withPncId(pojo.getPncId())
                .withWitnessStatementWelsh(pojo.getWitnessStatementWelsh())
                .withProsecutionCaseId(pojo.getProsecutionCaseId())
                .withMasterDefendantId(pojo.getMasterDefendantId())
                .withCourtProceedingsInitiated(pojo.getCourtProceedingsInitiated())
                .withIsYouth(pojo.getIsYouth())
                .withLegalAidStatus(pojo.getLegalaidStatus())
                .withProceedingsConcluded(pojo.isProceedingsConcluded())
                .withAssociatedDefenceOrganisation(associatedDefenceOrganisationJPAMapper.fromJPA(pojo.getAssociatedDefenceOrganisation()))
                .build();
    }

    public List<uk.gov.justice.core.courts.Defendant> fromJPA(final Set<Defendant> entities) {
        if (null == entities) {
            return new ArrayList<>();
        }
        return entities.stream().map(this::fromJPA).collect(Collectors.toList());
    }

    public List<uk.gov.justice.core.courts.Defendant> fromJPAWithCourtListRestrictions(final Set<Defendant> entities) {
        if (null == entities) {
            return new ArrayList<>();
        }
        return entities.stream()
                .filter(defendant -> nonNull(defendant) && (isNull(defendant.getCourtListRestricted()) || !defendant.getCourtListRestricted()))
                .map(this::fromJPA).collect(Collectors.toList());
    }
}
