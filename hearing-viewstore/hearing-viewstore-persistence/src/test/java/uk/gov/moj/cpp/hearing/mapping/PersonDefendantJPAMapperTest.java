package uk.gov.moj.cpp.hearing.mapping;

import static io.github.benas.randombeans.EnhancedRandomBuilder.aNewEnhancedRandom;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.gov.moj.cpp.hearing.mapping.OrganisationJPAMapperTest.whenOrganization;
import static uk.gov.moj.cpp.hearing.mapping.PersonJPAMapperTest.whenPerson;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;

import java.util.Arrays;

import org.junit.Test;

import uk.gov.justice.core.courts.BailStatus;
import uk.gov.justice.core.courts.Organisation;
import uk.gov.justice.core.courts.Person;
import uk.gov.justice.core.courts.PersonDefendant;
import uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher;

public class PersonDefendantJPAMapperTest {

    private PersonDefendantJPAMapper personDefendantJPAMapper = JPACompositeMappers.PERSON_DEFENDANT_JPA_MAPPER;

    @Test
    public void testFromJPA() {
        final uk.gov.moj.cpp.hearing.persist.entity.ha.PersonDefendant personDefendantEntity = aNewEnhancedRandom().nextObject(uk.gov.moj.cpp.hearing.persist.entity.ha.PersonDefendant.class);
        personDefendantEntity.setBailStatus(BailStatus.CONDITIONAL.name());
        assertThat(personDefendantJPAMapper.fromJPA(personDefendantEntity), whenPersonDefendant(isBean(PersonDefendant.class), personDefendantEntity));
    }

    @Test
    public void testToJPA() {
        final PersonDefendant personDefendantPojo = aNewEnhancedRandom().nextObject(PersonDefendant.class);
        assertThat(personDefendantJPAMapper.toJPA(personDefendantPojo), whenPersonDefendant(isBean(uk.gov.moj.cpp.hearing.persist.entity.ha.PersonDefendant.class), personDefendantPojo));
    }

    public static BeanMatcher<PersonDefendant> whenPersonDefendant(final BeanMatcher<PersonDefendant> m,
            final uk.gov.moj.cpp.hearing.persist.entity.ha.PersonDefendant entity) {
        return m.with(PersonDefendant::getAliases, is(Arrays.asList(entity.getAliases().split(","))))
                .with(PersonDefendant::getArrestSummonsNumber, is(entity.getArrestSummonsNumber()))
                .with(pd->pd.getBailStatus().name(), is(entity.getBailStatus()))
                .with(PersonDefendant::getCustodyTimeLimit, is(entity.getCustodyTimeLimit()))
                .with(PersonDefendant::getDriverNumber, is(entity.getDriverNumber()))
                .with(PersonDefendant::getEmployerOrganisation, whenOrganization(isBean(Organisation.class), entity.getEmployerOrganisation()))
                .with(PersonDefendant::getEmployerPayrollReference, is(entity.getEmployerPayrollReference()))
                .with(PersonDefendant::getObservedEthnicityCode, is(entity.getObservedEthnicityCode()))
                .with(PersonDefendant::getObservedEthnicityId, is(entity.getObservedEthnicityId()))
                .with(PersonDefendant::getPerceivedBirthYear, is(entity.getPerceivedBirthYear()))
                .with(PersonDefendant::getPersonDetails, whenPerson(isBean(Person.class), entity.getPersonDetails()))
                .with(PersonDefendant::getPncId, is(entity.getPncId()))
                .with(PersonDefendant::getSelfDefinedEthnicityCode, is(entity.getSelfDefinedEthnicityCode()))
                .with(PersonDefendant::getSelfDefinedEthnicityId, is(entity.getSelfDefinedEthnicityId()));
    }

    public static BeanMatcher<uk.gov.moj.cpp.hearing.persist.entity.ha.PersonDefendant> whenPersonDefendant(
            final BeanMatcher<uk.gov.moj.cpp.hearing.persist.entity.ha.PersonDefendant> m, final PersonDefendant pojo) {
        final String aliases = pojo.getAliases().toString();
        return m.with(uk.gov.moj.cpp.hearing.persist.entity.ha.PersonDefendant::getAliases, is(aliases.substring(1, aliases.length() -1)))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.PersonDefendant::getArrestSummonsNumber, is(pojo.getArrestSummonsNumber()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.PersonDefendant::getBailStatus, is(pojo.getBailStatus().name()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.PersonDefendant::getCustodyTimeLimit, is(pojo.getCustodyTimeLimit()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.PersonDefendant::getDriverNumber, is(pojo.getDriverNumber()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.PersonDefendant::getEmployerOrganisation, whenOrganization(isBean(uk.gov.moj.cpp.hearing.persist.entity.ha.Organisation.class), pojo.getEmployerOrganisation()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.PersonDefendant::getEmployerPayrollReference, is(pojo.getEmployerPayrollReference()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.PersonDefendant::getObservedEthnicityCode, is(pojo.getObservedEthnicityCode()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.PersonDefendant::getObservedEthnicityId, is(pojo.getObservedEthnicityId()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.PersonDefendant::getPerceivedBirthYear, is(pojo.getPerceivedBirthYear()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.PersonDefendant::getPersonDetails, whenPerson(isBean(uk.gov.moj.cpp.hearing.persist.entity.ha.Person.class), pojo.getPersonDetails()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.PersonDefendant::getPncId, is(pojo.getPncId()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.PersonDefendant::getSelfDefinedEthnicityCode, is(pojo.getSelfDefinedEthnicityCode()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.PersonDefendant::getSelfDefinedEthnicityId, is(pojo.getSelfDefinedEthnicityId()));
    }
}