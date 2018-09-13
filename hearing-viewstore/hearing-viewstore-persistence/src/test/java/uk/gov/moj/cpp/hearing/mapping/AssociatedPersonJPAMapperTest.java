package uk.gov.moj.cpp.hearing.mapping;

import static io.github.benas.randombeans.EnhancedRandomBuilder.aNewEnhancedRandom;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.gov.moj.cpp.hearing.mapping.PersonJPAMapperTest.whenPerson;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.utils.HearingJPADataTemplate.aNewHearingJPADataTemplate;

import org.junit.Test;
import uk.gov.justice.json.schemas.core.AssociatedPerson;
import uk.gov.justice.json.schemas.core.Person;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Defendant;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher;
import uk.gov.moj.cpp.hearing.test.matchers.ElementAtListMatcher;

public class AssociatedPersonJPAMapperTest {

    private AssociatedPersonJPAMapper associatedPersonJPAMapper = JPACompositeMappers.ASSOCIATED_PERSON_JPA_MAPPER;

    @Test
    public void testFromJPA() {
        final uk.gov.moj.cpp.hearing.persist.entity.ha.AssociatedPerson associatedPersonEntity = aNewEnhancedRandom().nextObject(uk.gov.moj.cpp.hearing.persist.entity.ha.AssociatedPerson.class);
        assertThat(associatedPersonJPAMapper.fromJPA(associatedPersonEntity), 
                whenAssociatedPerson(isBean(AssociatedPerson.class), associatedPersonEntity));
    }

    @Test
    public void testToJPA() {
        final Hearing hearingEntity = aNewHearingJPADataTemplate().getHearing();
        final Defendant defendantEntity = hearingEntity.getProsecutionCases().get(0).getDefendants().get(0);
        final AssociatedPerson associatedPersonPojo = aNewEnhancedRandom().nextObject(AssociatedPerson.class);
        assertThat(associatedPersonJPAMapper.toJPA(hearingEntity, defendantEntity, associatedPersonPojo), 
                whenAssociatedPerson(isBean(uk.gov.moj.cpp.hearing.persist.entity.ha.AssociatedPerson.class), associatedPersonPojo));
    }

    @SuppressWarnings("unchecked")
    public static ElementAtListMatcher whenFirstAssociatedPerson(final BeanMatcher<?> m, final uk.gov.moj.cpp.hearing.persist.entity.ha.AssociatedPerson entity) {
        return ElementAtListMatcher.first(whenAssociatedPerson((BeanMatcher<AssociatedPerson>) m, entity));
    }

    @SuppressWarnings("unchecked")
    public static ElementAtListMatcher whenFirstAssociatedPerson(final BeanMatcher<?> m, final AssociatedPerson pojo) {
        return ElementAtListMatcher.first(whenAssociatedPerson((BeanMatcher<uk.gov.moj.cpp.hearing.persist.entity.ha.AssociatedPerson>) m, pojo));
    }

    public static BeanMatcher<AssociatedPerson> whenAssociatedPerson(final BeanMatcher<AssociatedPerson> m,
            final uk.gov.moj.cpp.hearing.persist.entity.ha.AssociatedPerson entity) {
        return m.with(AssociatedPerson::getRole, is(entity.getRole()))
                .with(AssociatedPerson::getPerson, whenPerson(isBean(Person.class), entity.getPerson()));
    }

    public static BeanMatcher<uk.gov.moj.cpp.hearing.persist.entity.ha.AssociatedPerson> whenAssociatedPerson(
            final BeanMatcher<uk.gov.moj.cpp.hearing.persist.entity.ha.AssociatedPerson> m, final uk.gov.justice.json.schemas.core.AssociatedPerson pojo) {

        return m.with(uk.gov.moj.cpp.hearing.persist.entity.ha.AssociatedPerson::getRole, is(pojo.getRole()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.AssociatedPerson::getPerson, whenPerson(isBean(uk.gov.moj.cpp.hearing.persist.entity.ha.Person.class), pojo.getPerson()));
    }
}