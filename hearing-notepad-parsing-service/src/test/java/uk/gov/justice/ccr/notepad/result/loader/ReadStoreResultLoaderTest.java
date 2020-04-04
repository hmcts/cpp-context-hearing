package uk.gov.justice.ccr.notepad.result.loader;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.mockito.BDDMockito.given;
import static uk.gov.justice.ccr.notepad.util.FileUtil.givenPayload;

import uk.gov.justice.ccr.notepad.result.cache.model.ResultDefinition;
import uk.gov.justice.ccr.notepad.result.cache.model.ResultPrompt;
import uk.gov.justice.ccr.notepad.result.cache.model.ResultType;
import uk.gov.justice.ccr.notepad.service.ResultingQueryService;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

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
                .willReturn(givenPayload("/referencedata.result-definitions.json"));

        final List<ResultDefinition> resultDefinitions = underTest.loadResultDefinition(hearingDate);
        assertThat(resultDefinitions, hasSize(4));

        assertThat(resultDefinitions.get(0).getKeywords(), hasItems("restraining", "order", "period"));
        assertThat(resultDefinitions.get(1).getKeywords(), hasItems("restraop"));
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
        Set<String> fixedList = resultPrompts.get(0).getFixedList();
        assertThat(resultPrompts, hasSize(21));
        assertThat(resultPrompts.get(0).getPromptOrder(), is(1));
        assertThat(resultPrompts.get(0).getReference(), is(nullValue()));
        assertThat(resultPrompts.get(10).getReference(), is("HCHOUSE"));
        assertThat(resultPrompts.get(10).getType(), is(ResultType.FIXL));
        assertThat(resultPrompts.get(10).getFixedList().size(), is(2));
        assertThat(resultPrompts.get(11).getReference(), is("HTYPE"));
        assertThat(resultPrompts.get(11).getType(), is(ResultType.FIXL));
        assertThat(resultPrompts.get(11).getFixedList().size(), is(27));
        assertThat(resultPrompts.get(3).getKeywords(), hasItems("years"));
    }

    @Test
    public void resultPromptShouldLoadAssociatedFixedlist() throws Exception {
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
        List<ResultPrompt> resultPromptsWithFixedlist = resultPrompts.stream()
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
}
