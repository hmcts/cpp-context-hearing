package uk.gov.moj.cpp.hearing.mapping;

import static java.util.Objects.isNull;

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

    @Inject
    public DefendantJPAMapper(final AssociatedPersonJPAMapper associatedPersonJPAMapper,
                              OrganisationJPAMapper organisationJPAMapper,
                              OffenceJPAMapper offenceJPAMapper,
                              PersonDefendantJPAMapper personDefendantJPAMapper) {
        this.associatedPersonJPAMapper = associatedPersonJPAMapper;
        this.organisationJPAMapper = organisationJPAMapper;
        this.offenceJPAMapper = offenceJPAMapper;
        this.personDefendantJPAMapper = personDefendantJPAMapper;
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
        defendant.setIsYouth(pojo.getIsYouth());
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
                .withIsYouth(pojo.getIsYouth())
                .build();
    }

    public List<uk.gov.justice.core.courts.Defendant> fromJPA(final Set<Defendant> entities) {
        if (null == entities) {
            return new ArrayList<>();
        }
        return entities.stream().map(entity -> fromJPA(entity)).collect(Collectors.toList());
    }
}
