package uk.gov.moj.cpp.hearing.query.view.converter;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;
import static uk.gov.moj.cpp.hearing.common.ReusableInformationConverterType.ADDRESS;
import static uk.gov.moj.cpp.hearing.common.ReusableInformationConverterType.FIXL;
import static uk.gov.moj.cpp.hearing.common.ReusableInformationConverterType.FIXLM;
import static uk.gov.moj.cpp.hearing.common.ReusableInformationConverterType.FIXLOM;
import static uk.gov.moj.cpp.hearing.common.ReusableInformationConverterType.INT;
import static uk.gov.moj.cpp.hearing.common.ReusableInformationConverterType.INTC;
import static uk.gov.moj.cpp.hearing.common.ReusableInformationConverterType.NAMEADDRESS;
import static uk.gov.moj.cpp.hearing.common.ReusableInformationConverterType.TXT;
import static java.util.UUID.randomUUID;
import static java.util.Arrays.asList;

import uk.gov.justice.core.courts.Address;
import uk.gov.justice.core.courts.AssociatedPerson;
import uk.gov.justice.core.courts.ContactNumber;
import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.core.courts.CourtApplicationParty;
import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.MasterDefendant;
import uk.gov.justice.core.courts.Person;
import uk.gov.justice.core.courts.PersonDefendant;
import uk.gov.justice.core.courts.ProsecutingAuthority;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.justice.core.courts.ProsecutionCaseIdentifier;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.test.utils.framework.api.JsonObjectConvertersFactory;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt;
import uk.gov.moj.cpp.hearing.query.view.convertor.CustomReusableInfoConverter;
import uk.gov.moj.cpp.hearing.query.view.convertor.ReusableInformationFixlConverter;
import uk.gov.moj.cpp.hearing.query.view.convertor.ReusableInformationFixlmConverter;
import uk.gov.moj.cpp.hearing.query.view.convertor.ReusableInformationFixlomConverter;
import uk.gov.moj.cpp.hearing.query.view.convertor.ReusableInformationINTCConverter;
import uk.gov.moj.cpp.hearing.query.view.convertor.ReusableInformationIntConverter;
import uk.gov.moj.cpp.hearing.query.view.convertor.ReusableInformationMainConverter;
import uk.gov.moj.cpp.hearing.query.view.convertor.ReusableInformationObjectTypeConverter;
import uk.gov.moj.cpp.hearing.query.view.convertor.ReusableInformationTxtConverter;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.json.JsonObject;

