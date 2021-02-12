package uk.gov.moj.cpp.hearing.event.listener;

import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.defendantTemplate;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;

import uk.gov.justice.core.courts.FundingType;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.moj.cpp.hearing.domain.event.DefendantDetailsUpdated;
import uk.gov.moj.cpp.hearing.mapping.AddressJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.AssociatedDefenceOrganisationJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.ContactNumberJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.CourtApplicationsSerializer;
import uk.gov.moj.cpp.hearing.mapping.CustodialEstablishmentJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.OrganisationJPAMapper;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Address;
import uk.gov.moj.cpp.hearing.persist.entity.ha.AssociatedDefenceOrganisation;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Contact;
import uk.gov.moj.cpp.hearing.persist.entity.ha.CustodialEstablishment;
import uk.gov.moj.cpp.hearing.persist.entity.ha.DefenceOrganisation;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Defendant;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Organisation;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Person;
import uk.gov.moj.cpp.hearing.persist.entity.ha.PersonDefendant;
import uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCase;
import uk.gov.moj.cpp.hearing.repository.DefendantRepository;
import uk.gov.moj.cpp.hearing.repository.HearingRepository;

import java.time.LocalDate;
import java.util.UUID;

import javax.json.JsonObject;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CaseDefendantDetailsUpdatedEventListenerTest {

    @Mock
    HearingRepository hearingRepository;
    @Spy
    CourtApplicationsSerializer courtApplicationsSerializer;
    @InjectMocks
    private CaseDefendantDetailsUpdatedEventListener caseDefendantDetailsUpdatedEventListener;
    @Mock
    private DefendantRepository defendantRepository;
    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;
    @Spy
    private OrganisationJPAMapper organisationJPAMapper;

    @Mock
    private AssociatedDefenceOrganisationJPAMapper associatedDefenceOrganisationJPAMapper;

    @Before
    public void setup() {
        setField(this.jsonObjectToObjectConverter, "objectMapper", new ObjectMapperProducer().objectMapper());
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
        setField(this.courtApplicationsSerializer, "jsonObjectToObjectConverter", jsonObjectToObjectConverter);
        setField(this.courtApplicationsSerializer, "objectToJsonObjectConverter", objectToJsonObjectConverter);
        setField(this.caseDefendantDetailsUpdatedEventListener, "custodialEstablishmentJPAMapper", new CustodialEstablishmentJPAMapper());
        setField(this.organisationJPAMapper, "addressJPAMapper", new AddressJPAMapper());
        setField(this.organisationJPAMapper, "contactNumberJPAMapper", new ContactNumberJPAMapper());
    }

    @Test
    public void shouldUpdateDefendant() {

        final UUID hearingId = randomUUID();

        final DefendantDetailsUpdated defendantDetailsUpdated = DefendantDetailsUpdated.defendantDetailsUpdated()
                .setHearingId(hearingId)
                .setDefendant(defendantTemplate());

        final Hearing hearing = new Hearing();
        hearing.setId(hearingId);

        final JsonEnvelope envelope = createJsonEnvelope(defendantDetailsUpdated);

        final Defendant defendant = getDefendant(hearingId, defendantDetailsUpdated);

        when(defendantRepository.findBy(defendant.getId())).thenReturn(defendant);

        when(hearingRepository.findBy(hearingId)).thenReturn(hearing);

        when(associatedDefenceOrganisationJPAMapper.toJPA(any(uk.gov.justice.core.courts.AssociatedDefenceOrganisation.class)))
                .thenReturn(getAssociatedDefenceOrganisation());

        caseDefendantDetailsUpdatedEventListener.defendantDetailsUpdated(envelope);

        final ArgumentCaptor<Defendant> defendantexArgumentCaptor = ArgumentCaptor.forClass(Defendant.class);

        verify(defendantRepository).save(defendantexArgumentCaptor.capture());

        assertThat(defendantexArgumentCaptor.getValue(), isBean(Defendant.class)
                .with(defendant1 -> defendant.getId().getId(), is(defendantDetailsUpdated.getDefendant().getId()))
                .with(defendant1 -> defendant.getMasterDefendantId(), is(defendantDetailsUpdated.getDefendant().getMasterDefendantId()))
                .with(Defendant::getPersonDefendant, isBean(PersonDefendant.class)
                        .with(PersonDefendant::getCustodialEstablishment, isBean(CustodialEstablishment.class)
                                .with(CustodialEstablishment::getCustody, is(defendantDetailsUpdated.getDefendant().getPersonDefendant().getCustodialEstablishment().getCustody()))
                                .with(CustodialEstablishment::getId, is(defendantDetailsUpdated.getDefendant().getPersonDefendant().getCustodialEstablishment().getId()))
                                .with(CustodialEstablishment::getName, is(defendantDetailsUpdated.getDefendant().getPersonDefendant().getCustodialEstablishment().getName()))

                        )
                        .with(PersonDefendant::getPersonDetails, isBean(Person.class)
                                .with(Person::getNationalityCode, is(defendantDetailsUpdated.getDefendant().getPersonDefendant().getPersonDetails().getNationalityCode()))
                                .with(Person::getNationalityId, is(defendantDetailsUpdated.getDefendant().getPersonDefendant().getPersonDetails().getNationalityId()))
                                .with(Person::getNationalityDescription, is(defendantDetailsUpdated.getDefendant().getPersonDefendant().getPersonDetails().getNationalityDescription()))
                                .with(Person::getAdditionalNationalityCode, is(defendantDetailsUpdated.getDefendant().getPersonDefendant().getPersonDetails().getAdditionalNationalityCode()))
                                .with(Person::getAdditionalNationalityId, is(defendantDetailsUpdated.getDefendant().getPersonDefendant().getPersonDetails().getAdditionalNationalityId()))
                        )
                        .with(PersonDefendant::getEmployerOrganisation, isBean(Organisation.class)
                                .with(Organisation::getIncorporationNumber, is(defendantDetailsUpdated.getDefendant().getPersonDefendant().getEmployerOrganisation().getIncorporationNumber()))
                                .with(Organisation::getRegisteredCharityNumber, is(defendantDetailsUpdated.getDefendant().getPersonDefendant().getEmployerOrganisation().getRegisteredCharityNumber()))
                                .with(Organisation::getName, is(defendantDetailsUpdated.getDefendant().getPersonDefendant().getEmployerOrganisation().getName()))
                                .with(Organisation::getAddress, isBean(Address.class)
                                        .with(Address::getAddress1, is(defendantDetailsUpdated.getDefendant().getPersonDefendant().getEmployerOrganisation().getAddress().getAddress1()))
                                        .with(Address::getAddress2, is(defendantDetailsUpdated.getDefendant().getPersonDefendant().getEmployerOrganisation().getAddress().getAddress2()))
                                        .with(Address::getAddress3, is(defendantDetailsUpdated.getDefendant().getPersonDefendant().getEmployerOrganisation().getAddress().getAddress3()))
                                        .with(Address::getAddress4, is(defendantDetailsUpdated.getDefendant().getPersonDefendant().getEmployerOrganisation().getAddress().getAddress4()))
                                        .with(Address::getAddress5, is(defendantDetailsUpdated.getDefendant().getPersonDefendant().getEmployerOrganisation().getAddress().getAddress5()))
                                        .with(Address::getPostCode, is(defendantDetailsUpdated.getDefendant().getPersonDefendant().getEmployerOrganisation().getAddress().getPostcode()))
                                )
                                .with(Organisation::getContact, isBean(Contact.class)
                                        .with(Contact::getFax, is(defendantDetailsUpdated.getDefendant().getPersonDefendant().getEmployerOrganisation().getContact().getFax()))
                                        .with(Contact::getHome, is(defendantDetailsUpdated.getDefendant().getPersonDefendant().getEmployerOrganisation().getContact().getHome()))
                                        .with(Contact::getMobile, is(defendantDetailsUpdated.getDefendant().getPersonDefendant().getEmployerOrganisation().getContact().getMobile()))
                                        .with(Contact::getWork, is(defendantDetailsUpdated.getDefendant().getPersonDefendant().getEmployerOrganisation().getContact().getWork()))
                                        .with(Contact::getPrimaryEmail, is(defendantDetailsUpdated.getDefendant().getPersonDefendant().getEmployerOrganisation().getContact().getPrimaryEmail()))
                                        .with(Contact::getSecondaryEmail, is(defendantDetailsUpdated.getDefendant().getPersonDefendant().getEmployerOrganisation().getContact().getSecondaryEmail()))
                                )
                        )

                )
                .with(Defendant::getDefenceOrganisation, isBean(Organisation.class))
                .with(Defendant::getLegalEntityOrganisation, isBean(Organisation.class))
        );
        assertAssociatedDefenceOrganisation(defendant, defendantexArgumentCaptor);
    }

    @Test
    public void shouldUpdateDefendantWithoutMasterDefendantId() {

        final UUID hearingId = randomUUID();

        final uk.gov.moj.cpp.hearing.command.defendant.Defendant defendantTemplate = defendantTemplate();
        defendantTemplate.setMasterDefendantId(null);
        final DefendantDetailsUpdated defendantDetailsUpdated = DefendantDetailsUpdated.defendantDetailsUpdated()
                .setHearingId(hearingId)
                .setDefendant(defendantTemplate);

        final Hearing hearing = new Hearing();
        hearing.setId(hearingId);


        final Defendant defendant = getDefendant(hearingId, defendantDetailsUpdated);
        final UUID masterDefendantId = randomUUID();
        defendant.setMasterDefendantId(masterDefendantId);

        final JsonEnvelope envelope = createJsonEnvelope(defendantDetailsUpdated);

        when(defendantRepository.findBy(defendant.getId())).thenReturn(defendant);

        when(hearingRepository.findBy(hearingId)).thenReturn(hearing);

        when(associatedDefenceOrganisationJPAMapper.toJPA(any(uk.gov.justice.core.courts.AssociatedDefenceOrganisation.class)))
                .thenReturn(getAssociatedDefenceOrganisation());

        caseDefendantDetailsUpdatedEventListener.defendantDetailsUpdated(envelope);

        final ArgumentCaptor<Defendant> defendantexArgumentCaptor = ArgumentCaptor.forClass(Defendant.class);

        verify(defendantRepository).save(defendantexArgumentCaptor.capture());

        assertThat(defendantexArgumentCaptor.getValue(), isBean(Defendant.class)
                .with(defendant1 -> defendant.getId().getId(), is(defendantDetailsUpdated.getDefendant().getId()))
                .with(defendant1 -> defendant.getMasterDefendantId(), is(masterDefendantId))
                .with(Defendant::getPersonDefendant, isBean(PersonDefendant.class)
                        .with(PersonDefendant::getCustodialEstablishment, isBean(CustodialEstablishment.class)
                                .with(CustodialEstablishment::getCustody, is(defendantDetailsUpdated.getDefendant().getPersonDefendant().getCustodialEstablishment().getCustody()))
                                .with(CustodialEstablishment::getId, is(defendantDetailsUpdated.getDefendant().getPersonDefendant().getCustodialEstablishment().getId()))
                                .with(CustodialEstablishment::getName, is(defendantDetailsUpdated.getDefendant().getPersonDefendant().getCustodialEstablishment().getName()))

                        )
                        .with(PersonDefendant::getPersonDetails, isBean(Person.class)
                                .with(Person::getNationalityCode, is(defendantDetailsUpdated.getDefendant().getPersonDefendant().getPersonDetails().getNationalityCode()))
                                .with(Person::getNationalityId, is(defendantDetailsUpdated.getDefendant().getPersonDefendant().getPersonDetails().getNationalityId()))
                                .with(Person::getNationalityDescription, is(defendantDetailsUpdated.getDefendant().getPersonDefendant().getPersonDetails().getNationalityDescription()))
                                .with(Person::getAdditionalNationalityCode, is(defendantDetailsUpdated.getDefendant().getPersonDefendant().getPersonDetails().getAdditionalNationalityCode()))
                                .with(Person::getAdditionalNationalityId, is(defendantDetailsUpdated.getDefendant().getPersonDefendant().getPersonDetails().getAdditionalNationalityId()))
                        )

                )
        );
        assertAssociatedDefenceOrganisation(defendant, defendantexArgumentCaptor);
    }

    private AssociatedDefenceOrganisation getAssociatedDefenceOrganisation() {
        final AssociatedDefenceOrganisation associatedDefenceOrganisation = new AssociatedDefenceOrganisation();
        associatedDefenceOrganisation.setApplicationReference("application-reference");
        associatedDefenceOrganisation.setFundingType(FundingType.REPRESENTATION_ORDER);
        associatedDefenceOrganisation.setAssociationStartDate(LocalDate.of(2019, 12, 13));
        associatedDefenceOrganisation.setAssociationEndDate(LocalDate.of(2020, 12, 12));
        associatedDefenceOrganisation.setAssociatedByLAA(true);
        final DefenceOrganisation defenceOrganisation = new DefenceOrganisation();
        defenceOrganisation.setLaaContractNumber("LAA44569");
        defenceOrganisation.setName("Test");
        defenceOrganisation.setRegisteredCharityNumber("Reg-001");
        defenceOrganisation.setIncorporationNumber("Inc-0001");
        final Address address = new Address();
        address.setAddress1("address1");
        address.setAddress2("address2");
        address.setAddress3("address3");
        address.setAddress4("address4");
        address.setAddress5("address5");
        address.setPostCode("postCode");
        defenceOrganisation.setAddress(address);

        final Contact contact = new Contact();
        contact.setPrimaryEmail("test.test@test.com");
        contact.setSecondaryEmail("test-sec@test.com");
        contact.setMobile("07411201778");
        contact.setHome("02088888888");
        contact.setWork("02077777777");
        contact.setFax("020555555555");

        defenceOrganisation.setContact(contact);
        associatedDefenceOrganisation.setDefenceOrganisation(defenceOrganisation);
        return associatedDefenceOrganisation;
    }

    private Defendant getDefendant(final UUID hearingId, final DefendantDetailsUpdated defendantDetailsUpdated) {
        final ProsecutionCase prosecutionCase = new ProsecutionCase();
        prosecutionCase.setId(new HearingSnapshotKey(defendantDetailsUpdated.getDefendant().getProsecutionCaseId(), hearingId));

        final Defendant defendant = new Defendant();
        defendant.setProsecutionCase(prosecutionCase);
        defendant.setId(new HearingSnapshotKey(defendantDetailsUpdated.getDefendant().getId(), hearingId));
        defendant.setMasterDefendantId(defendantDetailsUpdated.getDefendant().getMasterDefendantId());
        final PersonDefendant personDefendant = new PersonDefendant();
        personDefendant.setPersonDetails(new Person());

        defendant.setPersonDefendant(personDefendant);
        return defendant;
    }

    private void assertAssociatedDefenceOrganisation(final Defendant defendant, final ArgumentCaptor<Defendant> defendantArgumentCaptor) {
        final Defendant defendantOut = defendantArgumentCaptor.getValue();
        assertThat(defendant.getId(), is(defendantOut.getId()));
        assertThat(defendantOut.getAssociatedDefenceOrganisation().getAssociatedByLAA(), is(true));
        assertThat(defendantOut.getAssociatedDefenceOrganisation().getApplicationReference(), is("application-reference"));

        final DefenceOrganisation defenceOrganisationResulted = defendantOut.getAssociatedDefenceOrganisation().getDefenceOrganisation();
        assertThat(defenceOrganisationResulted, notNullValue());
        assertThat(defenceOrganisationResulted.getLaaContractNumber(), is("LAA44569"));
        assertThat(defenceOrganisationResulted.getName(), is("Test"));
        assertThat(defendantOut.getAssociatedDefenceOrganisation().getAssociatedByLAA(), is(true));
        assertThat(defenceOrganisationResulted.getRegisteredCharityNumber(), is("Reg-001"));
        assertThat(defenceOrganisationResulted.getIncorporationNumber(), is("Inc-0001"));

        assertThat(defenceOrganisationResulted.getAddress().getAddress1(), is("address1"));
        assertThat(defenceOrganisationResulted.getAddress().getAddress2(), is("address2"));
        assertThat(defenceOrganisationResulted.getAddress().getAddress3(), is("address3"));
        assertThat(defenceOrganisationResulted.getAddress().getAddress4(), is("address4"));
        assertThat(defenceOrganisationResulted.getAddress().getAddress5(), is("address5"));
        assertThat(defenceOrganisationResulted.getAddress().getPostCode(), is("postCode"));

        assertThat(defenceOrganisationResulted.getContact().getPrimaryEmail(), is("test.test@test.com"));
        assertThat(defenceOrganisationResulted.getContact().getSecondaryEmail(), is("test-sec@test.com"));
        assertThat(defenceOrganisationResulted.getContact().getWork(), is("02077777777"));
        assertThat(defenceOrganisationResulted.getContact().getMobile(), is("07411201778"));
        assertThat(defenceOrganisationResulted.getContact().getFax(), is("020555555555"));
    }

    private JsonEnvelope createJsonEnvelope(final DefendantDetailsUpdated defendantDetailsUpdated) {

        final JsonObject jsonObject = objectToJsonObjectConverter.convert(defendantDetailsUpdated);

        return envelopeFrom((Metadata) null, jsonObject);
    }

}
