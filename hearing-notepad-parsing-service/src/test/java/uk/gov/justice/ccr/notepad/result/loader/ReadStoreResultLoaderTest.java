package uk.gov.justice.ccr.notepad.result.loader;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.justice.ccr.notepad.result.cache.model.ChildResultDefinition;
import uk.gov.justice.ccr.notepad.result.cache.model.ResultDefinition;
import uk.gov.justice.ccr.notepad.result.cache.model.ResultPrompt;
import uk.gov.justice.ccr.notepad.result.cache.model.ResultType;
import uk.gov.justice.ccr.notepad.service.ResultingQueryService;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.Boolean.TRUE;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.mockito.BDDMockito.given;
import static uk.gov.justice.ccr.notepad.util.FileUtil.givenPayload;

@RunWith(MockitoJUnitRunner.class)
public class ReadStoreResultLoaderTest {

    @Mock
    private ResultingQueryService resultingQueryService;

    @Mock
    private JsonEnvelope jsonEnvelope;

    @Mock
    private JsonEnvelope jsonEnvelopeDynaCourtCentre;

    @Mock
    private JsonEnvelope jsonEnvelopeDynaHearingType;

    @InjectMocks
    private ReadStoreResultLoader underTest;

    @Test
    public void loadResultDefinition() throws Exception {
        final LocalDate hearingDate = LocalDate.now();
        given(resultingQueryService.getAllDefinitions(jsonEnvelope, hearingDate)).willReturn(jsonEnvelope);
        given(jsonEnvelope.payloadAsJsonObject())
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
    }

    @Test
    public void loadResultDefinitionNoWordGroups() throws Exception {
        final LocalDate hearingDate = LocalDate.now();
        given(resultingQueryService.getAllDefinitions(jsonEnvelope, hearingDate)).willReturn(jsonEnvelope);
        given(jsonEnvelope.payloadAsJsonObject())
                .willReturn(givenPayload("/referencedata.result-definitions-no-word-groups.json"));

        final List<ResultDefinition> resultDefinitions = underTest.loadResultDefinition(hearingDate);
        assertThat(resultDefinitions, hasSize(2));
    }

    @Test
    public void loadResultDefinitionSynonym() throws Exception {
        final LocalDate hearingDate = LocalDate.now();
        given(resultingQueryService.getAllDefinitionWordSynonyms(jsonEnvelope, hearingDate)).willReturn(jsonEnvelope);
        given(jsonEnvelope.payloadAsJsonObject())
                .willReturn(givenPayload("/referencedata.result-word-synonyms.json"));

        assertThat(underTest.loadResultDefinitionSynonym(hearingDate), hasSize(3));
    }


    @Test
    public void loadResultPromptSynonym() throws Exception {
        final LocalDate hearingDate = LocalDate.now();
        given(resultingQueryService.getAllResultPromptWordSynonyms(jsonEnvelope, hearingDate)).willReturn(jsonEnvelope);
        given(jsonEnvelope.payloadAsJsonObject())
                .willReturn(givenPayload("/referencedata.result-prompt-synonyms.json"));

        assertThat(underTest.loadResultPromptSynonym(hearingDate), hasSize(2));
    }

