package uk.gov.moj.cpp.hearing.mapping;

import static org.apache.deltaspike.core.util.ArraysUtils.asSet;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static uk.gov.moj.cpp.hearing.mapping.AssociatedPersonJPAMapperTest.whenFirstAssociatedPerson;
import static uk.gov.moj.cpp.hearing.mapping.OffenceJPAMapperTest.whenFirstOffence;
import static uk.gov.moj.cpp.hearing.mapping.OrganisationJPAMapperTest.whenOrganization;
import static uk.gov.moj.cpp.hearing.mapping.PersonDefendantJPAMapperTest.whenPersonDefendant;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.utils.HearingJPADataTemplate.aNewHearingJPADataTemplate;

import uk.gov.justice.core.courts.AssociatedPerson;
import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.LegalEntityDefendant;
import uk.gov.justice.core.courts.Offence;
import uk.gov.justice.core.courts.Organisation;
import uk.gov.justice.core.courts.PersonDefendant;
import uk.gov.moj.cpp.hearing.persist.entity.ha.AssociatedDefenceOrganisation;
import uk.gov.moj.cpp.hearing.persist.entity.ha.DefenceOrganisation;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCase;
import uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher;
import uk.gov.moj.cpp.hearing.test.matchers.ElementAtListMatcher;

import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

public class DefendantJPAMapperTest {

    private DefendantJPAMapper defendantJPAMapper = JPACompositeMappers.DEFENDANT_JPA_MAPPER;
    private uk.gov.moj.cpp.hearing.persist.entity.ha.Defendant defendantEntity;
    private uk.gov.moj.cpp.hearing.persist.entity.ha.Organisation organisation;
    private HearingSnapshotKey hearingSnapshotKey;


    @Test
    public void testFromJPA() {

        final ProsecutionCase prosecutionCaseEntity = aNewHearingJPADataTemplate().getHearing().getProsecutionCases().iterator().next();
        final uk.gov.moj.cpp.hearing.persist.entity.ha.Defendant defendantEntity = prosecutionCaseEntity.getDefendants().iterator().next();

        assertThat(defendantJPAMapper.fromJPA(defendantEntity), whenDefendant(isBean(Defendant.class), defendantEntity));
    }

    @Test
    public void testToJPA() {

        final uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing hearingEntity = aNewHearingJPADataTemplate().getHearing();
        final ProsecutionCase prosecutionCaseEntity = hearingEntity.getProsecutionCases().iterator().next();
        final uk.gov.moj.cpp.hearing.persist.entity.ha.Defendant defendantEntity = prosecutionCaseEntity.getDefendants().iterator().next();
        final Defendant defendantPojo = defendantJPAMapper.fromJPA(defendantEntity);

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

        return //m.with(Defendant::getAliases, is(entity.getAliases()))
                m.with(Defendant::getAssociatedPersons,
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
                        .with(Defendant::getPncId, is(entity.getPncId()))
                        .with(Defendant::getWitnessStatementWelsh, is(entity.getWitnessStatementWelsh()))
                        .with(Defendant::getLegalAidStatus, is("Granted"))
                        .with(Defendant::getProceedingsConcluded, is(true));
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
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.Defendant::getPncId, is(pojo.getPncId()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.Defendant::getCourtListRestricted, is(true))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.Defendant::getWitnessStatementWelsh, is(pojo.getWitnessStatementWelsh()));
    }

    @Test
    public void testFromJPAMapWithLegalEntityDefendant() {
        setupTestData();
        organisation = new uk.gov.moj.cpp.hearing.persist.entity.ha.Organisation();
        organisation.setName("ABC LTD");
        defendantEntity.setLegalEntityOrganisation(organisation);
        Defendant results = defendantJPAMapper.fromJPA(defendantEntity);
        Assert.assertEquals("ABC LTD", results.getLegalEntityDefendant().getOrganisation().getName());
        Assert.assertNull(results.getPersonDefendant());
    }

    @Test
    public void testFromJPAMapWithPersonDefendant() {
        setupTestData();
        uk.gov.moj.cpp.hearing.persist.entity.ha.PersonDefendant personDefendant = new uk.gov.moj.cpp.hearing.persist.entity.ha.PersonDefendant();
        defendantEntity.setPersonDefendant(personDefendant);
        Defendant results = defendantJPAMapper.fromJPA(defendantEntity);
        Assert.assertNull(results.getLegalEntityDefendant());
        assertNotNull(results.getPersonDefendant());
    }

    @Test
    public void testFromJPAMapWithDefendantCourtListRestriction() {
        uk.gov.moj.cpp.hearing.persist.entity.ha.PersonDefendant personDefendant1 = new uk.gov.moj.cpp.hearing.persist.entity.ha.PersonDefendant();
        uk.gov.moj.cpp.hearing.persist.entity.ha.Defendant defendantEntity1 = new uk.gov.moj.cpp.hearing.persist.entity.ha.Defendant();
        UUID hearingID = UUID.randomUUID();
        defendantEntity1.setId(new HearingSnapshotKey(UUID.randomUUID(), hearingID));
        defendantEntity1.setPersonDefendant(personDefendant1);
        defendantEntity1.setCourtListRestricted(true);

        uk.gov.moj.cpp.hearing.persist.entity.ha.PersonDefendant personDefendant2 = new uk.gov.moj.cpp.hearing.persist.entity.ha.PersonDefendant();
        uk.gov.moj.cpp.hearing.persist.entity.ha.Defendant defendantEntity2 = new uk.gov.moj.cpp.hearing.persist.entity.ha.Defendant();
        defendantEntity2.setId(new HearingSnapshotKey(UUID.randomUUID(), hearingID));
        defendantEntity2.setPersonDefendant(personDefendant2);
        defendantEntity2.setCourtListRestricted(false);

        List<Defendant> results = defendantJPAMapper.fromJPAWithCourtListRestrictions(asSet(defendantEntity1, defendantEntity2));
        Assert.assertEquals(1, results.size());
    }

    @Test
    public void testFromJPAMapWithAssociatedDefenceOrganisation() {
        setupTestData();
        final DefenceOrganisation defenceOrganisation = new DefenceOrganisation();
        defenceOrganisation.setName("ABC LTD");
        defenceOrganisation.setLaaContractNumber("LAA123123");
        defenceOrganisation.setIncorporationNumber("INC001");
        defenceOrganisation.setRegisteredCharityNumber("Charity001");
        final AssociatedDefenceOrganisation associatedDefenceOrganisation = new AssociatedDefenceOrganisation();
        associatedDefenceOrganisation.setDefenceOrganisation(defenceOrganisation);

        defendantEntity.setAssociatedDefenceOrganisation(associatedDefenceOrganisation);
        final Defendant results = defendantJPAMapper.fromJPA(defendantEntity);
        assertNotNull(results.getAssociatedDefenceOrganisation());
        assertNotNull(results.getAssociatedDefenceOrganisation().getDefenceOrganisation());

        final uk.gov.justice.core.courts.DefenceOrganisation defenceOrganisationPojo = results.getAssociatedDefenceOrganisation().getDefenceOrganisation();
        Assert.assertEquals("ABC LTD", defenceOrganisationPojo.getOrganisation().getName());
        Assert.assertEquals("LAA123123", defenceOrganisationPojo.getLaaContractNumber());
    }

    private void setupTestData() {
        defendantEntity = new uk.gov.moj.cpp.hearing.persist.entity.ha.Defendant();
        hearingSnapshotKey = new HearingSnapshotKey();
        hearingSnapshotKey.setId(UUID.randomUUID());
        defendantEntity.setId(hearingSnapshotKey);
    }


}
