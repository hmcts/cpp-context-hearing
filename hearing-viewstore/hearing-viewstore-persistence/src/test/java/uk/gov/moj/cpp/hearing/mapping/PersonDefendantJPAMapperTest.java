package uk.gov.moj.cpp.hearing.mapping;

import static io.github.benas.randombeans.EnhancedRandomBuilder.aNewEnhancedRandom;
import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.gov.moj.cpp.hearing.mapping.OrganisationJPAMapperTest.whenOrganization;
import static uk.gov.moj.cpp.hearing.mapping.PersonJPAMapperTest.whenPerson;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;

import uk.gov.justice.core.courts.BailStatus;
import uk.gov.justice.core.courts.Organisation;
import uk.gov.justice.core.courts.Person;
import uk.gov.justice.core.courts.PersonDefendant;
import uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher;

import java.util.UUID;

import org.junit.Test;

public class PersonDefendantJPAMapperTest {

    private static final UUID BAIL_STATUS_ID = randomUUID();
    private PersonDefendantJPAMapper personDefendantJPAMapper = JPACompositeMappers.PERSON_DEFENDANT_JPA_MAPPER;

    @Test
    public void testFromJPA() {
        final uk.gov.moj.cpp.hearing.persist.entity.ha.PersonDefendant personDefendantEntity = aNewEnhancedRandom().nextObject(uk.gov.moj.cpp.hearing.persist.entity.ha.PersonDefendant.class);
        personDefendantEntity.setBailStatusDesc("Conditional Bail");
        personDefendantEntity.setBailStatusCode("B");
        personDefendantEntity.setBailStatusId(BAIL_STATUS_ID);
        assertThat(personDefendantJPAMapper.fromJPA(personDefendantEntity), whenPersonDefendant(isBean(PersonDefendant.class), personDefendantEntity));
    }

    @Test
    public void testToJPA() {
        final PersonDefendant personDefendantPojo = aNewEnhancedRandom().nextObject(PersonDefendant.class);
        assertThat(personDefendantJPAMapper.toJPA(personDefendantPojo), whenPersonDefendant(isBean(uk.gov.moj.cpp.hearing.persist.entity.ha.PersonDefendant.class), personDefendantPojo));
    }

    public static BeanMatcher<PersonDefendant> whenPersonDefendant(final BeanMatcher<PersonDefendant> m,
                                                                   final uk.gov.moj.cpp.hearing.persist.entity.ha.PersonDefendant entity) {
        return
//                m.with(PersonDefendant::getAliases, is(Arrays.asList(entity.getAliases().split(","))))
                m.with(PersonDefendant::getArrestSummonsNumber, is(entity.getArrestSummonsNumber()))
                        .with(pd -> pd.getBailStatus().getDescription(), is(entity.getBailStatusDesc()))
                        .with(pd -> pd.getBailStatus().getCode(), is(entity.getBailStatusCode()))
                        .with(pd -> pd.getBailStatus().getId(), is(entity.getBailStatusId()))
                        .with(PersonDefendant::getCustodyTimeLimit, is(entity.getCustodyTimeLimit()))
                        .with(PersonDefendant::getDriverNumber, is(entity.getDriverNumber()))
                        .with(PersonDefendant::getEmployerOrganisation, whenOrganization(isBean(Organisation.class), entity.getEmployerOrganisation()))
                        .with(PersonDefendant::getEmployerPayrollReference, is(entity.getEmployerPayrollReference()))
//                .with(PersonDefendant::getPersonDetails, is(entity.getObservedEthnicityCode()))
//                .with(PersonDefendant::getObservedEthnicityId, is(entity.getObservedEthnicityId()))
                        .with(PersonDefendant::getPerceivedBirthYear, is(entity.getPerceivedBirthYear()))
                        .with(PersonDefendant::getPersonDetails, whenPerson(isBean(Person.class), entity.getPersonDetails()))
//                .with(PersonDefendant::getSelfDefinedEthnicityCode, is(entity.getSelfDefinedEthnicityCode()))
//                .with(PersonDefendant::getSelfDefinedEthnicityId, is(entity.getSelfDefinedEthnicityId()))
                ;
    }

    public static BeanMatcher<uk.gov.moj.cpp.hearing.persist.entity.ha.PersonDefendant> whenPersonDefendant(
            final BeanMatcher<uk.gov.moj.cpp.hearing.persist.entity.ha.PersonDefendant> m, final PersonDefendant pojo) {
//        final String aliases = pojo.getAliases().toString();
        return
//                m.with(uk.gov.moj.cpp.hearing.persist.entity.ha.PersonDefendant::getAliases, is(aliases.substring(1, aliases.length() -1)))
                m.with(uk.gov.moj.cpp.hearing.persist.entity.ha.PersonDefendant::getArrestSummonsNumber, is(pojo.getArrestSummonsNumber()))
                        .with(uk.gov.moj.cpp.hearing.persist.entity.ha.PersonDefendant::getBailStatusDesc, is(pojo.getBailStatus().getDescription()))
                        .with(uk.gov.moj.cpp.hearing.persist.entity.ha.PersonDefendant::getBailStatusId, is(pojo.getBailStatus().getId()))
                        .with(uk.gov.moj.cpp.hearing.persist.entity.ha.PersonDefendant::getBailStatusCode, is(pojo.getBailStatus().getCode()))
                        .with(uk.gov.moj.cpp.hearing.persist.entity.ha.PersonDefendant::getCustodyTimeLimit, is(pojo.getCustodyTimeLimit()))
                        .with(uk.gov.moj.cpp.hearing.persist.entity.ha.PersonDefendant::getDriverNumber, is(pojo.getDriverNumber()))
                        .with(uk.gov.moj.cpp.hearing.persist.entity.ha.PersonDefendant::getEmployerOrganisation, whenOrganization(isBean(uk.gov.moj.cpp.hearing.persist.entity.ha.Organisation.class), pojo.getEmployerOrganisation()))
                        .with(uk.gov.moj.cpp.hearing.persist.entity.ha.PersonDefendant::getEmployerPayrollReference, is(pojo.getEmployerPayrollReference()))
//                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.PersonDefendant::getObservedEthnicityCode, is(pojo.getObservedEthnicityCode()))
//                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.PersonDefendant::getObservedEthnicityId, is(pojo.getObservedEthnicityId()))
                        .with(uk.gov.moj.cpp.hearing.persist.entity.ha.PersonDefendant::getPerceivedBirthYear, is(pojo.getPerceivedBirthYear()))
                        .with(uk.gov.moj.cpp.hearing.persist.entity.ha.PersonDefendant::getPersonDetails, whenPerson(isBean(uk.gov.moj.cpp.hearing.persist.entity.ha.Person.class), pojo.getPersonDetails()))
//                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.PersonDefendant::getPncId, is(pojo.getPncId()))
//                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.PersonDefendant::getSelfDefinedEthnicityCode, is(pojo.getSelfDefinedEthnicityCode()))
//                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.PersonDefendant::getSelfDefinedEthnicityId, is(pojo.getSelfDefinedEthnicityId()))
                ;
    }
}