    @Test
    public void loadResultPrompts() throws Exception {
        //given
        final LocalDate hearingDate = LocalDate.now();
        given(resultingQueryService.getAllFixedLists(jsonEnvelope, hearingDate)).willReturn(jsonEnvelope);
        given(resultingQueryService.getAllDefinitions(jsonEnvelope, hearingDate)).willReturn(jsonEnvelope);
        given(resultingQueryService.getAllCourtCentre(jsonEnvelope)).willReturn(jsonEnvelopeDynaCourtCentre);
        given(resultingQueryService.getHearingTypes(jsonEnvelope)).willReturn(jsonEnvelopeDynaHearingType);
        given(jsonEnvelope.payloadAsJsonObject())
                .willReturn(givenPayload("/referencedata.fixedlists.json"))
                .willReturn(givenPayload("/referencedata.result-definitions.json"));
        given(jsonEnvelopeDynaCourtCentre.payloadAsJsonObject())
                .willReturn(givenPayload("/referencedata.dyna.fixedlists.court.centre.json"));
        given(jsonEnvelopeDynaHearingType.payloadAsJsonObject())
                .willReturn(givenPayload("/referencedata.dyna.fixedlists.hearing.type.json"));

        //when
        final List<ResultPrompt> resultPrompts = underTest.loadResultPrompt(hearingDate);

        //then
        final Set<String> fixedList = resultPrompts.get(0).getFixedList();
        assertThat(resultPrompts, hasSize(23));
        assertThat(resultPrompts.get(0).getPromptOrder(), is(1));
        assertThat(resultPrompts.get(0).getReference(), is(nullValue()));
        assertThat(resultPrompts.get(0).getDurationSequence(), is(1));
        assertThat(resultPrompts.get(10).getReference(), is("HCHOUSE"));
        assertThat(resultPrompts.get(10).getType(), is(ResultType.FIXL));
        assertThat(resultPrompts.get(10).getFixedList().size(), is(2));
        assertThat(resultPrompts.get(10).getDurationSequence(), is(0));
        assertThat(resultPrompts.get(11).getReference(), is("HTYPE"));
        assertThat(resultPrompts.get(11).getType(), is(ResultType.FIXL));
        assertThat(resultPrompts.get(11).getFixedList().size(), is(27));
        assertThat(resultPrompts.get(12).getType(), is(ResultType.FIXLO));
        assertThat(resultPrompts.get(13).getType(), is(ResultType.FIXLOM));
        assertThat(resultPrompts.get(3).getKeywords(), hasItems("years"));
        assertThat(resultPrompts.get(0).getHidden(), is(false));
    }

    @Test
    public void shouldLoadResultPromptsWithLegacyData() throws Exception {
        //given
        final LocalDate hearingDate = LocalDate.now();
        given(resultingQueryService.getAllFixedLists(jsonEnvelope, hearingDate)).willReturn(jsonEnvelope);
        given(resultingQueryService.getAllDefinitions(jsonEnvelope, hearingDate)).willReturn(jsonEnvelope);
        given(resultingQueryService.getAllCourtCentre(jsonEnvelope)).willReturn(jsonEnvelopeDynaCourtCentre);
        given(resultingQueryService.getHearingTypes(jsonEnvelope)).willReturn(jsonEnvelopeDynaHearingType);
        given(jsonEnvelope.payloadAsJsonObject())
                .willReturn(givenPayload("/referencedata.fixedlists.json"))
                .willReturn(givenPayload("/referencedata.result-definitions-legacy.json"));
        given(jsonEnvelopeDynaCourtCentre.payloadAsJsonObject())
                .willReturn(givenPayload("/referencedata.dyna.fixedlists.court.centre.json"));
        given(jsonEnvelopeDynaHearingType.payloadAsJsonObject())
                .willReturn(givenPayload("/referencedata.dyna.fixedlists.hearing.type.json"));

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
        given(resultingQueryService.getAllFixedLists(jsonEnvelope, hearingDate)).willReturn(jsonEnvelope);
        given(resultingQueryService.getAllDefinitions(jsonEnvelope, hearingDate)).willReturn(jsonEnvelope);
        given(resultingQueryService.getAllCourtCentre(jsonEnvelope)).willReturn(jsonEnvelopeDynaCourtCentre);
        given(resultingQueryService.getHearingTypes(jsonEnvelope)).willReturn(jsonEnvelopeDynaHearingType);
        given(jsonEnvelope.payloadAsJsonObject())
                .willReturn(givenPayload("/referencedata.fixedlists.json"))
                .willReturn(givenPayload("/referencedata.result-definitions.json"));
        given(jsonEnvelopeDynaCourtCentre.payloadAsJsonObject())
                .willReturn(givenPayload("/referencedata.dyna.fixedlists.court.centre.json"));
        given(jsonEnvelopeDynaHearingType.payloadAsJsonObject())
                .willReturn(givenPayload("/referencedata.dyna.fixedlists.hearing.type.json"));
        //when
        final List<ResultPrompt> resultPrompts = underTest.loadResultPrompt(hearingDate);

        //then
        final List<ResultPrompt> resultPromptsWithFixedlist = resultPrompts.stream()
                .filter(resultPrompt -> ResultType.FIXL == resultPrompt.getType())
                .collect(Collectors.toList());

        assertThat(resultPromptsWithFixedlist, hasSize(4));

        assertThat(resultPromptsWithFixedlist.get(0).getFixedList(), hasSize(2));
        assertThat(resultPromptsWithFixedlist.get(0).getFixedList(), hasItems("Acquitted", "Convicted"));

        assertThat(resultPromptsWithFixedlist.get(1).getFixedList(), hasSize(5));
        assertThat(resultPromptsWithFixedlist.get(1).getFixedList(), hasItems("London Alcohol Abstinence Monitor",
                "HLNY Alcohol Abstinence Monitor", "Midlands GPS Tag Monitoring Centre", "London GPS Tag Monitoring Centre"
        ));
    }

