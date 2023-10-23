package uk.gov.moj.cpp.hearing.query.view.service;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.moj.cpp.hearing.common.ReusableInformationConverterType.ADDRESS;
import static uk.gov.moj.cpp.hearing.common.ReusableInformationConverterType.FIXL;
import static uk.gov.moj.cpp.hearing.common.ReusableInformationConverterType.FIXLM;
import static uk.gov.moj.cpp.hearing.common.ReusableInformationConverterType.INT;
import static uk.gov.moj.cpp.hearing.common.ReusableInformationConverterType.TXT;

import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.core.courts.CourtApplicationParty;
import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.MasterDefendant;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt;
import uk.gov.moj.cpp.hearing.persist.entity.ha.ReusableInfo;
import uk.gov.moj.cpp.hearing.query.view.convertor.ReusableInformationMainConverter;
import uk.gov.moj.cpp.hearing.repository.ReusableInfoRepository;
import uk.gov.justice.core.courts.ProsecutionCase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.json.Json;
import javax.json.JsonObject;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ReusableInfoServiceTest {

    @Mock
    private ReusableInfoRepository reusableInfoRepository;

    @Mock
    private ObjectMapper mapper;

    @Mock
    private ReusableInformationMainConverter reusableInformationMainConverter;

    @InjectMocks
    private ReusableInfoService reusableInfoService;

    @Test
    public void shouldGetReusableInfoForDefendantsIfDefendantListIsEmpty() {
        final List<ReusableInfo> result = reusableInfoService.getReusableInfoForDefendants(emptyList());
        verifyNoMoreInteractions(reusableInfoRepository);
        assertThat(result, is(emptyList()));
    }

    @Test
    public void shouldGetCaseDetailReusableInformation(){
        final UUID masterDefendantId = UUID.randomUUID();
        Defendant defendant = new Defendant.Builder().withMasterDefendantId(masterDefendantId).build();
        ProsecutionCase prosecutionCase = new ProsecutionCase.Builder().withDefendants(Stream.of(defendant).collect(Collectors.toList())).build();
        final List<Prompt> prompts = new ArrayList<>();
        prompts.addAll(prepareTxtPromptsWithCommaSeperated());
        prompts.addAll(prepareTxtPrompts());
        prompts.addAll(prepareAddressPrompts());
        prompts.addAll(prepareFixlmPrompts());
        prompts.addAll(prepareIntPrompts());
        prompts.addAll(prepareFixlPrompts());
        Map<String, String> map = Stream.of(new String[][] {
                { "c1","country1" },
                { "c2","country2" },
        }).collect(Collectors.toMap(data -> data[0], data -> data[1]));
        final List<JsonObject> caseDetailReusableInformation = reusableInfoService.getCaseDetailReusableInformation(Stream.of(prosecutionCase).collect(Collectors.toList()), prompts, map);
        assertThat(caseDetailReusableInformation.size(),is(0));
    }

    @Test
    public void shouldGetApplicationDetailReusableInformation(){
        final UUID masterDefendantId = UUID.randomUUID();
        final CourtApplicationParty courtApplicationParty = new CourtApplicationParty.Builder()
                .withMasterDefendant(new MasterDefendant.Builder().withMasterDefendantId(masterDefendantId).build()).build();
        final CourtApplication courtApplication = new CourtApplication.Builder()
                .withApplicant(courtApplicationParty)
                .withSubject(courtApplicationParty).build();
        final List<Prompt> prompts = new ArrayList<>();
        prompts.addAll(prepareTxtPromptsWithCommaSeperated());
        prompts.addAll(prepareTxtPrompts());
        prompts.addAll(prepareAddressPrompts());
        prompts.addAll(prepareFixlmPrompts());
        prompts.addAll(prepareIntPrompts());
        prompts.addAll(prepareFixlPrompts());

        final List<JsonObject> applicationDetailReusableInformation = reusableInfoService.getApplicationDetailReusableInformation(Stream.of(courtApplication).collect(toList()), prompts);
        assertThat(applicationDetailReusableInformation.size(),is(0));
    }

    @Test
    public void shouldGetViewStoreReusableInformation() throws IOException {
        final UUID masterDefendantId = UUID.randomUUID();
        Defendant defendant = new Defendant.Builder()
                .withMasterDefendantId(masterDefendantId).build();
        final JsonObject jsonObj = Json.createObjectBuilder()
                .add("reusablePrompts",Json.createArrayBuilder().add("reusablePrompts1").add("reusablePrompts2").build())
                .add("reusableResults",Json.createArrayBuilder().add("reusableResults1").add("reusableResults2").build())
                .build();
        final ReusableInfo reusableInfo = ReusableInfo.builder()
                .withId(masterDefendantId)
                .withPayload(getJsonNode(jsonObj)).build();
        when(reusableInfoRepository.findReusableInfoByMasterDefendantIds(Stream.of(masterDefendantId).collect(toList())))
                .thenReturn(Stream.of(reusableInfo).collect(toList()));
        when(mapper.treeToValue(getJsonNode(jsonObj), JsonObject.class))
                .thenReturn(Json.createObjectBuilder().build());
        reusableInfoService.getViewStoreReusableInformation(Stream.of(defendant).collect(toList()), Stream.of(Json.createObjectBuilder().build()).collect(toList()));
    }

    private JsonNode getJsonNode(final JsonObject jsonObj) throws IOException {
        final ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readTree(jsonObj.toString());
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

    private List<Prompt> prepareTxtPrompts() {
        return Collections.unmodifiableList(Arrays.asList(new Prompt()
                .setId(UUID.randomUUID())
                .setType(TXT.name())
                .setCacheable(2)
                .setCacheDataPath("personDefendant.driverNumber")
                .setReference("defendantDrivingLicenceNumber")));
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

}
