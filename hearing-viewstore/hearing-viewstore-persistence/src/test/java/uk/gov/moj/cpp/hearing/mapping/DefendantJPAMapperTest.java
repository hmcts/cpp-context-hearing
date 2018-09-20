package uk.gov.moj.cpp.hearing.mapping;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.gov.moj.cpp.hearing.mapping.AssociatedPersonJPAMapperTest.whenFirstAssociatedPerson;
import static uk.gov.moj.cpp.hearing.mapping.OffenceJPAMapperTest.whenFirstOffence;
import static uk.gov.moj.cpp.hearing.mapping.OrganisationJPAMapperTest.whenOrganization;
import static uk.gov.moj.cpp.hearing.mapping.PersonDefendantJPAMapperTest.whenPersonDefendant;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.utils.HearingJPADataTemplate.aNewHearingJPADataTemplate;

import org.junit.Test;

import uk.gov.justice.json.schemas.core.AssociatedPerson;
import uk.gov.justice.json.schemas.core.Defendant;
import uk.gov.justice.json.schemas.core.LegalEntityDefendant;
import uk.gov.justice.json.schemas.core.Offence;
import uk.gov.justice.json.schemas.core.Organisation;
import uk.gov.justice.json.schemas.core.PersonDefendant;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCase;
import uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher;
import uk.gov.moj.cpp.hearing.test.matchers.ElementAtListMatcher;

public class DefendantJPAMapperTest {

    private DefendantJPAMapper defendantJPAMapper = JPACompositeMappers.DEFENDANT_JPA_MAPPER;

    @Test
    public void testFromJPA() {

        final ProsecutionCase prosecutionCaseEntity = aNewHearingJPADataTemplate().getHearing().getProsecutionCases().iterator().next();
        final uk.gov.moj.cpp.hearing.persist.entity.ha.Defendant defendantEntity = prosecutionCaseEntity.getDefendants().iterator().next();

        assertThat(defendantJPAMapper.fromJPA(prosecutionCaseEntity.getId().getId(), defendantEntity), whenDefendant(isBean(Defendant.class), defendantEntity));
    }

    @Test
    public void testToJPA() {

        final uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing hearingEntity = aNewHearingJPADataTemplate().getHearing();
        final uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCase prosecutionCaseEntity = hearingEntity.getProsecutionCases().iterator().next();
        final uk.gov.moj.cpp.hearing.persist.entity.ha.Defendant defendantEntity = prosecutionCaseEntity.getDefendants().iterator().next();
        final Defendant defendantPojo = defendantJPAMapper.fromJPA(prosecutionCaseEntity.getId().getId(), defendantEntity);

        assertThat(defendantJPAMapper.toJPA(hearingEntity, prosecutionCaseEntity, defendantPojo), whenDefendant(isBean(uk.gov.moj.cpp.hearing.persist.entity.ha.Defendant.class), defendantPojo));
    }

    @SuppressWarnings("unchecked")
    public static ElementAtListMatcher whenFirstDefendant(final BeanMatcher<?> m, final uk.gov.moj.cpp.hearing.persist.entity.ha.Defendant entity) {
        return ElementAtListMatcher.first(whenDefendant((BeanMatcher<Defendant>) m, entity));
    }

    @SuppressWarnings("unchecked")
    public static ElementAtListMatcher whenFirstDefendant(final BeanMatcher<?> m, final Defendant entity) {
        return ElementAtListMatcher.first(whenDefendant((BeanMatcher<uk.gov.moj.cpp.hearing.persist.entity.ha.Defendant>) m, entity));
    }

    public static BeanMatcher<Defendant> whenDefendant(final BeanMatcher<Defendant> m,
            final uk.gov.moj.cpp.hearing.persist.entity.ha.Defendant entity) {

        return m.with(Defendant::getAssociatedPersons, 
                        whenFirstAssociatedPerson(isBean(AssociatedPerson.class), entity.getAssociatedPersons().iterator().next()))

                .with(Defendant::getDefenceOrganisation, 
                        whenOrganization(isBean(Organisation.class), entity.getDefenceOrganisation()))

                .with(Defendant::getId, is(entity.getId().getId()))

                .with(Defendant::getLegalEntityDefendant, isBean(LegalEntityDefendant.class)
                        .with(LegalEntityDefendant::getOrganisation, whenOrganization(isBean(Organisation.class), entity.getLegalEntityOrganisation())))

                .with(Defendant::getOffences, 
                        whenFirstOffence(isBean(Offence.class), (entity.getOffences().iterator().next())))

                .with(Defendant::getPersonDefendant, 
                        whenPersonDefendant(isBean(PersonDefendant.class), entity.getPersonDefendant()))

                .with(Defendant::getProsecutionAuthorityReference, is(entity.getProsecutionAuthorityReference()))
                .with(Defendant::getProsecutionCaseId, is(entity.getProsecutionCaseId()))
                .with(Defendant::getWitnessStatement, is(entity.getWitnessStatement()))
                .with(Defendant::getWitnessStatementWelsh, is(entity.getWitnessStatementWelsh()));
    }

    public static BeanMatcher<uk.gov.moj.cpp.hearing.persist.entity.ha.Defendant> whenDefendant(
            final BeanMatcher<uk.gov.moj.cpp.hearing.persist.entity.ha.Defendant> m, final Defendant pojo) {

        return m
//                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.Defendant::getAssociatedPersons,
//                        whenFirstAssociatedPerson(isBean(uk.gov.moj.cpp.hearing.persist.entity.ha.AssociatedPerson.class), pojo.getAssociatedPersons().get(0)))

                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.Defendant::getDefenceOrganisation, 
                        whenOrganization(isBean(uk.gov.moj.cpp.hearing.persist.entity.ha.Organisation.class), pojo.getDefenceOrganisation()))
                 
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.Defendant::getId, isBean(HearingSnapshotKey.class)
                        .with(HearingSnapshotKey::getId, is(pojo.getId())))
                
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.Defendant::getLegalEntityOrganisation, 
                        whenOrganization(isBean(uk.gov.moj.cpp.hearing.persist.entity.ha.Organisation.class), pojo.getLegalEntityDefendant().getOrganisation()))

//                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.Defendant::getOffences,
//                        whenFirstOffence(isBean(uk.gov.moj.cpp.hearing.persist.entity.ha.Offence.class), (pojo.getOffences().get(0))))

                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.Defendant::getPersonDefendant, 
                        whenPersonDefendant(isBean(uk.gov.moj.cpp.hearing.persist.entity.ha.PersonDefendant.class), pojo.getPersonDefendant()))

                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.Defendant::getProsecutionAuthorityReference, is(pojo.getProsecutionAuthorityReference()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.Defendant::getProsecutionCaseId, is(pojo.getProsecutionCaseId()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.Defendant::getWitnessStatement, is(pojo.getWitnessStatement()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.Defendant::getWitnessStatementWelsh, is(pojo.getWitnessStatementWelsh()));
    }
}