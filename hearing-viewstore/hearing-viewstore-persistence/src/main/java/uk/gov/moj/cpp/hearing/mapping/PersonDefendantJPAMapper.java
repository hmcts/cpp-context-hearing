package uk.gov.moj.cpp.hearing.mapping;

import uk.gov.justice.core.courts.BailStatus;
import uk.gov.moj.cpp.hearing.persist.entity.ha.PersonDefendant;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class PersonDefendantJPAMapper {

    private OrganisationJPAMapper organisationJPAMapper;
    private PersonJPAMapper personJPAMapper;

    @Inject
    public PersonDefendantJPAMapper(final OrganisationJPAMapper organisationJPAMapper, final PersonJPAMapper personJPAMapper) {
        this.organisationJPAMapper = organisationJPAMapper;
        this.personJPAMapper = personJPAMapper;
    }

    //To keep cditester happy
    public PersonDefendantJPAMapper() {
    }

    public PersonDefendant toJPA(final uk.gov.justice.core.courts.PersonDefendant pojo) {
        if (null == pojo) {
            return null;
        }
        final PersonDefendant personDefendant = new PersonDefendant();
        personDefendant.setArrestSummonsNumber(pojo.getArrestSummonsNumber());
        if (pojo.getBailStatus() != null) {
            personDefendant.setBailStatus(pojo.getBailStatus().name());
        }
        personDefendant.setCustodyTimeLimit(pojo.getCustodyTimeLimit());
        personDefendant.setDriverNumber(pojo.getDriverNumber());
        personDefendant.setEmployerOrganisation(organisationJPAMapper.toJPA(pojo.getEmployerOrganisation()));
        personDefendant.setEmployerPayrollReference(pojo.getEmployerPayrollReference());
        personDefendant.setPerceivedBirthYear(pojo.getPerceivedBirthYear());
        personDefendant.setPersonDetails(personJPAMapper.toJPA(pojo.getPersonDetails()));
        return personDefendant;
    }

    public uk.gov.justice.core.courts.PersonDefendant fromJPA(final PersonDefendant pojo) {
        if (null == pojo) {
            return null;
        }
        return uk.gov.justice.core.courts.PersonDefendant.personDefendant()
                .withArrestSummonsNumber(pojo.getArrestSummonsNumber())
                .withBailStatus(pojo.getBailStatus() != null ? BailStatus.valueOf(pojo.getBailStatus()) : null)
                .withCustodyTimeLimit(pojo.getCustodyTimeLimit())
                .withDriverNumber(pojo.getDriverNumber())
                .withEmployerOrganisation(organisationJPAMapper.fromJPA(pojo.getEmployerOrganisation()))
                .withEmployerPayrollReference(pojo.getEmployerPayrollReference())
                .withPerceivedBirthYear(pojo.getPerceivedBirthYear())
                .withPersonDetails(personJPAMapper.fromJPA(pojo.getPersonDetails()))
                .build();
    }
}