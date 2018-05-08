package uk.gov.justice.ccr.notepad.result.loader;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertNotSame;
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

    @InjectMocks
    private ReadStoreResultLoader underTest;

    @Test
    public void loadResultDefinition() throws Exception {
        given(resultingQueryService.getAllDefinitions(jsonEnvelope)).willReturn(jsonEnvelope);
        given(jsonEnvelope.payloadAsJsonObject())
                .willReturn(givenPayload("/referencedata.result.definitions.json"));

        final List<ResultDefinition> resultDefinitions = underTest.loadResultDefinition();
        assertThat(resultDefinitions, hasSize(4));

        assertThat(resultDefinitions.get(0).getKeywords(), hasItems("restraining", "order", "period"));
        assertThat(resultDefinitions.get(1).getKeywords(), hasItems("restraop"));
    }

    @Test
    public void loadResultDefinitionSynonym() throws Exception {
        given(resultingQueryService.getAllDefinitionKeywordSynonyms(jsonEnvelope)).willReturn(jsonEnvelope);
        given(jsonEnvelope.payloadAsJsonObject())
                .willReturn(givenPayload("/referencedata.result.definition-keyword-synonyms.json"));

        assertThat(underTest.loadResultDefinitionSynonym(), hasSize(1));
    }


    @Test
    public void loadResultPromptSynonym() throws Exception {
        given(resultingQueryService.getAllPromptKeywordSynonyms(jsonEnvelope)).willReturn(jsonEnvelope);
        given(jsonEnvelope.payloadAsJsonObject())
                .willReturn(givenPayload("/referencedata.result.prompt-keyword-synonyms.json"));

        assertThat(underTest.loadResultPromptSynonym(), hasSize(2));
    }

    @Test
    public void loadResultPrompts() throws Exception {
        //given
        given(resultingQueryService.getAllPromptFixedLists(jsonEnvelope)).willReturn(jsonEnvelope);
        given(resultingQueryService.getAllDefinitions(jsonEnvelope)).willReturn(jsonEnvelope);
        given(jsonEnvelope.payloadAsJsonObject())
                .willReturn(givenPayload("/referencedata.result.prompt-fixedlists.json"))
                .willReturn(givenPayload("/referencedata.result.definitions.json"));

        //when
        final List<ResultPrompt> resultPrompts = underTest.loadResultPrompt();

        //then
        Set<String> fixedList = resultPrompts.get(0).getFixedList();
        assertThat(resultPrompts, hasSize(19));
        assertThat(resultPrompts.get(0).getPromptOrder(), is(1));
        assertThat(resultPrompts.get(0).getReference(), is(nullValue()));
        assertThat(resultPrompts.get(3).getKeywords(), hasItems("years"));
    }

    @Test
    public void resultPromptShouldLoadAssociatedFixedlist() throws Exception {
        //given
        given(resultingQueryService.getAllPromptFixedLists(jsonEnvelope)).willReturn(jsonEnvelope);
        given(resultingQueryService.getAllDefinitions(jsonEnvelope)).willReturn(jsonEnvelope);
        given(jsonEnvelope.payloadAsJsonObject())
                .willReturn(givenPayload("/referencedata.result.prompt-fixedlists.json"))
                .willReturn(givenPayload("/referencedata.result.definitions.json"));

        //when
        final List<ResultPrompt> resultPrompts = underTest.loadResultPrompt();

        //then
        List<ResultPrompt> resultPromptsWithFixedlist = resultPrompts.stream()
                .filter(resultPrompt -> ResultType.FIXL == resultPrompt.getType())
                .collect(Collectors.toList());

        assertThat(resultPromptsWithFixedlist, hasSize(2));

        assertThat(resultPromptsWithFixedlist.get(0).getFixedList(), hasSize(2));
        assertThat(resultPromptsWithFixedlist.get(0).getFixedList(), hasItems("Acquitted","Convicted"));

        assertThat(resultPromptsWithFixedlist.get(1).getFixedList(), hasSize(5));
        assertThat(resultPromptsWithFixedlist.get(1).getFixedList(), hasItems("London Alcohol Abstinence Monitor",
                "HLNY Alcohol Abstinence Monitor","Midlands GPS Tag Monitoring Centre","London GPS Tag Monitoring Centre"
        ));
    }
}