    @Test
    public void resultPromptShouldLoadAssociatedFixedlistOthers() throws Exception {
        //given
        final LocalDate hearingDate = LocalDate.now();
        given(resultingQueryService.getAllFixedLists(jsonEnvelope, hearingDate)).willReturn(jsonEnvelope);
        given(resultingQueryService.getAllDefinitions(jsonEnvelope, hearingDate)).willReturn(jsonEnvelope);
        given(resultingQueryService.getAllCourtCentre(jsonEnvelope)).willReturn(jsonEnvelopeDynaCourtCentre);
        given(resultingQueryService.getHearingTypes(jsonEnvelope)).willReturn(jsonEnvelopeDynaHearingType);
        given(jsonEnvelope.payloadAsJsonObject())
                .willReturn(givenPayload("/referencedata.fixedlists.json"))
                .willReturn(givenPayload("/referencedata.result-definitions.json"));
        given(jsonEnvelopeDynaCourtCentre.payloadAsJsonObject())
                .willReturn(givenPayload("/referencedata.dyna.fixedlists.court.centre.json"));
        given(jsonEnvelopeDynaHearingType.payloadAsJsonObject())
                .willReturn(givenPayload("/referencedata.dyna.fixedlists.hearing.type.json"));
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
        given(resultingQueryService.getAllFixedLists(jsonEnvelope, hearingDate)).willReturn(jsonEnvelope);
        given(resultingQueryService.getAllDefinitions(jsonEnvelope, hearingDate)).willReturn(jsonEnvelope);
        given(resultingQueryService.getAllCourtCentre(jsonEnvelope)).willReturn(jsonEnvelopeDynaCourtCentre);
        given(resultingQueryService.getHearingTypes(jsonEnvelope)).willReturn(jsonEnvelopeDynaHearingType);
        given(jsonEnvelope.payloadAsJsonObject())
                .willReturn(givenPayload("/referencedata.fixedlists.json"))
                .willReturn(givenPayload("/referencedata.result-definitions.json"));
        given(jsonEnvelopeDynaCourtCentre.payloadAsJsonObject())
                .willReturn(givenPayload("/referencedata.dyna.fixedlists.court.centre.json"));
        given(jsonEnvelopeDynaHearingType.payloadAsJsonObject())
                .willReturn(givenPayload("/referencedata.dyna.fixedlists.hearing.type.json"));
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
        given(resultingQueryService.getAllDefinitions(jsonEnvelope, hearingDate)).willReturn(jsonEnvelope);
        given(jsonEnvelope.payloadAsJsonObject())
                .willReturn(givenPayload("/referencedata.result-definitions-with-rules.json"));

        //when
        final List<ResultDefinition> resultDefinitions = underTest.loadResultDefinition(hearingDate);

        //then
        List<ChildResultDefinition> childResultDefinitions = resultDefinitions.get(0).getChildResultDefinitions();
        assertThat(childResultDefinitions.size(), is(3));
        assertThat(childResultDefinitions.get(0).getRuleType(), is("mandatory"));
    }
}