import com.jayway.jsonpath.DocumentContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
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

    @BeforeEach
    public void setUp() {
        setField(this.reusableInformationMainConverter, "reusableInformationTxtConverter", new ReusableInformationTxtConverter());
        setField(this.reusableInformationMainConverter, "reusableInformationIntConverter", new ReusableInformationIntConverter());
        setField(this.reusableInformationMainConverter, "reusableInformationFixlConverter", new ReusableInformationFixlConverter());
        setField(this.reusableInformationMainConverter, "reusableInformationFixlmConverter", new ReusableInformationFixlmConverter());
        setField(this.reusableInformationMainConverter, "reusableInformationFixlomConverter", new ReusableInformationFixlomConverter());
        setField(this.reusableInformationMainConverter, "reusableInformationINTCConverter", new ReusableInformationINTCConverter());

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
        prompts.addAll(prepareFixlomPrompts());
        prompts.addAll(prepareINTCPromptsHome());
        prompts.addAll(prepareINTCPromptsMobile());


        final Map<String, Map<String, String>> customPromptValues = new HashMap<>();
        final Map<String, String> customCodesMap = new HashMap<>();
        customCodesMap.put("350", "350");
        customCodesMap.put("460", "460");
        customPromptValues.put("nationality", customCodesMap);

        final Map<Defendant, List<JsonObject>> defendantListMap = reusableInformationMainConverter.convertDefendant(defendants, prompts, customPromptValues);

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
        assertINTCTypeHome(defendants.get(0), prompts, promptJsonObjects);
        assertINTCTypeMobile(defendants.get(0), prompts, promptJsonObjects);

    }

    @Test
    public void shouldConvertForMasterDefendant() {
        final List<MasterDefendant> defendants = Collections.unmodifiableList(Arrays.asList(prepareMasterDefendant()));
        final List<Prompt> prompts = new ArrayList<>();
        prompts.addAll(prepareTxtPromptsWithCommaSeperated());
        prompts.addAll(prepareTxtPrompts());
        prompts.addAll(prepareAddressPrompts());
        prompts.addAll(prepareFixlmPrompts());
        prompts.addAll(prepareIntPrompts());
        prompts.addAll(prepareFixlPrompts());

        final Map<MasterDefendant, List<JsonObject>> defendantListMap = reusableInformationMainConverter.convertMasterDefendant(defendants, prompts);

        assertNotNull(defendantListMap);

        assertThat(defendantListMap.size(), is(defendants.size()));

        final List<JsonObject> promptJsonObjects = defendantListMap.get(defendants.get(0));

        assertTxtType(defendants.get(0), prompts, promptJsonObjects);
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

        // TXT prompts with no matching case paths must be omitted (not emitted as blank values)
        assertThat(promptJsonObjects.size(), is(1));
        assertThat(promptJsonObjects.get(0).getString("promptRef"), is("minorcreditornameandaddress"));
        assertThat(promptJsonObjects.get(0).getString("type"), is("NAMEADDRESS"));
        assertThat(promptJsonObjects.get(0).getJsonObject("value").getString("minorcreditornameandaddressOrganisationName"), is("AuthorityName"));
        assertThat(promptJsonObjects.get(0).getJsonObject("value").getString("minorcreditornameandaddressAddress1"), is("line 1"));
        assertThat(promptJsonObjects.get(0).getJsonObject("value").get("minorcreditornameandaddressAddress2"), nullValue());
    }


    @Test
    public void shouldConvertForApplicationWithRespondent() {
        final List<CourtApplication> courtApplications = singletonList(prepareCourtApplicationWithRespondent());
        final List<Prompt> prompts = prepareApplicationPrompts();
        final Map<CourtApplication, List<JsonObject>> applicationListMap = reusableInformationMainConverter.convertApplication(courtApplications, prompts);

        assertThat(applicationListMap.size(), is(courtApplications.size()));

        final List<JsonObject> promptJsonObjects = applicationListMap.get(courtApplications.get(0));
        assertThat(promptJsonObjects.get(0).getString("promptRef"), is("prosecutortobenotified"));
        assertThat(promptJsonObjects.get(0).getString("type"), is("NAMEADDRESS"));
        assertThat(promptJsonObjects.get(0).getString("applicationId"), is(courtApplications.get(0).getId().toString()));

        final ProsecutingAuthority prosecutingAuthority = courtApplications.get(0).getRespondents().get(0).getProsecutingAuthority();
        final Address address = prosecutingAuthority.getAddress();
        final ContactNumber contact = prosecutingAuthority.getContact();

        assertThat(promptJsonObjects.get(0).getJsonObject("value").getString("prosecutortobenotifiedOrganisationName"), is(prosecutingAuthority.getName()));
        assertThat(promptJsonObjects.get(0).getJsonObject("value").getString("prosecutortobenotifiedAddress1"), is(address.getAddress1()));
        assertThat(promptJsonObjects.get(0).getJsonObject("value").getString("prosecutortobenotifiedAddress2"), is(address.getAddress2()));
        assertThat(promptJsonObjects.get(0).getJsonObject("value").getString("prosecutortobenotifiedPostCode"), is(address.getPostcode()));
        assertThat(promptJsonObjects.get(0).getJsonObject("value").getString("prosecutortobenotifiedEmailAddress1"), is(contact.getPrimaryEmail()));
    }

    @Test
    public void shouldConvertForApplicationWithApplicant() {
        final List<CourtApplication> courtApplications = singletonList(prepareCourtApplicationWithApplicant());
        final List<Prompt> prompts = prepareApplicationPrompts();
        final Map<CourtApplication, List<JsonObject>> applicationListMap = reusableInformationMainConverter.convertApplication(courtApplications, prompts);

        assertThat(applicationListMap.size(), is(courtApplications.size()));

        final List<JsonObject> promptJsonObjects = applicationListMap.get(courtApplications.get(0));
        assertThat(promptJsonObjects.get(0).getString("promptRef"), is("prosecutortobenotified"));
        assertThat(promptJsonObjects.get(0).getString("type"), is("NAMEADDRESS"));
        assertThat(promptJsonObjects.get(0).getString("applicationId"), is(courtApplications.get(0).getId().toString()));

        final ProsecutingAuthority prosecutingAuthority = courtApplications.get(0).getApplicant().getProsecutingAuthority();
        final Address address = prosecutingAuthority.getAddress();
        final ContactNumber contact = prosecutingAuthority.getContact();

        assertThat(promptJsonObjects.get(0).getJsonObject("value").getString("prosecutortobenotifiedOrganisationName"), is(prosecutingAuthority.getName()));
        assertThat(promptJsonObjects.get(0).getJsonObject("value").getString("prosecutortobenotifiedAddress1"), is(address.getAddress1()));
        assertThat(promptJsonObjects.get(0).getJsonObject("value").getString("prosecutortobenotifiedAddress2"), is(address.getAddress2()));
        assertThat(promptJsonObjects.get(0).getJsonObject("value").getString("prosecutortobenotifiedPostCode"), is(address.getPostcode()));
        assertThat(promptJsonObjects.get(0).getJsonObject("value").getString("prosecutortobenotifiedEmailAddress1"), is(contact.getPrimaryEmail()));
    }

    @Test
    public void shouldConvertForApplicationWithApplicantWhenPromptsInAnyOrder() {
        final List<CourtApplication> courtApplications = singletonList(prepareCourtApplicationWithApplicant());
        final List<Prompt> prompts = prepareApplicationPrompts();

        List<Prompt> randomPromptList = asList(prompts.get(1), prompts.get(4), prompts.get(2), prompts.get(3), prompts.get(0));
        final Map<CourtApplication, List<JsonObject>> applicationListMap = reusableInformationMainConverter.convertApplication(courtApplications, randomPromptList);

        assertThat(applicationListMap.size(), is(courtApplications.size()));

        final List<JsonObject> promptJsonObjects = applicationListMap.get(courtApplications.get(0));
        assertThat(promptJsonObjects.get(0).getString("promptRef"), is("prosecutortobenotified"));
        assertThat(promptJsonObjects.get(0).getString("type"), is("NAMEADDRESS"));
        assertThat(promptJsonObjects.get(0).getString("applicationId"), is(courtApplications.get(0).getId().toString()));

        final ProsecutingAuthority prosecutingAuthority = courtApplications.get(0).getApplicant().getProsecutingAuthority();
        final Address address = prosecutingAuthority.getAddress();
        final ContactNumber contact = prosecutingAuthority.getContact();

        assertThat(promptJsonObjects.get(0).getJsonObject("value").getString("prosecutortobenotifiedOrganisationName"), is(prosecutingAuthority.getName()));
        assertThat(promptJsonObjects.get(0).getJsonObject("value").getString("prosecutortobenotifiedAddress1"), is(address.getAddress1()));
        assertThat(promptJsonObjects.get(0).getJsonObject("value").getString("prosecutortobenotifiedAddress2"), is(address.getAddress2()));
        assertThat(promptJsonObjects.get(0).getJsonObject("value").getString("prosecutortobenotifiedPostCode"), is(address.getPostcode()));
        assertThat(promptJsonObjects.get(0).getJsonObject("value").getString("prosecutortobenotifiedEmailAddress1"), is(contact.getPrimaryEmail()));
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
        assertThat("Matthew Thompson", is(parentGuardiansNameJsonObject.getString("value")));
    }

    private void assertTxtType(final Defendant defendant, final List<Prompt> prompts, final List<JsonObject> promptJsonObjects, final Optional<AssociatedPerson> associatedPerson) {
        final JsonObject drivingLicenceNumberJsonObject = promptJsonObjects.stream()
                .filter(jsonObject -> jsonObject.getString(PROMPT_REF).equals("defendantDrivingLicenceNumber"))
                .findAny().get();

        final Prompt prompt = prompts.stream().filter(promptToFilter -> promptToFilter.getReference().equals("defendantDrivingLicenceNumber")).findAny().get();

        assertThat(prompt.getReference(), is(drivingLicenceNumberJsonObject.getString(PROMPT_REF)));
        assertThat(prompt.getType(), is(drivingLicenceNumberJsonObject.getString(TYPE)));
        assertThat(defendant.getMasterDefendantId().toString(), is(drivingLicenceNumberJsonObject.getString("masterDefendantId")));
        assertThat("MORGA657054SM9BF", is(drivingLicenceNumberJsonObject.getString("value")));
    }

    private void assertTxtType(final MasterDefendant defendant, final List<Prompt> prompts, final List<JsonObject> promptJsonObjects) {
        final JsonObject drivingLicenceNumberJsonObject = promptJsonObjects.stream()
                .filter(jsonObject -> jsonObject.getString(PROMPT_REF).equals("defendantDrivingLicenceNumber"))
                .findAny().get();

        final Prompt prompt = prompts.stream().filter(promptToFilter -> promptToFilter.getReference().equals("defendantDrivingLicenceNumber")).findAny().get();

        assertThat(prompt.getReference(), is(drivingLicenceNumberJsonObject.getString(PROMPT_REF)));
        assertThat(prompt.getType(), is(drivingLicenceNumberJsonObject.getString(TYPE)));
        assertThat(defendant.getMasterDefendantId().toString(), is(drivingLicenceNumberJsonObject.getString("masterDefendantId")));
        assertThat("MORGA657054SM9BF", is(drivingLicenceNumberJsonObject.getString("value")));
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
    private void assertINTCTypeHome(final Defendant defendant, final List<Prompt> prompts, final List<JsonObject> promptJsonObjects) {
        final JsonObject defendantsHomeTelephoneNumber = promptJsonObjects.stream()
                .filter(jsonObject -> jsonObject.getString(PROMPT_REF).equals("defendantsHomeTelephoneNumber"))
                .findAny().get();

        final Prompt prompt = prompts.stream().filter(promptToFilter -> promptToFilter.getReference().equals("defendantsHomeTelephoneNumber")).findAny().get();

        assertThat(prompt.getReference(), is(defendantsHomeTelephoneNumber.getString(PROMPT_REF)));
        assertThat(prompt.getType(), is(defendantsHomeTelephoneNumber.getString(TYPE)));
        assertThat(defendant.getMasterDefendantId().toString(), is(defendantsHomeTelephoneNumber.getString("masterDefendantId")));
        assertThat("0123456789", is(defendantsHomeTelephoneNumber.getString("value")));
    }

    private void assertINTCTypeMobile(final Defendant defendant, final List<Prompt> prompts, final List<JsonObject> promptJsonObjects) {
        final JsonObject defendantsMobileNumber = promptJsonObjects.stream()
                .filter(jsonObject -> jsonObject.getString(PROMPT_REF).equals("defendantsMobileNumber"))
                .findAny().get();

        final Prompt prompt = prompts.stream().filter(promptToFilter -> promptToFilter.getReference().equals("defendantsMobileNumber")).findAny().get();

        assertThat(prompt.getReference(), is(defendantsMobileNumber.getString(PROMPT_REF)));
        assertThat(prompt.getType(), is(defendantsMobileNumber.getString(TYPE)));
        assertThat(defendant.getMasterDefendantId().toString(), is(defendantsMobileNumber.getString("masterDefendantId")));
        assertThat("9876543210", is(defendantsMobileNumber.getString("value")));
    }



    private List<Prompt> prepareTxtPrompts() {
        return Collections.unmodifiableList(Arrays.asList(new Prompt()
                .setId(UUID.randomUUID())
                .setType(TXT.name())
                .setCacheable(2)
                .setCacheDataPath("personDefendant.driverNumber")
                .setReference("defendantDrivingLicenceNumber")));
    }


    private List<Prompt> prepareINTCPromptsHome() {
        return Collections.unmodifiableList(Arrays.asList(new Prompt()
                .setId(UUID.randomUUID())
                .setType(INTC.name())
                .setCacheable(2)
                .setCacheDataPath("personDefendant.personDetails.contact.home")
                .setReference("defendantsHomeTelephoneNumber")));
    }

    private List<Prompt> prepareINTCPromptsMobile() {
        return Collections.unmodifiableList(Arrays.asList(new Prompt()
                .setId(UUID.randomUUID())
                .setType(INTC.name())
                .setCacheable(2)
                .setCacheDataPath("personDefendant.personDetails.contact.mobile")
                .setReference("defendantsMobileNumber")));
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

    private List<Prompt> prepareFixlomPrompts() {
        return Collections.unmodifiableList(Arrays.asList(new Prompt()
                .setId(UUID.randomUUID())
                .setType(FIXLOM.name())
                .setCacheable(2)
                .setCacheDataPath("prosecutionAuthorityReference")
                .setReference("nationality")));
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
        final ContactNumber contactNumber = ContactNumber
                .contactNumber()
                .withHome("0123456789")
                .withMobile("9876543210")
                .build();
        final Defendant defendant = Defendant.defendant()
                .withMasterDefendantId(UUID.randomUUID())
                .withNumberOfPreviousConvictionsCited(3)
                .withProsecutionAuthorityReference("REF1").build();

        final Person personDetails = Person.person()
                .withNationalityCode("350")
                .withContact(contactNumber)
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

    public MasterDefendant prepareMasterDefendant() {
        final MasterDefendant defendant = MasterDefendant.masterDefendant()
                .withMasterDefendantId(UUID.randomUUID())
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

    public ProsecutionCase prepareCase() {
        return ProsecutionCase.prosecutionCase()
                .withDefendants(singletonList(prepareDefendant()))
                .withProsecutionCaseIdentifier(ProsecutionCaseIdentifier.prosecutionCaseIdentifier()
                        .withContact(ContactNumber.contactNumber().withPrimaryEmail("contact@cpp.co.uk").build())
                        .withProsecutionAuthorityName("AuthorityName")
                        .withAddress(Address.address().withAddress1("line 1").withPostcode("E14 4XA").build())
                        .build())
                .build();
    }

    public CourtApplication prepareCourtApplicationWithRespondent() {
        final Address address = Address.address()
                .withAddress1("28 Burlton Road")
                .withAddress2("Oxford")
                .withPostcode("OX23MX")
                .build();
        final ContactNumber contact = ContactNumber.contactNumber()
                .withPrimaryEmail("John.Joseph@gmail.com").build();
        final ProsecutingAuthority prosecutingAuthority = ProsecutingAuthority.prosecutingAuthority()
                .withName("John")
                .withAddress(address)
                .withContact(contact)
                .build();
        final CourtApplicationParty courtApplicationParty = CourtApplicationParty.courtApplicationParty().withProsecutingAuthority(prosecutingAuthority).build();

        return CourtApplication.courtApplication()
                .withId(randomUUID())
                .withRespondents(asList(courtApplicationParty))
                .build();
    }

    public CourtApplication prepareCourtApplicationWithApplicant() {
        final Address address = Address.address()
                .withAddress1("28 Burlton Road")
                .withAddress2("Oxford")
                .withPostcode("OX23MX")
                .build();
        final ContactNumber contact = ContactNumber.contactNumber()
                .withPrimaryEmail("John.Joseph@gmail.com").build();
        final ProsecutingAuthority prosecutingAuthority = ProsecutingAuthority.prosecutingAuthority()
                .withName("John")
                .withAddress(address)
                .withContact(contact)
                .build();
        final CourtApplicationParty courtApplicationParty = CourtApplicationParty.courtApplicationParty().withProsecutingAuthority(prosecutingAuthority).build();

        return CourtApplication.courtApplication()
                .withId(randomUUID())
                .withApplicant(courtApplicationParty)
                .build();
    }

    private List<Prompt> prepareApplicationPrompts() {
        final UUID promtId = randomUUID();

        final Prompt prompt1 = new Prompt()
                .setId(promtId)
                .setType(NAMEADDRESS.name())
                .setCacheable(2)
                .setCacheDataPath("respondents[0].prosecutingAuthority.name;applicant.prosecutingAuthority.name")
                .setReference("prosecutortobenotifiedOrganisationName")
                .setPartName("OrganisationName");

        final Prompt prompt2 = new Prompt()
                .setId(promtId)
                .setType(NAMEADDRESS.name())
                .setCacheable(2)
                .setCacheDataPath("respondents[0].prosecutingAuthority.address.address1; applicant.prosecutingAuthority.address.address1")
                .setReference("prosecutortobenotifiedAddress1")
                .setPartName("Address1Line1");

        final Prompt prompt3 = new Prompt()
                .setId(promtId)
                .setType(NAMEADDRESS.name())
                .setCacheable(2)
                .setCacheDataPath(" respondents[0].prosecutingAuthority.address.address2; applicant.prosecutingAuthority.address.address2")
                .setReference("prosecutortobenotifiedAddress2")
                .setPartName("AddressLine2");;

        final Prompt prompt4 = new Prompt()
                .setId(promtId)
                .setType(NAMEADDRESS.name())
                .setCacheable(2)
                .setCacheDataPath(" respondents[0].prosecutingAuthority.address.postcode ; applicant.prosecutingAuthority.address.postcode ")
                .setReference("prosecutortobenotifiedPostCode")
                .setPartName("PostCode");

        final Prompt prompt5 = new Prompt()
                .setId(promtId)
                .setType(NAMEADDRESS.name())
                .setCacheable(2)
                .setCacheDataPath("respondents[0].prosecutingAuthority.contact.primaryEmail;applicant.prosecutingAuthority.contact.primaryEmail")
                .setReference("prosecutortobenotifiedEmailAddress1")
                .setPartName("EmailAddress1");

        return asList(prompt1, prompt2, prompt3, prompt4, prompt5);
    }

    @Test
    public void shouldReadPromptValueFromDeeplyNestedJsonBeyondJsonSmartDefaultDepth() throws Exception {
        final int depth = 450;
        final StringBuilder json = new StringBuilder();
        final StringBuilder path = new StringBuilder();
        for (int i = 0; i < depth; i++) {
            json.append("{\"nested\":");
            if (i > 0) {
                path.append('.');
            }
            path.append("nested");
        }
        json.append("{\"value\":\"deep-value\"}");
        path.append(".value");
        for (int i = 0; i < depth; i++) {
            json.append('}');
        }

        final DocumentContext documentContext = parseJson(json.toString());
        final Optional<String> value = toTxtValue(documentContext, path.toString());

        assertTrue(value.isPresent());
        assertThat(value.get(), is("deep-value"));
    }

    @Test
    public void shouldOmitBlankTxtAndIntcPromptsForDefendantWhenPathsDoNotMatch() {
        final Defendant defendant = Defendant.defendant()
                .withMasterDefendantId(randomUUID())
                .withPersonDefendant(PersonDefendant.personDefendant()
                        .withPersonDetails(Person.person().build())
                        .build())
                .build();

        final List<Prompt> prompts = new ArrayList<>();
        prompts.addAll(prepareTxtPrompts());
        prompts.addAll(prepareINTCPromptsHome());
        prompts.addAll(prepareINTCPromptsMobile());

        final Map<Defendant, List<JsonObject>> defendantListMap = reusableInformationMainConverter.convertDefendant(
                singletonList(defendant), prompts, Collections.emptyMap());

        assertThat(defendantListMap.get(defendant).size(), is(0));
    }

    @Test
    public void shouldConvertDefendantWhenCustomPromptValuesIsNull() {
        final List<Defendant> defendants = singletonList(prepareDefendant());
        final List<Prompt> prompts = prepareFixlmPrompts();

        final Map<Defendant, List<JsonObject>> defendantListMap = reusableInformationMainConverter.convertDefendant(
                defendants, prompts, null);

        final List<JsonObject> promptJsonObjects = defendantListMap.get(defendants.get(0));
        assertThat(promptJsonObjects.size(), is(1));
        assertThat(promptJsonObjects.get(0).getString("value"), is("350" + DELIMITER + "460"));
    }

    @Test
    public void shouldMapNationalityCodesViaCustomPromptValuesWithoutMutatingConverterState() {
        final List<Defendant> defendants = singletonList(prepareDefendant());
        final List<Prompt> prompts = prepareFixlmPrompts();

        final Map<String, String> countryCodesMap = new HashMap<>();
        countryCodesMap.put("350", "British");
        countryCodesMap.put("460", "Irish");
        final Map<String, Map<String, String>> customPromptValues = new HashMap<>();
        customPromptValues.put("nationality", countryCodesMap);

        final Map<Defendant, List<JsonObject>> defendantListMap = reusableInformationMainConverter.convertDefendant(
                defendants, prompts, customPromptValues);

        final List<JsonObject> promptJsonObjects = defendantListMap.get(defendants.get(0));
        assertThat(promptJsonObjects.size(), is(1));
        assertThat(promptJsonObjects.get(0).getString("value"), is("British" + DELIMITER + "Irish"));
    }

    @Test
    public void shouldLeaveNationalityCodesUnchangedWhenNationalityKeyMissingFromCustomPromptValues() {
        final List<Defendant> defendants = singletonList(prepareDefendant());
        final List<Prompt> prompts = prepareFixlmPrompts();

        final Map<String, Map<String, String>> customPromptValues = new HashMap<>();
        customPromptValues.put("otherKey", Collections.singletonMap("350", "British"));

        final Map<Defendant, List<JsonObject>> defendantListMap = reusableInformationMainConverter.convertDefendant(
                defendants, prompts, customPromptValues);

        assertThat(defendantListMap.get(defendants.get(0)).get(0).getString("value"), is("350" + DELIMITER + "460"));
    }

    @Test
    public void shouldUseFirstMatchingCacheDataPathForApplicationPrompts() throws Exception {
        final UUID promptId = randomUUID();
        final List<Prompt> prompts = singletonList(new Prompt()
                .setId(promptId)
                .setType(NAMEADDRESS.name())
                .setCacheable(2)
                .setCacheDataPath("missing.path;organisationName")
                .setReference("prosecutortobenotifiedOrganisationName")
                .setPartName("OrganisationName"));

        final DocumentContext documentContext = parseJson("{\"organisationName\":\"ABC Org\",\"fallbackName\":\"ShouldNotUse\"}");

        final Method promptsToJsonObjectCacheDataPathList = ReusableInformationMainConverter.class.getDeclaredMethod(
                "promptsToJsonObjectCacheDataPathList", List.class, DocumentContext.class,
                uk.gov.moj.cpp.hearing.common.ReusableInformationConverterType.class);
        promptsToJsonObjectCacheDataPathList.setAccessible(true);

        final JsonObject result = (JsonObject) promptsToJsonObjectCacheDataPathList.invoke(
                reusableInformationMainConverter, prompts, documentContext, NAMEADDRESS);

        assertThat(result.getString("prosecutortobenotifiedOrganisationName"), is("ABC Org"));
        assertFalse(result.containsKey("fallbackName"));
    }

    @Test
    public void shouldPreferFirstMatchingPathAndIgnoreLaterPaths() throws Exception {
        final UUID promptId = randomUUID();
        final List<Prompt> prompts = singletonList(new Prompt()
                .setId(promptId)
                .setType(NAMEADDRESS.name())
                .setCacheable(2)
                .setCacheDataPath("firstName;secondName")
                .setReference("prosecutortobenotifiedOrganisationName")
                .setPartName("OrganisationName"));

        final DocumentContext documentContext = parseJson("{\"firstName\":\"First\",\"secondName\":\"Second\"}");

        final Method promptsToJsonObjectCacheDataPathList = ReusableInformationMainConverter.class.getDeclaredMethod(
                "promptsToJsonObjectCacheDataPathList", List.class, DocumentContext.class,
                uk.gov.moj.cpp.hearing.common.ReusableInformationConverterType.class);
        promptsToJsonObjectCacheDataPathList.setAccessible(true);

        final JsonObject result = (JsonObject) promptsToJsonObjectCacheDataPathList.invoke(
                reusableInformationMainConverter, prompts, documentContext, NAMEADDRESS);

        assertThat(result.getString("prosecutortobenotifiedOrganisationName"), is("First"));
    }

    @Test
    public void shouldReturnFirstElementWhenJsonPathResolvesToCollection() throws Exception {
        final DocumentContext documentContext = parseJson("{\"names\":[\"Alice\",\"Bob\"]}");

        final Optional<String> value = toTxtValue(documentContext, "names");

        assertTrue(value.isPresent());
        assertThat(value.get(), is("Alice"));
    }

    @Test
    public void shouldReturnEmptyOptionalWhenJsonPathIsInvalid() throws Exception {
        final DocumentContext documentContext = parseJson("{\"name\":\"Alice\"}");

        final Optional<String> value = toTxtValue(documentContext, "does.not.exist");

        assertFalse(value.isPresent());
    }

    @Test
    public void shouldReturnEmptyOptionalWhenJsonPathValueIsNull() throws Exception {
        final DocumentContext documentContext = parseJson("{\"name\":null}");

        final Optional<String> value = toTxtValue(documentContext, "name");

        assertFalse(value.isPresent());
    }

    private DocumentContext parseJson(final String json) throws Exception {
        final Method parseJson = ReusableInformationMainConverter.class.getDeclaredMethod("parseJson", String.class);
        parseJson.setAccessible(true);
        return (DocumentContext) parseJson.invoke(reusableInformationMainConverter, json);
    }

    @SuppressWarnings("unchecked")
    private Optional<String> toTxtValue(final DocumentContext documentContext, final String promptPath) throws Exception {
        final Method toTxtValue = ReusableInformationMainConverter.class.getDeclaredMethod(
                "toTxtValue", DocumentContext.class, String.class);
        toTxtValue.setAccessible(true);
        return (Optional<String>) toTxtValue.invoke(reusableInformationMainConverter, documentContext, promptPath);
    }
}