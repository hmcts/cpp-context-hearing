package uk.gov.moj.cpp.hearing.mapping;

import uk.gov.justice.core.courts.BailStatus;
import uk.gov.moj.cpp.hearing.persist.entity.ha.PersonDefendant;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class PersonDefendantJPAMapper {

    private OrganisationJPAMapper organisationJPAMapper;
    private PersonJPAMapper personJPAMapper;
    private CustodialEstablishmentJPAMapper custodialEstablishmentJPAMapper;

    @Inject
    public PersonDefendantJPAMapper(final OrganisationJPAMapper organisationJPAMapper,
                                    final PersonJPAMapper personJPAMapper,
                                    final CustodialEstablishmentJPAMapper custodialEstablishmentJPAMapper) {
        this.organisationJPAMapper = organisationJPAMapper;
        this.personJPAMapper = personJPAMapper;
        this.custodialEstablishmentJPAMapper = custodialEstablishmentJPAMapper;
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
            personDefendant.setBailStatusDesc(pojo.getBailStatus().getDescription());
            personDefendant.setBailStatusId(pojo.getBailStatus().getId());
            personDefendant.setBailStatusCode(pojo.getBailStatus().getCode());
        }
        personDefendant.setCustodyTimeLimit(pojo.getCustodyTimeLimit());
        personDefendant.setDriverNumber(pojo.getDriverNumber());
        personDefendant.setEmployerOrganisation(organisationJPAMapper.toJPA(pojo.getEmployerOrganisation()));
        personDefendant.setEmployerPayrollReference(pojo.getEmployerPayrollReference());
        personDefendant.setPerceivedBirthYear(pojo.getPerceivedBirthYear());
        personDefendant.setPersonDetails(personJPAMapper.toJPA(pojo.getPersonDetails()));
        personDefendant.setCustodialEstablishment(custodialEstablishmentJPAMapper.toJPA(pojo.getCustodialEstablishment()));
        return personDefendant;
    }

    public uk.gov.justice.core.courts.PersonDefendant fromJPA(final PersonDefendant pojo) {
        if (null == pojo) {
            return null;
        }
        uk.gov.justice.core.courts.PersonDefendant.Builder personDetailsBuilder = uk.gov.justice.core.courts.PersonDefendant.personDefendant()
                .withArrestSummonsNumber(pojo.getArrestSummonsNumber())
                .withBailStatus(extractBailStatus(pojo))
                .withCustodyTimeLimit(pojo.getCustodyTimeLimit())
                .withDriverNumber(pojo.getDriverNumber())
                .withEmployerOrganisation(organisationJPAMapper.fromJPA(pojo.getEmployerOrganisation()))
                .withEmployerPayrollReference(pojo.getEmployerPayrollReference())
                .withPerceivedBirthYear(pojo.getPerceivedBirthYear())
                .withPersonDetails(personJPAMapper.fromJPA(pojo.getPersonDetails()))
                .withCustodialEstablishment(custodialEstablishmentJPAMapper.fromJPA(pojo.getCustodialEstablishment()));
        return personDetailsBuilder
                .build();
    }

    private BailStatus extractBailStatus(final PersonDefendant pojo) {
        if (pojo.getBailStatusCode() == null && pojo.getBailStatusId() == null && pojo.getBailStatusDesc() == null) {
            return null;
        } else {
            return BailStatus.bailStatus().withId(pojo.getBailStatusId()).withCode(pojo.getBailStatusCode()).withDescription(pojo.getBailStatusDesc()).build();
        }
    }

}