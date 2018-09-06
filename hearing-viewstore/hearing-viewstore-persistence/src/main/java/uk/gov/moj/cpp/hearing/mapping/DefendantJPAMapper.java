package uk.gov.moj.cpp.hearing.mapping;

import static java.util.Objects.isNull;

import uk.gov.justice.json.schemas.core.LegalEntityDefendant;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Defendant;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCase;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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

    public Defendant toJPA(final Hearing hearing, final ProsecutionCase prosecutionCase, final uk.gov.justice.json.schemas.core.Defendant pojo) {
        final Defendant defendant = toJPA(hearing, pojo);

        if (isNull(defendant)) {
            return null;
        }

        defendant.setProsecutionCase(prosecutionCase);
        defendant.setProsecutionCaseId(prosecutionCase.getId().getId());
        return defendant;
    }

    private Defendant toJPA(final Hearing hearing, final uk.gov.justice.json.schemas.core.Defendant pojo) {
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
        return defendant;
    }

    public List<Defendant> toJPA(Hearing hearing, final ProsecutionCase prosecutionCase, final List<uk.gov.justice.json.schemas.core.Defendant> pojos) {
        if (null == pojos) {
            return new ArrayList<>();
        }
        return pojos.stream().map(pojo -> toJPA(hearing, prosecutionCase, pojo)).collect(Collectors.toList());
    }

    uk.gov.justice.json.schemas.core.Defendant fromJPA(final UUID prosecutionCaseId, final Defendant pojo) {
        if (null == pojo) {
            return null;
        }
        return uk.gov.justice.json.schemas.core.Defendant.defendant()
                .withId(pojo.getId().getId())
                .withAssociatedPersons(associatedPersonJPAMapper.fromJPA(pojo.getAssociatedPersons()))
                .withDefenceOrganisation(organisationJPAMapper.fromJPA(pojo.getDefenceOrganisation()))
                .withLegalEntityDefendant(LegalEntityDefendant.legalEntityDefendant()
                        .withOrganisation(organisationJPAMapper.fromJPA(pojo.getLegalEntityOrganisation()))
                        .build())
                .withMitigation(pojo.getMitigation())
                .withMitigationWelsh(pojo.getMitigationWelsh())
                .withNumberOfPreviousConvictionsCited(pojo.getNumberOfPreviousConvictionsCited())
                .withOffences(offenceJPAMapper.fromJPA(pojo.getOffences()))
                .withPersonDefendant(personDefendantJPAMapper.fromJPA(pojo.getPersonDefendant()))
                .withProsecutionAuthorityReference(pojo.getProsecutionAuthorityReference())
                .withWitnessStatement(pojo.getWitnessStatement())
                .withWitnessStatementWelsh(pojo.getWitnessStatementWelsh())
                .withProsecutionCaseId(prosecutionCaseId)
                .build();
    }

    public List<uk.gov.justice.json.schemas.core.Defendant> fromJPA(final UUID prosecutionCaseId, final List<Defendant> entities) {
        if (null == entities) {
            return new ArrayList<>();
        }
        return entities.stream().map(entity -> fromJPA(prosecutionCaseId, entity)).collect(Collectors.toList());
    }
}
