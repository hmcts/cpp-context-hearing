package uk.gov.moj.cpp.hearing.query.view.converter;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;
import static uk.gov.moj.cpp.hearing.common.ReusableInformationConverterType.ADDRESS;
import static uk.gov.moj.cpp.hearing.common.ReusableInformationConverterType.FIXL;
import static uk.gov.moj.cpp.hearing.common.ReusableInformationConverterType.FIXLM;
import static uk.gov.moj.cpp.hearing.common.ReusableInformationConverterType.INT;
import static uk.gov.moj.cpp.hearing.common.ReusableInformationConverterType.NAMEADDRESS;
import static uk.gov.moj.cpp.hearing.common.ReusableInformationConverterType.TXT;

import uk.gov.justice.core.courts.Address;
import uk.gov.justice.core.courts.AssociatedPerson;
import uk.gov.justice.core.courts.ContactNumber;
import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.Person;
import uk.gov.justice.core.courts.PersonDefendant;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.justice.core.courts.ProsecutionCaseIdentifier;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.test.utils.framework.api.JsonObjectConvertersFactory;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt;
import uk.gov.moj.cpp.hearing.query.view.convertor.CustomReusableInfoConverter;
import uk.gov.moj.cpp.hearing.query.view.convertor.ReusableInformationFixlConverter;
import uk.gov.moj.cpp.hearing.query.view.convertor.ReusableInformationFixlmConverter;
import uk.gov.moj.cpp.hearing.query.view.convertor.ReusableInformationFixlomConverter;
import uk.gov.moj.cpp.hearing.query.view.convertor.ReusableInformationIntConverter;
import uk.gov.moj.cpp.hearing.query.view.convertor.ReusableInformationMainConverter;
import uk.gov.moj.cpp.hearing.query.view.convertor.ReusableInformationObjectTypeConverter;
import uk.gov.moj.cpp.hearing.query.view.convertor.ReusableInformationTxtConverter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.json.JsonObject;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ReusableInformationMainConverterTest {

    @InjectMocks
    private ReusableInformationMainConverter reusableInformationMainConverter;

    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter = new JsonObjectConvertersFactory().objectToJsonObjectConverter();

    @Spy
    private CustomReusableInfoConverter customReusableInfoConverter = new CustomReusableInfoConverter();

    private static final String DELIMITER = "###";

    private static final String PROMPT_REF = "promptRef";

    private static final String TYPE = "type";

    @Before
    public void setUp() {
        setField(this.reusableInformationMainConverter, "reusableInformationTxtConverter", new ReusableInformationTxtConverter());
        setField(this.reusableInformationMainConverter, "reusableInformationIntConverter", new ReusableInformationIntConverter());
        setField(this.reusableInformationMainConverter, "reusableInformationFixlConverter", new ReusableInformationFixlConverter());
        setField(this.reusableInformationMainConverter, "reusableInformationFixlmConverter", new ReusableInformationFixlmConverter());
        setField(this.reusableInformationMainConverter, "reusableInformationFixlomConverter", new ReusableInformationFixlomConverter());

        final ReusableInformationObjectTypeConverter reusableInformationObjectTypeConverter = new ReusableInformationObjectTypeConverter();
        setField(reusableInformationObjectTypeConverter, "objectToJsonObjectConverter", objectToJsonObjectConverter);
        setField(this.reusableInformationMainConverter, "reusableInformationObjectTypeConverter", reusableInformationObjectTypeConverter);
    }

    @Test
    public void shouldConvertForDefendant() {
        final List<Defendant> defendants = Collections.unmodifiableList(Arrays.asList(prepareDefendant()));
        final List<Prompt> prompts = new ArrayList<>();
        prompts.addAll(prepareTxtPromptsWithCommaSeperated());
        prompts.addAll(prepareTxtPrompts());
        prompts.addAll(prepareAddressPrompts());
        prompts.addAll(prepareFixlmPrompts());
        prompts.addAll(prepareIntPrompts());
        prompts.addAll(prepareFixlPrompts());

        final Map<Defendant, List<JsonObject>> defendantListMap = reusableInformationMainConverter.convertDefendant(defendants, prompts, Collections.emptyMap());

        assertNotNull(defendantListMap);

        assertThat(defendantListMap.size(), is(defendants.size()));

        final List<JsonObject> promptJsonObjects = defendantListMap.get(defendants.get(0));

        final Optional<AssociatedPerson> associatedPerson = defendants.get(0).getAssociatedPersons()
                .stream()
                .filter(person -> person.getRole().equals("ParentGuardian"))
                .findAny();

        final Person person = defendants.get(0).getPersonDefendant().getPersonDetails();

        assertIntType(defendants.get(0), promptJsonObjects);
        assertTxtType(defendants.get(0), prompts, promptJsonObjects, associatedPerson);
        assertTxtTypeWithCommaSeperated(defendants.get(0), prompts, promptJsonObjects, associatedPerson);
        assertAddressType(defendants.get(0), promptJsonObjects, associatedPerson);
        assertFixlType(defendants.get(0), promptJsonObjects);
        assertFixlmType(defendants.get(0), promptJsonObjects, person);
    }

    @Test
    public void shouldConvertForCase() {
        final List<ProsecutionCase> cases = singletonList(prepareCase());
        final List<Prompt> prompts = new ArrayList<>();
        prompts.addAll(prepareCasePrompts());
        prompts.addAll(prepareTxtPromptsWithCommaSeperated());
        prompts.addAll(prepareTxtPrompts());

        final Map<ProsecutionCase, List<JsonObject>> caseListMap = reusableInformationMainConverter.convertCase(cases, prompts, Collections.emptyMap());

        assertNotNull(caseListMap);

        assertThat(caseListMap.size(), is(cases.size()));

        final List<JsonObject> promptJsonObjects = caseListMap.get(cases.get(0));

        assertThat(promptJsonObjects.get(0).getString("promptRef"), is("minorcreditornameandaddress"));
        assertThat(promptJsonObjects.get(0).getString("type"), is("NAMEADDRESS"));
        assertThat(promptJsonObjects.get(0).getJsonObject("value").getString("minorcreditornameandaddressOrganisationName"), is("AuthorityName"));
        assertThat(promptJsonObjects.get(0).getJsonObject("value").getString("minorcreditornameandaddressAddress1"), is("line 1"));
        assertThat(promptJsonObjects.get(0).getJsonObject("value").get("minorcreditornameandaddressAddress2"), nullValue());

        assertThat(promptJsonObjects.get(1).getString("promptRef"), is("parentguardiansname"));
        assertThat(promptJsonObjects.get(1).getString("value"), is(""));

        assertThat(promptJsonObjects.get(2).getString("promptRef"), is("defendantDrivingLicenceNumber"));
        assertThat(promptJsonObjects.get(2).getString("value"), is(""));
    }

    private void assertFixlType(final Defendant defendant, final List<JsonObject> promptJsonObjects) {
        final JsonObject prosecutionAuthorityReferencePromptJsonObject = promptJsonObjects.stream()
                .filter(jsonObject -> jsonObject.getString(PROMPT_REF).equals("prosecutionAuthorityReference"))
                .findAny().get();

        assertThat("prosecutionAuthorityReference", is(prosecutionAuthorityReferencePromptJsonObject.getString(PROMPT_REF)));
        assertThat(FIXL.name(), is(prosecutionAuthorityReferencePromptJsonObject.getString(TYPE)));
        assertThat(defendant.getMasterDefendantId().toString(), is(prosecutionAuthorityReferencePromptJsonObject.getString("masterDefendantId")));
        assertThat(defendant.getProsecutionAuthorityReference(), is(prosecutionAuthorityReferencePromptJsonObject.getString("value")));
    }

    private void assertIntType(final Defendant defendant, final List<JsonObject> promptJsonObjects) {
        final JsonObject numberOfPreviousConvictionsCitedPromptJsonObject = promptJsonObjects.stream()
                .filter(jsonObject -> jsonObject.getString(PROMPT_REF).equals("numberOfPreviousConvictionsCited"))
                .findAny().get();

        assertThat("numberOfPreviousConvictionsCited", is(numberOfPreviousConvictionsCitedPromptJsonObject.getString(PROMPT_REF)));
        assertThat(INT.name(), is(numberOfPreviousConvictionsCitedPromptJsonObject.getString(TYPE)));
        assertThat(defendant.getMasterDefendantId().toString(), is(numberOfPreviousConvictionsCitedPromptJsonObject.getString("masterDefendantId")));
        assertThat(defendant.getNumberOfPreviousConvictionsCited().toString(), is(numberOfPreviousConvictionsCitedPromptJsonObject.getString("value")));
    }

    private void assertFixlmType(final Defendant defendant, final List<JsonObject> promptJsonObjects, final Person person) {
        final JsonObject nationalityPromptJsonObject = promptJsonObjects.stream()
                .filter(jsonObject -> jsonObject.getString(PROMPT_REF).equals("nationality"))
                .findAny().get();

        assertThat("nationality", is(nationalityPromptJsonObject.getString(PROMPT_REF)));
        assertThat(FIXLM.name(), is(nationalityPromptJsonObject.getString(TYPE)));
        assertThat(defendant.getMasterDefendantId().toString(), is(nationalityPromptJsonObject.getString("masterDefendantId")));
        assertThat(person.getNationalityCode() + DELIMITER + person.getAdditionalNationalityCode(), is(nationalityPromptJsonObject.getString("value")));
    }

    private void assertTxtTypeWithCommaSeperated(final Defendant defendant, final List<Prompt> prompts, final List<JsonObject> promptJsonObjects, final Optional<AssociatedPerson> associatedPerson) {
        final JsonObject parentGuardiansNameJsonObject = promptJsonObjects.stream()
                .filter(jsonObject -> jsonObject.getString(PROMPT_REF).equals("parentguardiansname"))
                .findAny().get();

        final Prompt prompt = prompts.stream().filter(promptToFilter -> promptToFilter.getReference().equals("parentguardiansname")).findAny().get();

        assertThat(prompt.getReference(), is(parentGuardiansNameJsonObject.getString(PROMPT_REF)));
        assertThat(prompt.getType(), is(parentGuardiansNameJsonObject.getString(TYPE)));
        assertThat(defendant.getMasterDefendantId().toString(), is(parentGuardiansNameJsonObject.getString("masterDefendantId")));
        assertThat("Matthew Thompson" , is(parentGuardiansNameJsonObject.getString("value")));
    }

    private void assertTxtType(final Defendant defendant, final List<Prompt> prompts, final List<JsonObject> promptJsonObjects, final Optional<AssociatedPerson> associatedPerson) {
        final JsonObject drivingLicenceNumberJsonObject = promptJsonObjects.stream()
                .filter(jsonObject -> jsonObject.getString(PROMPT_REF).equals("defendantDrivingLicenceNumber"))
                .findAny().get();

        final Prompt prompt = prompts.stream().filter(promptToFilter -> promptToFilter.getReference().equals("defendantDrivingLicenceNumber")).findAny().get();

        assertThat(prompt.getReference(), is(drivingLicenceNumberJsonObject.getString(PROMPT_REF)));
        assertThat(prompt.getType(), is(drivingLicenceNumberJsonObject.getString(TYPE)));
        assertThat(defendant.getMasterDefendantId().toString(), is(drivingLicenceNumberJsonObject.getString("masterDefendantId")));
        assertThat("MORGA657054SM9BF" , is(drivingLicenceNumberJsonObject.getString("value")));
    }

    private void assertAddressType(final Defendant defendant, final List<JsonObject> promptJsonObjects, final Optional<AssociatedPerson> associatedPerson) {
        final JsonObject addressPromptJsonObject = promptJsonObjects.stream()
                .filter(jsonObject -> jsonObject.getString(PROMPT_REF).equals("parentguardiansaddressAddress1"))
                .findAny().get();
        assertThat("parentguardiansaddressAddress1", is(addressPromptJsonObject.getString(PROMPT_REF)));
        assertThat(ADDRESS.name(), is(addressPromptJsonObject.getString(TYPE)));
        assertThat(defendant.getMasterDefendantId().toString(), is(addressPromptJsonObject.getString("masterDefendantId")));


        final JsonObject addressPromptValue = addressPromptJsonObject.getJsonObject("value");
        assertThat(associatedPerson.get().getPerson().getAddress().getAddress1(), is(addressPromptValue.getString("parentguardiansaddressAddress1")));
        assertThat(associatedPerson.get().getPerson().getAddress().getAddress2(), is(addressPromptValue.getString("parentguardiansaddressAddress2")));
        assertThat(associatedPerson.get().getPerson().getAddress().getAddress3(), is(addressPromptValue.getString("parentguardiansaddressAddress3")));
        assertThat(associatedPerson.get().getPerson().getAddress().getAddress4(), is(addressPromptValue.getString("parentguardiansaddressAddress4")));
        assertThat(associatedPerson.get().getPerson().getAddress().getAddress5(), is(addressPromptValue.getString("parentguardiansaddressAddress5")));
        assertThat(associatedPerson.get().getPerson().getAddress().getPostcode(), is(addressPromptValue.getString("parentguardiansaddressPostCode")));
        assertThat(associatedPerson.get().getPerson().getContact().getPrimaryEmail(), is(addressPromptValue.getString("parentguardiansaddressEmailAddress1")));
        assertNull(associatedPerson.get().getPerson().getContact().getSecondaryEmail());
    }

    private List<Prompt> prepareTxtPrompts() {
        return Collections.unmodifiableList(Arrays.asList(new Prompt()
                .setId(UUID.randomUUID())
                .setType(TXT.name())
                .setCacheable(2)
                .setCacheDataPath("personDefendant.driverNumber")
                .setReference("defendantDrivingLicenceNumber")));
    }

    private List<Prompt> prepareTxtPromptsWithCommaSeperated() {
        return Collections.unmodifiableList(Arrays.asList(new Prompt()
                .setId(UUID.randomUUID())
                .setType(TXT.name())
                .setCacheable(2)
                .setCacheDataPath("associatedPersons[?(@.role=='ParentGuardian')].person.firstName;associatedPersons[?(@.role=='ParentGuardian')].person.middleName;associatedPersons[?(@.role=='ParentGuardian')].person.lastName")
                .setReference("parentguardiansname")));
    }

    private List<Prompt> prepareIntPrompts() {
        return Collections.unmodifiableList(Arrays.asList(new Prompt()
                .setId(UUID.randomUUID())
                .setType(INT.name())
                .setCacheable(2)
                .setCacheDataPath("numberOfPreviousConvictionsCited")
                .setReference("numberOfPreviousConvictionsCited")));
    }

    private List<Prompt> prepareFixlmPrompts() {
        return Collections.unmodifiableList(Arrays.asList(new Prompt()
                .setId(UUID.randomUUID())
                .setType(FIXLM.name())
                .setCacheable(2)
                .setCacheDataPath("personDefendant.personDetails.nationalityCode;personDefendant.personDetails.additionalNationalityCode")
                .setReference("nationality")));
    }

    private List<Prompt> prepareFixlPrompts() {
        return Collections.unmodifiableList(Arrays.asList(new Prompt()
                .setId(UUID.randomUUID())
                .setType(FIXL.name())
                .setCacheable(2)
                .setCacheDataPath("prosecutionAuthorityReference")
                .setReference("prosecutionAuthorityReference")));
    }

    private List<Prompt> prepareAddressPrompts() {
        final UUID promptId = UUID.randomUUID();
        final Prompt promptForParentGuardiansAddressAddress1 = new Prompt()
                .setId(promptId)
                .setType(ADDRESS.name())
                .setReference("parentguardiansaddressAddress1")
                .setCacheable(2)
                .setCacheDataPath("associatedPersons[?(@.role=='ParentGuardian' || @.role=='PARENT')].person.address.address1");

        final Prompt promptForParentGuardiansAddressAddress2 = new Prompt()
                .setId(promptId)
                .setType(ADDRESS.name())
                .setReference("parentguardiansaddressAddress2")
                .setCacheable(2)
                .setCacheDataPath("associatedPersons[?(@.role=='ParentGuardian' || @.role=='PARENT')].person.address.address2");


        final Prompt promptForParentGuardiansAddressAddress3 = new Prompt()
                .setId(promptId)
                .setType(ADDRESS.name())
                .setReference("parentguardiansaddressAddress3")
                .setCacheable(2)
                .setCacheDataPath("associatedPersons[?(@.role=='ParentGuardian' || @.role=='PARENT')].person.address.address3");

        final Prompt promptForParentGuardiansAddressAddress4 = new Prompt()
                .setId(promptId)
                .setType(ADDRESS.name())
                .setReference("parentguardiansaddressAddress4")
                .setCacheable(2)
                .setCacheDataPath("associatedPersons[?(@.role=='ParentGuardian' || @.role=='PARENT')].person.address.address4");

        final Prompt promptForParentGuardiansAddressAddress5 = new Prompt()
                .setId(promptId)
                .setType(ADDRESS.name())
                .setReference("parentguardiansaddressAddress5")
                .setCacheable(2)
                .setCacheDataPath("associatedPersons[?(@.role=='ParentGuardian' || @.role=='PARENT')].person.address.address5");

        final Prompt promptForParentGuardiansAddressPostCode = new Prompt()
                .setId(promptId)
                .setType(ADDRESS.name())
                .setReference("parentguardiansaddressPostCode")
                .setCacheable(2)
                .setCacheDataPath("associatedPersons[?(@.role=='ParentGuardian' || @.role=='PARENT')].person.address.postcode");

        final Prompt promptForParentGuardiansAddressEmailAddress1 = new Prompt()
                .setId(promptId)
                .setType(ADDRESS.name())
                .setReference("parentguardiansaddressEmailAddress1")
                .setCacheable(2)
                .setCacheDataPath("associatedPersons[?(@.role=='ParentGuardian' || @.role=='PARENT')].person.contact.primaryEmail");

        final Prompt promptForParentGuardiansAddressEmailAddress2 = new Prompt()
                .setId(promptId)
                .setType(ADDRESS.name())
                .setReference("parentguardiansaddressEmailAddress2")
                .setCacheable(2)
                .setCacheDataPath("associatedPersons[?(@.role=='ParentGuardian' || @.role=='PARENT')].person.contact.secondaryEmail");

        return Arrays.asList(promptForParentGuardiansAddressAddress1,
                promptForParentGuardiansAddressAddress2,
                promptForParentGuardiansAddressAddress3,
                promptForParentGuardiansAddressAddress4,
                promptForParentGuardiansAddressAddress5,
                promptForParentGuardiansAddressPostCode,
                promptForParentGuardiansAddressEmailAddress1,
                promptForParentGuardiansAddressEmailAddress2);
    }

    private Collection<? extends Prompt> prepareCasePrompts() {
        final UUID id = UUID.randomUUID();
        final Prompt name = new Prompt()
                .setId(id)
                .setType(NAMEADDRESS.name())
                .setCacheable(2)
                .setCacheDataPath("prosecutionCaseIdentifier.prosecutionAuthorityName")
                .setReference("minorcreditornameandaddressOrganisationName")
                .setPartName("OrganisationName");

        final Prompt addresLine1 = new Prompt()
                .setId(id)
                .setType(NAMEADDRESS.name())
                .setCacheable(2)
                .setCacheDataPath("prosecutionCaseIdentifier.address.address1")
                .setReference("minorcreditornameandaddressAddress1")
                .setPartName("Address1");

        final Prompt addresLine2 = new Prompt()
                .setId(id)
                .setType(NAMEADDRESS.name())
                .setCacheable(2)
                .setCacheDataPath("prosecutionCaseIdentifier.address.address2")
                .setReference("minorcreditornameandaddressAddress2")
                .setPartName("Address2");

        return Arrays.asList(name, addresLine1, addresLine2);
    }

    public Defendant prepareDefendant() {
        final Defendant defendant = Defendant.defendant()
                .withMasterDefendantId(UUID.randomUUID())
                .withNumberOfPreviousConvictionsCited(3)
                .withProsecutionAuthorityReference("REF1").build();

        final Person personDetails = Person.person()
                .withNationalityCode("350")
                .withAdditionalNationalityCode("460").build();
        final PersonDefendant personDefendant = PersonDefendant.personDefendant()
                .withDriverNumber("MORGA657054SM9BF")
                .withPersonDetails(personDetails).build();

        final List<AssociatedPerson> associatedPersons = new ArrayList<>();

        final Address address = Address.address()
                .withAddress1("address 1")
                .withAddress2("address 2")
                .withAddress3("address 3")
                .withAddress4("address 4")
                .withAddress5("address 5")
                .withPostcode("post code").build();


        final ContactNumber contact = ContactNumber.contactNumber()
                .withPrimaryEmail("primaryemail@example.com").build();

        final Person person = Person.person()
                .withAddress(address)
                .withFirstName("Matthew")
                .withLastName("Thompson")
                .withContact(contact).build();

        final AssociatedPerson associatedPerson = AssociatedPerson.associatedPerson()
                .withRole("ParentGuardian")
                .withPerson(person).build();

        associatedPersons.add(associatedPerson);

        defendant.setAssociatedPersons(associatedPersons);

        defendant.setPersonDefendant(personDefendant);

        return defendant;
    }

    public ProsecutionCase prepareCase(){
        return ProsecutionCase.prosecutionCase()
                .withDefendants(singletonList(prepareDefendant()))
                .withProsecutionCaseIdentifier(ProsecutionCaseIdentifier.prosecutionCaseIdentifier()
                        .withContact(ContactNumber.contactNumber().withPrimaryEmail("contact@cpp.co.uk").build())
                        .withProsecutionAuthorityName("AuthorityName")
                        .withAddress(Address.address().withAddress1("line 1").withPostcode("E14 4XA").build())
                        .build())
                .build();
    }
}