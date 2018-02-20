package uk.gov.justice.ccr.notepad.result.loader;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.BDDMockito.given;
import static uk.gov.justice.ccr.notepad.util.FileUtil.givenPayload;

import uk.gov.justice.ccr.notepad.result.cache.model.ResultPrompt;
import uk.gov.justice.ccr.notepad.service.ResultingQueryService;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.List;

import com.google.common.collect.Sets;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ReadStoreResultLoaderTest {

    @Mock
     ResultingQueryService resultingQueryService;

    @Mock
    private JsonEnvelope jsonEnvelope;



    @InjectMocks
    private ReadStoreResultLoader testObj;

    @Test
    public void loadResultDefinition() throws Exception {
        given(resultingQueryService.getAllDefinitions(jsonEnvelope)).willReturn(jsonEnvelope);
        given(jsonEnvelope.payloadAsJsonObject())
                .willReturn(givenPayload("/referencedata.result.definitions.json")) ;

        assertThat(testObj.loadResultDefinition().size()
                , is(1)
        );
    }

    @Test
    public void loadResultDefinitionSynonym() throws Exception {
        given(resultingQueryService.getAllDefinitionKeywordSynonyms(jsonEnvelope)).willReturn(jsonEnvelope);
        given(jsonEnvelope.payloadAsJsonObject())
                .willReturn(givenPayload("/referencedata.result.definition-keyword-synonyms.json")) ;

        assertThat(testObj.loadResultDefinitionSynonym().size()
                , is(1)
        );
    }



    @Test
    public void loadResultPromptSynonym() throws Exception {
        given(resultingQueryService.getAllPromptKeywordSynonyms(jsonEnvelope)).willReturn(jsonEnvelope);
        given(jsonEnvelope.payloadAsJsonObject())
                .willReturn(givenPayload("/referencedata.result.prompt-keyword-synonyms.json")) ;

        assertThat(testObj.loadResultPromptSynonym().size()
                , is(2)
        );
    }

    @Test
    public void loadResultPrompt() throws Exception {
        //given
        given(resultingQueryService.getAllPromptFixedLists(jsonEnvelope)).willReturn(jsonEnvelope);
        given(resultingQueryService.getAllPrompts(jsonEnvelope)).willReturn(jsonEnvelope);
        given(jsonEnvelope.payloadAsJsonObject())
                .willReturn(givenPayload("/referencedata.result.prompt-fixedlists.json"))
                .willReturn(givenPayload("/referencedata.result.prompts.json"));

        //when
        List<ResultPrompt> resultPrompts = testObj.loadResultPrompt();

        //then
        assertThat(resultPrompts.size(), is(1));
        assertThat(resultPrompts.get(0).getFixedList(), is(Sets.newHashSet("Acquittal", "Convicted")));
        assertThat(resultPrompts.get(0).getPromptOrder(), is(1));
    }



}