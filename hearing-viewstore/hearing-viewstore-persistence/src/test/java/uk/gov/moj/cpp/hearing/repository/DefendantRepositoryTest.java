package uk.gov.moj.cpp.hearing.repository;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import uk.gov.justice.core.courts.FundingType;
import uk.gov.justice.services.test.utils.persistence.HibernateTestEntityManagerProvider;
import uk.gov.moj.cpp.hearing.dto.DefendantSearch;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Address;
import uk.gov.moj.cpp.hearing.persist.entity.ha.AssociatedDefenceOrganisation;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Contact;
import uk.gov.moj.cpp.hearing.persist.entity.ha.DefenceOrganisation;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Defendant;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Organisation;

import java.time.LocalDate;
import java.util.UUID;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;


class DefendantRepositoryTest {

    private static final String PERSISTENCE_UNIT = "hearing-test-persistence-unit";

    @RegisterExtension
    static HibernateTestEntityManagerProvider hibernateTestEntityManagerProvider =
            new HibernateTestEntityManagerProvider(PERSISTENCE_UNIT);

    private static final UUID hearingId = UUID.randomUUID();
    private static final UUID id = UUID.randomUUID();

    private DefendantRepository defendantRepository;

    @BeforeEach
    void openEntityManagerAndCreateRepository() {
        defendantRepository = new DefendantRepository();
        hibernateTestEntityManagerProvider.injectEntityManagerInto(defendantRepository);
    }

    @Test
    void shouldPersistDefendant() {
        final Defendant defendant = buildDefendant();

        defendantRepository.save(defendant);
        final Defendant actual = defendantRepository.findBy(new HearingSnapshotKey(id, hearingId));

        assertThat(actual, notNullValue());
    }

    @Test
    void testDefendantDetailsForSearching() {
        final Defendant defendant1 = buildDefendant();
        final Defendant defendant2 = buildDefendant();
        defendant2.getId().setId(UUID.randomUUID());
        defendant2.getId().setHearingId(UUID.randomUUID());
        defendant2.getAssociatedDefenceOrganisation().getDefenceOrganisation().setLaaContractNumber("ABCLT2");

        defendantRepository.save(defendant1);
        defendantRepository.save(defendant2);

        DefendantSearch defendantDetailsForSearching = defendantRepository.getDefendantDetailsForSearching(defendant1.getId().getId());

        assertThat(defendantDetailsForSearching, notNullValue());
    }

    @Test
    void testDuplicateDefendantDetailsForSearching() {
        final Defendant defendant = buildDefendant();
        final Defendant defendantDupli = buildDefendant();
        defendantDupli.getId().setHearingId(UUID.randomUUID());
        defendantDupli.getAssociatedDefenceOrganisation().getDefenceOrganisation().setLaaContractNumber("ABCLT2");

        defendantRepository.save(defendant);
        defendantRepository.save(defendantDupli);

        DefendantSearch defendantDetailsForSearching = defendantRepository.getDefendantDetailsForSearching(defendant.getId().getId());

        assertThat(defendantDetailsForSearching, notNullValue());
    }

    @Test
    void testDefendantDetailsForSearchingNoResultFound() {
        assertThat(defendantRepository.getDefendantDetailsForSearching(UUID.randomUUID()), is(nullValue()));
    }

    private Defendant buildDefendant() {
        final Defendant defendant = new Defendant();
        HearingSnapshotKey key = new HearingSnapshotKey(id, hearingId);
        defendant.setId(key);
        defendant.setDefenceOrganisation(buildDefendantOrganisation());
        defendant.setAssociatedDefenceOrganisation(buildAssociatedDefenceOrganisation());
        defendant.setLegalEntityOrganisation(buildDefendantOrganisation());

        return defendant;
    }

    private Organisation buildDefendantOrganisation() {
        Organisation organisation = new Organisation();
        organisation.setId(UUID.randomUUID());
        organisation.setName("defendant organisation");
        organisation.setIncorporationNumber("INC-0001");
        final Address address = new Address();
        address.setAddress1("defendant Address 1");
        address.setAddress2("defendant Address 2");
        address.setAddress3("defendant Address 3");
        address.setAddress4("defendant Address 4");
        address.setAddress5("defendant Address 5");
        address.setPostCode("defendant Post Code");
        organisation.setAddress(address);

        final Contact contact = new Contact();
        contact.setFax("+442011201631");
        contact.setHome("+442011201632");
        contact.setMobile("07420201631");
        contact.setPrimaryEmail("test-def@test.com");
        contact.setPrimaryEmail("test1-def@test.com");
        organisation.setContact(contact);
        return organisation;
    }

    private AssociatedDefenceOrganisation buildAssociatedDefenceOrganisation() {
        final AssociatedDefenceOrganisation associatedDefenceOrganisation = new AssociatedDefenceOrganisation();
        associatedDefenceOrganisation.setAssociationEndDate(LocalDate.of(2019, 12, 23));
        associatedDefenceOrganisation.setAssociationStartDate(LocalDate.of(2019, 12, 13));
        associatedDefenceOrganisation.setAssociatedByLAA(true);
        associatedDefenceOrganisation.setFundingType(FundingType.REPRESENTATION_ORDER);
        associatedDefenceOrganisation.setApplicationReference("REF001");
        final DefenceOrganisation defenceOrganisation = new DefenceOrganisation();
        defenceOrganisation.setLaaContractNumber("ABCLTD");

        final Organisation organisation = new Organisation();
        organisation.setId(UUID.randomUUID());
        organisation.setName("org name");
        organisation.setIncorporationNumber("INC Number");
        organisation.setRegisteredCharityNumber("Charity007");

        final Address address = new Address();
        address.setAddress1("Address 1");
        address.setAddress2("Address 2");
        address.setAddress3("Address 3");
        address.setAddress4("Address 4");
        address.setAddress5("Address 5");
        address.setPostCode("Post Code");

        defenceOrganisation.setAddress(address);

        final Contact contact = new Contact();
        contact.setFax("+442011201630");
        contact.setHome("+442011201631");
        contact.setMobile("07420201630");
        contact.setPrimaryEmail("test@test.com");
        contact.setPrimaryEmail("test1@test.com");
        defenceOrganisation.setContact(contact);

        associatedDefenceOrganisation.setDefenceOrganisation(defenceOrganisation);
        return associatedDefenceOrganisation;
    }
}
