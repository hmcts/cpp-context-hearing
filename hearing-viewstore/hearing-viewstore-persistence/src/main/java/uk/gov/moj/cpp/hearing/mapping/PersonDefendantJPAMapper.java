package uk.gov.moj.cpp.hearing.mapping;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import uk.gov.moj.cpp.hearing.persist.entity.ha.PersonDefendant;

@ApplicationScoped
public class PersonDefendantJPAMapper {

    private DefendantAliasesJPAMapper defendantAliasesJPAMapper;
    private OrganisationJPAMapper organisationJPAMapper;
    private PersonJPAMapper personJPAMapper;

    @Inject
    public PersonDefendantJPAMapper(final DefendantAliasesJPAMapper defendantAliasesJPAMapper,
            final OrganisationJPAMapper organisationJPAMapper, final PersonJPAMapper personJPAMapper) {
        this.defendantAliasesJPAMapper = defendantAliasesJPAMapper;
        this.organisationJPAMapper = organisationJPAMapper;
        this.personJPAMapper = personJPAMapper;
    }

    //To keep cditester happy
    public PersonDefendantJPAMapper() {
    }

    public PersonDefendant toJPA(final uk.gov.justice.json.schemas.core.PersonDefendant pojo) {
        if (null == pojo) {
            return null;
        }
        final PersonDefendant personDefendant = new PersonDefendant();
        personDefendant.setAliases(defendantAliasesJPAMapper.toJPA(pojo.getAliases()));
        personDefendant.setArrestSummonsNumber(pojo.getArrestSummonsNumber());
        personDefendant.setBailStatus(pojo.getBailStatus());
        personDefendant.setCustodyTimeLimit(pojo.getCustodyTimeLimit());
        personDefendant.setDriverNumber(pojo.getDriverNumber());
        personDefendant.setEmployerOrganisation(organisationJPAMapper.toJPA(pojo.getEmployerOrganisation()));
        personDefendant.setEmployerPayrollReference(pojo.getEmployerPayrollReference());
        personDefendant.setObservedEthnicityCode(pojo.getObservedEthnicityCode());
        personDefendant.setObservedEthnicityId(pojo.getObservedEthnicityId());
        personDefendant.setPerceivedBirthYear(pojo.getPerceivedBirthYear());
        personDefendant.setPersonDetails(personJPAMapper.toJPA(pojo.getPersonDetails()));
        personDefendant.setPncId(pojo.getPncId());
        personDefendant.setSelfDefinedEthnicityCode(pojo.getSelfDefinedEthnicityCode());
        personDefendant.setSelfDefinedEthnicityId(pojo.getSelfDefinedEthnicityId());
        return personDefendant;
    }

    public uk.gov.justice.json.schemas.core.PersonDefendant fromJPA(final PersonDefendant pojo) {
        if (null == pojo) {
            return null;
        }
        return uk.gov.justice.json.schemas.core.PersonDefendant.personDefendant()
                .withAliases(defendantAliasesJPAMapper.fromJPA(pojo.getAliases()))
                .withArrestSummonsNumber(pojo.getArrestSummonsNumber())
                .withBailStatus(pojo.getBailStatus())
                .withCustodyTimeLimit(pojo.getCustodyTimeLimit())
                .withDriverNumber(pojo.getDriverNumber())
                .withEmployerOrganisation(organisationJPAMapper.fromJPA(pojo.getEmployerOrganisation()))
                .withEmployerPayrollReference(pojo.getEmployerPayrollReference())
                .withObservedEthnicityCode(pojo.getObservedEthnicityCode())
                .withObservedEthnicityId(pojo.getObservedEthnicityId())
                .withPerceivedBirthYear(pojo.getPerceivedBirthYear())
                .withPersonDetails(personJPAMapper.fromJPA(pojo.getPersonDetails()))
                .withPncId(pojo.getPncId())
                .withSelfDefinedEthnicityCode(pojo.getSelfDefinedEthnicityCode())
                .withSelfDefinedEthnicityId(pojo.getSelfDefinedEthnicityId())
                .build();
    }
}