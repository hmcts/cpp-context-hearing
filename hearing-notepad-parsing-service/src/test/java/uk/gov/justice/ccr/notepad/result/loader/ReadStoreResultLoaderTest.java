package uk.gov.justice.ccr.notepad.result.loader;

import static java.lang.Boolean.TRUE;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.mockito.BDDMockito.given;
import static uk.gov.justice.ccr.notepad.util.FileUtil.givenPayload;

import uk.gov.justice.ccr.notepad.result.cache.model.ChildResultDefinition;
import uk.gov.justice.ccr.notepad.result.cache.model.ResultDefinition;
import uk.gov.justice.ccr.notepad.result.cache.model.ResultPrompt;
import uk.gov.justice.ccr.notepad.result.cache.model.ResultPromptDynamicListNameAddress;
import uk.gov.justice.ccr.notepad.result.cache.model.ResultType;
import uk.gov.justice.ccr.notepad.service.ResultsQueryService;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.json.JsonObject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ReadStoreResultLoaderTest {

    @Mock
    private ResultsQueryService resultsQueryService;

    @Mock
    private JsonEnvelope jsonEnvelope;

    @Mock
    private Envelope<JsonObject> responseEnvelope;

    @Mock
    private Envelope<JsonObject> jsonEnvelopeDynaCourtCentre;

    @Mock
    private Envelope<JsonObject> jsonEnvelopeDynaHearingType;

    @Mock
    private Envelope<JsonObject> jsonEnvelopeLocalJusticeArea;

    @Mock
    private Envelope<JsonObject> jsonEnvelopeCountries;

    @Mock
    private Envelope<JsonObject> jsonEnvelopeLanguages;

    @Mock
    private Envelope<JsonObject> jsonEnvelopeLocalAuthorities;

    @Mock
    private Envelope<JsonObject> jsonEnvelopePrisonNames;

    @Mock
    private Envelope<JsonObject> jsonEnvelopeCrownCourtNames;

    @Mock
    private Envelope<JsonObject> jsonEnvelopeScottishNICourtNames;

    @Mock
    private Envelope<JsonObject> jsonEnvelopeYouthCourtNames;


    @Mock
    private NameAddressRefDataEndPointMapper nameAddressRefDataEndPointMapper;

    @InjectMocks
    private ReadStoreResultLoader underTest;

    @Test
    public void loadResultDefinition() throws Exception {
        final LocalDate hearingDate = LocalDate.now();
        given(resultsQueryService.getAllDefinitions(jsonEnvelope, hearingDate)).willReturn(responseEnvelope);
        given(responseEnvelope.payload())
                .willReturn(givenPayload("/referencedata.result-definitions-legacy.json"));

        final List<ResultDefinition> resultDefinitions = underTest.loadResultDefinition(hearingDate);
        assertThat(resultDefinitions, hasSize(4));

        final ResultDefinition resultDefinition1 = resultDefinitions.get(0);
        assertThat(resultDefinition1.getKeywords(), hasItems("restraining", "order", "period"));
        final ResultDefinition resultDefinition2 = resultDefinitions.get(1);
        assertThat(resultDefinition2.getKeywords(), hasItems("restraop"));
        assertThat(resultDefinition1.getTerminatesOffenceProceedings(), is(TRUE));
        assertThat(resultDefinition1.getAlwaysPublished(), is(TRUE));
        assertThat(resultDefinition1.getLifeDuration(), is(TRUE));
        assertThat(resultDefinition1.getD20(), is(TRUE));
        assertThat(resultDefinition1.getExcludedFromResults(), is(TRUE));
        assertThat(resultDefinition1.getUrgent(), is(TRUE));
        assertThat(resultDefinition1.getPublishedAsAPrompt(), is(TRUE));
        assertThat(resultDefinition1.getPublishedForNows(), is(TRUE));
        assertThat(resultDefinition1.getRollUpPrompts(), is(TRUE));
        assertThat(resultDefinition1.getRollUpPrompts(), is(TRUE));
    }

    @Test
    public void loadResultDefinitionNoWordGroups() throws Exception {
        final LocalDate hearingDate = LocalDate.now();
        given(resultsQueryService.getAllDefinitions(jsonEnvelope, hearingDate)).willReturn(responseEnvelope);
        given(responseEnvelope.payload())
                .willReturn(givenPayload("/referencedata.result-definitions-no-word-groups.json"));

        final List<ResultDefinition> resultDefinitions = underTest.loadResultDefinition(hearingDate);
        assertThat(resultDefinitions, hasSize(2));
    }

    @Test
    public void loadResultDefinitionSynonym() throws Exception {
        final LocalDate hearingDate = LocalDate.now();
        given(resultsQueryService.getAllDefinitionWordSynonyms(jsonEnvelope, hearingDate)).willReturn(responseEnvelope);
        given(responseEnvelope.payload())
                .willReturn(givenPayload("/referencedata.result-word-synonyms.json"));

        assertThat(underTest.loadResultDefinitionSynonym(hearingDate), hasSize(3));
    }


    @Test
    public void loadResultPromptSynonym() throws Exception {
        final LocalDate hearingDate = LocalDate.now();
        given(resultsQueryService.getAllResultPromptWordSynonyms(jsonEnvelope, hearingDate)).willReturn(responseEnvelope);
        given(responseEnvelope.payload())
                .willReturn(givenPayload("/referencedata.result-prompt-synonyms.json"));

        assertThat(underTest.loadResultPromptSynonym(hearingDate), hasSize(2));
    }

    @Test
    public void loadResultPrompts() throws Exception {
        //given
        final LocalDate hearingDate = LocalDate.now();
        final Map<String, Set<ResultPromptDynamicListNameAddress>>  resultPromptDynamicListNameAddress = new HashMap<>();
        final Set<ResultPromptDynamicListNameAddress>  nameAddressSet = new HashSet<>();
        nameAddressSet.add(ResultPromptDynamicListNameAddress.resultPromptDynamicListNameAddressBuilder()
                .withName("testName")
                .withAddressLine1("test addressLine 1")
                .withAddressLine2("test AddressLIne2")
                .withPostCode("SW1A 2AA")
                .withEmailAddress1("xyz@gmail.com")
                .build()
        );

        final Set<ResultPromptDynamicListNameAddress>  nameAddressSet2 = new HashSet<>();
        nameAddressSet2.add(ResultPromptDynamicListNameAddress.resultPromptDynamicListNameAddressBuilder()
                .withName("EMC Test")
                .withEmailAddress1("emc@test.com")
                .build());
        resultPromptDynamicListNameAddress.put("protectedperson", nameAddressSet);
        resultPromptDynamicListNameAddress.put("electronicmonitoringcontractor", nameAddressSet2);


        given(resultsQueryService.getAllFixedLists(jsonEnvelope, hearingDate)).willReturn(responseEnvelope);
        given(resultsQueryService.getAllDefinitions(jsonEnvelope, hearingDate)).willReturn(responseEnvelope);
        given(resultsQueryService.getAllCourtCentre(jsonEnvelope)).willReturn(jsonEnvelopeDynaCourtCentre);
        given(resultsQueryService.getHearingTypes(jsonEnvelope)).willReturn(jsonEnvelopeDynaHearingType);
        given(resultsQueryService.getLocalJusticeAreas(jsonEnvelope)).willReturn(jsonEnvelopeLocalJusticeArea);
        given(resultsQueryService.getCountriesNames(jsonEnvelope)).willReturn(jsonEnvelopeCountries);
        given(resultsQueryService.getLanguages(jsonEnvelope)).willReturn(jsonEnvelopeLanguages);
        given(resultsQueryService.getLocalAuthorityNameAddress(jsonEnvelope)).willReturn(jsonEnvelopeLocalAuthorities);
        given(resultsQueryService.getPrisonNameAddress(jsonEnvelope)).willReturn(jsonEnvelopePrisonNames);
        given(resultsQueryService.getCrownCourtsNameAddress(jsonEnvelope)).willReturn(jsonEnvelopeCrownCourtNames);
        given(resultsQueryService.getScottishCourtAddress(jsonEnvelope)).willReturn(jsonEnvelopeScottishNICourtNames);
        given(resultsQueryService.getYouthCourtAddress(jsonEnvelope)).willReturn(jsonEnvelopeYouthCourtNames);


        given(nameAddressRefDataEndPointMapper.loadAllNameAddressFromRefData()).willReturn(resultPromptDynamicListNameAddress);
        given(responseEnvelope.payload())
                .willReturn(givenPayload("/referencedata.fixedlists.json"))
                .willReturn(givenPayload("/referencedata.result-definitions.json"));
        given(jsonEnvelopeDynaCourtCentre.payload())
                .willReturn(givenPayload("/referencedata.dyna.fixedlists.court.centre.json"));
        given(jsonEnvelopeDynaHearingType.payload())
                .willReturn(givenPayload("/referencedata.dyna.fixedlists.hearing.type.json"));
        given(jsonEnvelopeLocalJusticeArea.payload())
                .willReturn(givenPayload("/referencedata.local-justice-area.json"));
        given(jsonEnvelopeCountries.payload())
                .willReturn(givenPayload("/referencedata.countries.json"));
        given(jsonEnvelopeLanguages.payload())
                .willReturn(givenPayload("/referencedata.languages.json"));
        given(jsonEnvelopeLocalAuthorities.payload())
                .willReturn(givenPayload("/referencedata.local-authorities.json"));
        given(jsonEnvelopePrisonNames.payload())
                .willReturn(givenPayload("/referencedata.prisons.json"));
        given(jsonEnvelopeCrownCourtNames.payload())
                .willReturn(givenPayload("/referencedata.crown-courts.json"));
        given(jsonEnvelopeScottishNICourtNames.payload())
                .willReturn(givenPayload("/referencedata.scottis-ni-courts.json"));
        given(jsonEnvelopeYouthCourtNames.payload())
                .willReturn(givenPayload("/referencedata.youth-courts.json"));



        //when
        final List<ResultPrompt> resultPrompts = underTest.loadResultPrompt(hearingDate);

        //then
        final ResultPrompt resultPromptWithNameAddressList = resultPrompts.stream().filter(resultPrompt -> resultPrompt.getId() .toString().equals( "3054909b-15b6-499f-b44f-67b2b1215c72")).findFirst().get();
        assertThat(resultPrompts, hasSize(35));
        assertThat(resultPrompts.get(0).getPromptOrder(), is(100));
        assertThat(resultPrompts.get(0).getReference(), is("electronicmonitoringcontractorOrganisationName"));
        assertThat(resultPrompts.get(0).getNameAddressList().size(), is(1));
        assertThat(resultPrompts.get(12).getPromptOrder(), is(1));
        assertThat(resultPrompts.get(12).getReference(), is(nullValue()));
        assertThat(resultPrompts.get(12).getDurationSequence(), is(1));
        assertThat(resultPrompts.get(19).getFixedList().size(), is(9));
        assertThat(resultPrompts.get(19).getFixedList(),hasItems("Aberdeen Sheriff Court District", "Bedfordshire YOT", "Blackfriars Crown Court", "Central Criminal Court", "Leicester and Rutland Magistrates' Court",
                "North and East Hertfordshire Magistrates' Court", "North and West Cumbria Magistrates' Court", "Sussex (Eastern) Magistrates' Court", "West Cross Court District"));
        assertThat(resultPrompts.get(22).getReference(), is("HCHOUSE"));
        assertThat(resultPrompts.get(22).getType(), is(ResultType.FIXL));
        assertThat(resultPrompts.get(22).getFixedList().size(), is(2));
        assertThat(resultPrompts.get(22).getDurationSequence(), is(0));
        assertThat(resultPrompts.get(23).getReference(), is("HTYPE"));
        assertThat(resultPrompts.get(23).getType(), is(ResultType.FIXL));
        assertThat(resultPrompts.get(23).getFixedList().size(), is(27));
        assertThat(resultPrompts.get(23).getMinLength(), is("1"));
        assertThat(resultPrompts.get(23).getMaxLength(), is("4000"));
        assertThat(resultPrompts.get(24).getType(), is(ResultType.FIXLO));
        assertThat(resultPrompts.get(25).getType(), is(ResultType.FIXLOM));
        assertThat(resultPrompts.get(15).getKeywords(), hasItems("years"));
        assertThat(resultPrompts.get(12).getHidden(), is(false));
        assertThat(resultPrompts.get(15).getWelshDurationElement(), is("Flynedd"));
        assertThat(resultPrompts.get(15).getDurationElement(), is("Years"));
        assertThat(resultPromptWithNameAddressList.getNameAddressList().size(), is(1));
    }

    @Test
    public void shouldLoadResultPromptsWithLegacyData() throws Exception {
        //given
        final LocalDate hearingDate = LocalDate.now();
        given(resultsQueryService.getAllFixedLists(jsonEnvelope, hearingDate)).willReturn(responseEnvelope);
        given(resultsQueryService.getAllDefinitions(jsonEnvelope, hearingDate)).willReturn(responseEnvelope);
        given(resultsQueryService.getAllCourtCentre(jsonEnvelope)).willReturn(jsonEnvelopeDynaCourtCentre);
        given(resultsQueryService.getHearingTypes(jsonEnvelope)).willReturn(jsonEnvelopeDynaHearingType);
        given(resultsQueryService.getLocalJusticeAreas(jsonEnvelope)).willReturn(jsonEnvelopeLocalJusticeArea);
        given(resultsQueryService.getCountriesNames(jsonEnvelope)).willReturn(jsonEnvelopeCountries);
        given(resultsQueryService.getLanguages(jsonEnvelope)).willReturn(jsonEnvelopeLanguages);
        given(resultsQueryService.getLocalAuthorityNameAddress(jsonEnvelope)).willReturn(jsonEnvelopeLocalAuthorities);
        given(resultsQueryService.getPrisonNameAddress(jsonEnvelope)).willReturn(jsonEnvelopePrisonNames);
        given(resultsQueryService.getCrownCourtsNameAddress(jsonEnvelope)).willReturn(jsonEnvelopeCrownCourtNames);
        given(resultsQueryService.getScottishCourtAddress(jsonEnvelope)).willReturn(jsonEnvelopeScottishNICourtNames);
        given(resultsQueryService.getYouthCourtAddress(jsonEnvelope)).willReturn(jsonEnvelopeYouthCourtNames);

        given(responseEnvelope.payload())
                .willReturn(givenPayload("/referencedata.fixedlists.json"))
                .willReturn(givenPayload("/referencedata.result-definitions-legacy.json"));
        given(jsonEnvelopeDynaCourtCentre.payload())
                .willReturn(givenPayload("/referencedata.dyna.fixedlists.court.centre.json"));
        given(jsonEnvelopeDynaHearingType.payload())
                .willReturn(givenPayload("/referencedata.dyna.fixedlists.hearing.type.json"));
        given(jsonEnvelopeLocalJusticeArea.payload())
                .willReturn(givenPayload("/referencedata.local-justice-area.json"));
        given(jsonEnvelopeCountries.payload())
                .willReturn(givenPayload("/referencedata.countries.json"));
        given(jsonEnvelopeLanguages.payload())
                .willReturn(givenPayload("/referencedata.languages.json"));
        given(jsonEnvelopeLocalAuthorities.payload())
                .willReturn(givenPayload("/referencedata.local-authorities.json"));
        given(jsonEnvelopePrisonNames.payload())
                .willReturn(givenPayload("/referencedata.prisons.json"));
        given(jsonEnvelopeCrownCourtNames.payload())
                .willReturn(givenPayload("/referencedata.crown-courts.json"));
        given(jsonEnvelopeScottishNICourtNames.payload())
                .willReturn(givenPayload("/referencedata.scottis-ni-courts.json"));
        given(jsonEnvelopeYouthCourtNames.payload())
                .willReturn(givenPayload("/referencedata.youth-courts.json"));

        //when
        final List<ResultPrompt> resultPrompts = underTest.loadResultPrompt(hearingDate);

        //then
        assertThat(resultPrompts, hasSize(21));
        assertThat(resultPrompts.get(0).getResultPromptRule(), is("mandatory"));
        assertThat(resultPrompts.get(9).getResultPromptRule(), is("optional"));
    }

    @Test
    public void resultPromptShouldLoadAssociatedFixedList() throws Exception {
        //given
        final LocalDate hearingDate = LocalDate.now();
        given(resultsQueryService.getAllFixedLists(jsonEnvelope, hearingDate)).willReturn(responseEnvelope);
        given(resultsQueryService.getAllDefinitions(jsonEnvelope, hearingDate)).willReturn(responseEnvelope);
        given(resultsQueryService.getAllCourtCentre(jsonEnvelope)).willReturn(jsonEnvelopeDynaCourtCentre);
        given(resultsQueryService.getHearingTypes(jsonEnvelope)).willReturn(jsonEnvelopeDynaHearingType);
        given(resultsQueryService.getLocalJusticeAreas(jsonEnvelope)).willReturn(jsonEnvelopeLocalJusticeArea);
        given(resultsQueryService.getCountriesNames(jsonEnvelope)).willReturn(jsonEnvelopeCountries);
        given(resultsQueryService.getLanguages(jsonEnvelope)).willReturn(jsonEnvelopeLanguages);
        given(resultsQueryService.getLocalAuthorityNameAddress(jsonEnvelope)).willReturn(jsonEnvelopeLocalAuthorities);
        given(resultsQueryService.getPrisonNameAddress(jsonEnvelope)).willReturn(jsonEnvelopePrisonNames);
        given(resultsQueryService.getCrownCourtsNameAddress(jsonEnvelope)).willReturn(jsonEnvelopeCrownCourtNames);
        given(resultsQueryService.getScottishCourtAddress(jsonEnvelope)).willReturn(jsonEnvelopeScottishNICourtNames);
        given(resultsQueryService.getYouthCourtAddress(jsonEnvelope)).willReturn(jsonEnvelopeYouthCourtNames);

        given(responseEnvelope.payload())
                .willReturn(givenPayload("/referencedata.fixedlists.json"))
                .willReturn(givenPayload("/referencedata.result-definitions.json"));
        given(jsonEnvelopeDynaCourtCentre.payload())
                .willReturn(givenPayload("/referencedata.dyna.fixedlists.court.centre.json"));
        given(jsonEnvelopeDynaHearingType.payload())
                .willReturn(givenPayload("/referencedata.dyna.fixedlists.hearing.type.json"));
        given(jsonEnvelopeLocalJusticeArea.payload())
                .willReturn(givenPayload("/referencedata.local-justice-area.json"));
        given(jsonEnvelopeCountries.payload())
                .willReturn(givenPayload("/referencedata.countries.json"));
        given(jsonEnvelopeLanguages.payload())
                .willReturn(givenPayload("/referencedata.languages.json"));
        given(jsonEnvelopeLocalAuthorities.payload())
                .willReturn(givenPayload("/referencedata.local-authorities.json"));
        given(jsonEnvelopePrisonNames.payload())
                .willReturn(givenPayload("/referencedata.prisons.json"));
        given(jsonEnvelopeCrownCourtNames.payload())
                .willReturn(givenPayload("/referencedata.crown-courts.json"));
        given(jsonEnvelopeScottishNICourtNames.payload())
                .willReturn(givenPayload("/referencedata.scottis-ni-courts.json"));
        given(jsonEnvelopeYouthCourtNames.payload())
                .willReturn(givenPayload("/referencedata.youth-courts.json"));

        //when
        final List<ResultPrompt> resultPrompts = underTest.loadResultPrompt(hearingDate);

        //then
        final List<ResultPrompt> resultPromptsWithFixedlist = resultPrompts.stream()
                .filter(resultPrompt -> ResultType.FIXL == resultPrompt.getType())
                .collect(Collectors.toList());

        assertThat(resultPromptsWithFixedlist, hasSize(4));

        assertThat(resultPromptsWithFixedlist.get(0).getFixedList(), hasSize(9));
        assertThat(resultPromptsWithFixedlist.get(0).getFixedList(), hasItems("Aberdeen Sheriff Court District","Bedfordshire YOT", "Blackfriars Crown Court", "Central Criminal Court", "Leicester and Rutland Magistrates' Court",
                "North and East Hertfordshire Magistrates' Court", "North and West Cumbria Magistrates' Court", "Sussex (Eastern) Magistrates' Court", "West Cross Court District"));

        assertThat(resultPromptsWithFixedlist.get(1).getFixedList(), hasSize(5));
        assertThat(resultPromptsWithFixedlist.get(1).getFixedList(), hasItems("London Alcohol Abstinence Monitor",
                "HLNY Alcohol Abstinence Monitor", "Midlands GPS Tag Monitoring Centre", "London GPS Tag Monitoring Centre"
        ));
    }

    @Test
    public void resultPromptShouldLoadAssociatedFixedlistOthers() throws Exception {
        //given
        final LocalDate hearingDate = LocalDate.now();
        given(resultsQueryService.getAllFixedLists(jsonEnvelope, hearingDate)).willReturn(responseEnvelope);
        given(resultsQueryService.getAllDefinitions(jsonEnvelope, hearingDate)).willReturn(responseEnvelope);
        given(resultsQueryService.getAllCourtCentre(jsonEnvelope)).willReturn(jsonEnvelopeDynaCourtCentre);
        given(resultsQueryService.getHearingTypes(jsonEnvelope)).willReturn(jsonEnvelopeDynaHearingType);
        given(resultsQueryService.getLocalJusticeAreas(jsonEnvelope)).willReturn(jsonEnvelopeLocalJusticeArea);
        given(resultsQueryService.getCountriesNames(jsonEnvelope)).willReturn(jsonEnvelopeCountries);
        given(resultsQueryService.getLanguages(jsonEnvelope)).willReturn(jsonEnvelopeLanguages);
        given(resultsQueryService.getLocalAuthorityNameAddress(jsonEnvelope)).willReturn(jsonEnvelopeLocalAuthorities);
        given(resultsQueryService.getPrisonNameAddress(jsonEnvelope)).willReturn(jsonEnvelopePrisonNames);
        given(resultsQueryService.getCrownCourtsNameAddress(jsonEnvelope)).willReturn(jsonEnvelopeCrownCourtNames);
        given(resultsQueryService.getScottishCourtAddress(jsonEnvelope)).willReturn(jsonEnvelopeScottishNICourtNames);
        given(resultsQueryService.getYouthCourtAddress(jsonEnvelope)).willReturn(jsonEnvelopeYouthCourtNames);

        given(responseEnvelope.payload())
                .willReturn(givenPayload("/referencedata.fixedlists.json"))
                .willReturn(givenPayload("/referencedata.result-definitions.json"));
        given(jsonEnvelopeDynaCourtCentre.payload())
                .willReturn(givenPayload("/referencedata.dyna.fixedlists.court.centre.json"));
        given(jsonEnvelopeDynaHearingType.payload())
                .willReturn(givenPayload("/referencedata.dyna.fixedlists.hearing.type.json"));
        given(jsonEnvelopeLocalJusticeArea.payload())
                .willReturn(givenPayload("/referencedata.local-justice-area.json"));
        given(jsonEnvelopeCountries.payload())
                .willReturn(givenPayload("/referencedata.countries.json"));
        given(jsonEnvelopeLanguages.payload())
                .willReturn(givenPayload("/referencedata.languages.json"));
        given(jsonEnvelopeLocalAuthorities.payload())
                .willReturn(givenPayload("/referencedata.local-authorities.json"));
        given(jsonEnvelopePrisonNames.payload())
                .willReturn(givenPayload("/referencedata.prisons.json"));
        given(jsonEnvelopeCrownCourtNames.payload())
                .willReturn(givenPayload("/referencedata.crown-courts.json"));
        given(jsonEnvelopeScottishNICourtNames.payload())
                .willReturn(givenPayload("/referencedata.scottis-ni-courts.json"));
        given(jsonEnvelopeYouthCourtNames.payload())
                .willReturn(givenPayload("/referencedata.youth-courts.json"));

        //when
        final List<ResultPrompt> resultPrompts = underTest.loadResultPrompt(hearingDate);

        //then
        final List<ResultPrompt> resultPromptsWithFixedlist = resultPrompts.stream()
                .filter(resultPrompt -> ResultType.FIXLO == resultPrompt.getType())
                .collect(Collectors.toList());

        assertThat(resultPromptsWithFixedlist, hasSize(1));

        assertThat(resultPromptsWithFixedlist.get(0).getFixedList(), hasSize(5));
        assertThat(resultPromptsWithFixedlist.get(0).getFixedList(), hasItems("EMS Manchester (curfew)", "HLNY Alcohol Abstinence Monitor",
                "London Alcohol Abstinence Monitor", "London GPS Tag Monitoring Centre", "Midlands GPS Tag Monitoring Centre"));
    }

    @Test
    public void resultPromptShouldLoadAssociatedFixedlistOthersMultiple() throws Exception {
        //given
        final LocalDate hearingDate = LocalDate.now();
        given(resultsQueryService.getAllFixedLists(jsonEnvelope, hearingDate)).willReturn(responseEnvelope);
        given(resultsQueryService.getAllDefinitions(jsonEnvelope, hearingDate)).willReturn(responseEnvelope);
        given(resultsQueryService.getAllCourtCentre(jsonEnvelope)).willReturn(jsonEnvelopeDynaCourtCentre);
        given(resultsQueryService.getHearingTypes(jsonEnvelope)).willReturn(jsonEnvelopeDynaHearingType);
        given(resultsQueryService.getLocalJusticeAreas(jsonEnvelope)).willReturn(jsonEnvelopeLocalJusticeArea);
        given(resultsQueryService.getCountriesNames(jsonEnvelope)).willReturn(jsonEnvelopeCountries);
        given(resultsQueryService.getLanguages(jsonEnvelope)).willReturn(jsonEnvelopeLanguages);
        given(resultsQueryService.getLocalAuthorityNameAddress(jsonEnvelope)).willReturn(jsonEnvelopeLocalAuthorities);
        given(resultsQueryService.getPrisonNameAddress(jsonEnvelope)).willReturn(jsonEnvelopePrisonNames);
        given(resultsQueryService.getCrownCourtsNameAddress(jsonEnvelope)).willReturn(jsonEnvelopeCrownCourtNames);
        given(resultsQueryService.getScottishCourtAddress(jsonEnvelope)).willReturn(jsonEnvelopeScottishNICourtNames);
        given(resultsQueryService.getYouthCourtAddress(jsonEnvelope)).willReturn(jsonEnvelopeYouthCourtNames);

        given(responseEnvelope.payload())
                .willReturn(givenPayload("/referencedata.fixedlists.json"))
                .willReturn(givenPayload("/referencedata.result-definitions.json"));
        given(jsonEnvelopeDynaCourtCentre.payload())
                .willReturn(givenPayload("/referencedata.dyna.fixedlists.court.centre.json"));
        given(jsonEnvelopeDynaHearingType.payload())
                .willReturn(givenPayload("/referencedata.dyna.fixedlists.hearing.type.json"));
        given(jsonEnvelopeLocalJusticeArea.payload())
                .willReturn(givenPayload("/referencedata.local-justice-area.json"));
        given(jsonEnvelopeCountries.payload())
                .willReturn(givenPayload("/referencedata.countries.json"));
        given(jsonEnvelopeLanguages.payload())
                .willReturn(givenPayload("/referencedata.languages.json"));
        given(jsonEnvelopeLocalAuthorities.payload())
                .willReturn(givenPayload("/referencedata.local-authorities.json"));
        given(jsonEnvelopePrisonNames.payload())
                .willReturn(givenPayload("/referencedata.prisons.json"));
        given(jsonEnvelopeCrownCourtNames.payload())
                .willReturn(givenPayload("/referencedata.crown-courts.json"));
        given(jsonEnvelopeScottishNICourtNames.payload())
                .willReturn(givenPayload("/referencedata.scottis-ni-courts.json"));
        given(jsonEnvelopeYouthCourtNames.payload())
                .willReturn(givenPayload("/referencedata.youth-courts.json"));

        //when
        final List<ResultPrompt> resultPrompts = underTest.loadResultPrompt(hearingDate);

        //then
        final List<ResultPrompt> resultPromptsWithFixedlist = resultPrompts.stream()
                .filter(resultPrompt -> ResultType.FIXLOM == resultPrompt.getType())
                .collect(Collectors.toList());

        assertThat(resultPromptsWithFixedlist, hasSize(1));

        assertThat(resultPromptsWithFixedlist.get(0).getFixedList(), hasSize(5));
        assertThat(resultPromptsWithFixedlist.get(0).getFixedList(), hasItems("EMS Manchester (curfew)", "HLNY Alcohol Abstinence Monitor",
                "London Alcohol Abstinence Monitor", "London GPS Tag Monitoring Centre", "Midlands GPS Tag Monitoring Centre"));
    }

    @Test
    public void loadResultDefinitionsWithRules() throws Exception {
        //given
        final LocalDate hearingDate = LocalDate.now();
        given(resultsQueryService.getAllDefinitions(jsonEnvelope, hearingDate)).willReturn(responseEnvelope);
        given(responseEnvelope.payload())
                .willReturn(givenPayload("/referencedata.result-definitions-with-rules.json"));

        //when
        final List<ResultDefinition> resultDefinitions = underTest.loadResultDefinition(hearingDate);

        //then
        List<ChildResultDefinition> childResultDefinitions = resultDefinitions.get(0).getChildResultDefinitions();
        assertThat(childResultDefinitions.size(), is(3));
        assertThat(childResultDefinitions.get(0).getRuleType(), is("mandatory"));
    }

    @Test
    public void shouldSetRollUpPrompts() throws IOException {
        final LocalDate hearingDate = LocalDate.now();
        given(resultsQueryService.getAllDefinitions(jsonEnvelope, hearingDate)).willReturn(responseEnvelope);
        given(responseEnvelope.payload())
                .willReturn(givenPayload("/referencedata.result-definitions-rollUpPrompts.json"));

        final List<ResultDefinition> resultDefinitions = underTest.loadResultDefinition(hearingDate);
        // when rollUpPrompts is null
        testRollUpPrompts(resultDefinitions.get(0), true, false, false, false);
        testRollUpPrompts(resultDefinitions.get(1), false, true, false, false);
        testRollUpPrompts(resultDefinitions.get(2), false, false, true, false);
        testRollUpPrompts(resultDefinitions.get(3), false, false, false, true);

        // when rollUpPrompts is set to false explicitly
        testRollUpPrompts(resultDefinitions.get(4), true, false, false, false);
        testRollUpPrompts(resultDefinitions.get(5), false, true, false, false);
        testRollUpPrompts(resultDefinitions.get(6), false, false, true, false);
        testRollUpPrompts(resultDefinitions.get(7), false, false, false, false);

        // when rollUpPrompts is set to true explicitly
        testRollUpPrompts(resultDefinitions.get(8), true, false, false, true);
        testRollUpPrompts(resultDefinitions.get(9), false, true, false, true);
        testRollUpPrompts(resultDefinitions.get(10), false, false, true, true);
        testRollUpPrompts(resultDefinitions.get(11), false, false, false, true);

    }

    private void testRollUpPrompts(final ResultDefinition resultDefinition,
                                   final Boolean publishedAsAPrompt,
                                   final Boolean excludedFromResults,
                                   final Boolean alwaysPublished,
                                   final Boolean rollUpPrompts) {
        assertThat(resultDefinition.getPublishedAsAPrompt(), is(publishedAsAPrompt));
        assertThat(resultDefinition.getExcludedFromResults(), is(excludedFromResults));
        assertThat(resultDefinition.getAlwaysPublished(), is(alwaysPublished));
        assertThat(resultDefinition.getRollUpPrompts(), is(rollUpPrompts));
    }
}
