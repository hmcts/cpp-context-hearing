package uk.gov.justice.ccr.notepad.process;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.justice.ccr.notepad.result.cache.model.ResultType.*;
import static uk.gov.justice.ccr.notepad.view.Part.State.RESOLVED;

import uk.gov.justice.ccr.notepad.result.cache.ResultCache;
import uk.gov.justice.ccr.notepad.result.cache.model.ResultType;
import uk.gov.justice.ccr.notepad.result.loader.FileResultLoader;
import uk.gov.justice.ccr.notepad.view.Part;
import uk.gov.justice.ccr.notepad.view.PromptChoice;
import uk.gov.justice.ccr.notepad.view.parser.PartsResolver;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;


public class ProcessorTest {

    Processor processor = new Processor();
    FileResultLoader fileResultLoader = new FileResultLoader();
    ResultCache resultCache = new ResultCache();
    ResultPromptMatcher resultPromptMatcher = new ResultPromptMatcher();
    ResultDefinitionMatcher resultDefinitionMatcher = new ResultDefinitionMatcher();
    FindPromptsIndexesByKeyword findPromptsIndexesByKeyword = new FindPromptsIndexesByKeyword();
    ComparePromptKeywordsUsingIndexes comparePromptKeywordsUsingIndexes = new ComparePromptKeywordsUsingIndexes();
    FindPromptSynonyms findPromptSynonyms = new FindPromptSynonyms();
    FindDefinitionsIndexesByKeyword findDefinitionsIndexesByKeyword = new FindDefinitionsIndexesByKeyword();
    CompareDefinitionKeywordsUsingIndexes compareDefinitionKeywordsUsingIndexes = new CompareDefinitionKeywordsUsingIndexes();
    FindDefinitionPartialMatchSynonyms findDefinitionPartialMatchSynonyms = new FindDefinitionPartialMatchSynonyms();
    FindDefinitionsByShortCodes findDefinitionsByShortCodes = new FindDefinitionsByShortCodes();
    FindDefinitionExactMatchSynonyms findDefinitionExactMatchSynonyms = new FindDefinitionExactMatchSynonyms();
    GroupResultByIndex groupResultByIndex = new GroupResultByIndex();
    Time24HoursMatcher time24HoursMatcher = new Time24HoursMatcher();
    DateMatcher dateMatcher = new DateMatcher();

    @Before
    public void init() throws ExecutionException {
        resultCache.setResultLoader(fileResultLoader);
        resultCache.lazyLoad(null);
        findPromptsIndexesByKeyword.resultCache = resultCache;
        comparePromptKeywordsUsingIndexes.resultCache = resultCache;
        findPromptSynonyms.resultCache = resultCache;
        findDefinitionExactMatchSynonyms.resultCache = resultCache;
        findDefinitionsByShortCodes.resultCache = resultCache;
        findDefinitionPartialMatchSynonyms.resultCache = resultCache;
        resultPromptMatcher.findPromptsIndexesByKeyword = findPromptsIndexesByKeyword;
        resultPromptMatcher.comparePromptKeywordsUsingIndexes = comparePromptKeywordsUsingIndexes;
        resultPromptMatcher.findPromptSynonyms = findPromptSynonyms;
        resultPromptMatcher.groupResultByIndex = groupResultByIndex;
        findDefinitionsIndexesByKeyword.resultCache = resultCache;
        compareDefinitionKeywordsUsingIndexes.resultCache = resultCache;
        findDefinitionExactMatchSynonyms.resultCache = resultCache;
        findDefinitionsByShortCodes.resultCache = resultCache;
        findDefinitionPartialMatchSynonyms.resultCache = resultCache;
        resultDefinitionMatcher.findDefinitionsIndexesByKeyword = findDefinitionsIndexesByKeyword;
        resultDefinitionMatcher.compareDefinitionKeywordsUsingIndexes = compareDefinitionKeywordsUsingIndexes;
        resultDefinitionMatcher.findDefinitionExactMatchSynonyms = findDefinitionExactMatchSynonyms;
        resultDefinitionMatcher.groupResultByIndex = groupResultByIndex;
        resultDefinitionMatcher.findDefinitionPartialMatchSynonyms = findDefinitionPartialMatchSynonyms;
        resultDefinitionMatcher.findDefinitionsByShortCodes = findDefinitionsByShortCodes;
        processor.resultPromptMatcher = resultPromptMatcher;
        processor.resultDefinitionMatcher = resultDefinitionMatcher;
        processor.resultCache = resultCache;
        processor.time24HoursMatcher = time24HoursMatcher;
        processor.dateMatcher = dateMatcher;
    }

    @Test
    public void processResultDefinition1() throws Exception {
        List<Part> parts = new PartsResolver().getParts("Imp 2 years");
        List<String> values = parts.stream().map(Part::getValueAsString).collect(toList());

        Knowledge knowledge = processor.processParts(values);

        assertThat(
                knowledge.getResultDefinitionParts().size()
                , is(1)
        );
        assertThat(
                knowledge.getResultPromptParts().size()
                , is(2)
        );

    }

    @Test
    public void processResultDefinition2() throws Exception {
        List<Part> parts = new PartsResolver().getParts("Pard $367 conc conc [33] m");
        List<String> values = parts.stream().map(Part::getValueAsString).collect(toList());

        Knowledge knowledge = processor.processParts(values);

        assertThat(
                knowledge.getResultDefinitionParts().size()
                , is(1)
        );
        assertThat(
                knowledge.getResultPromptParts().size()
                , is(4)
        );
        assertThat(
                knowledge.getResultPromptParts().get("m").getType()
                , is(DURATION)
        );

    }

    @Test
    public void processResultDefinition3() throws Exception {
        List<Part> parts = new PartsResolver().getParts("sus imp 4 yr 8 mo $5666 conc Early not apply [2ewe wew[wwe] [wewe ew]");
        List<String> values = parts.stream().map(Part::getValueAsString).collect(toList());

        Knowledge knowledge = processor.processParts(values);
        assertThat(
                knowledge.getResultDefinitionParts().size()
                , is(2)
        );
        assertThat(
                knowledge.getResultPromptParts().size()
                , is(11)
        );
    }

    @Test
    public void processResultDefinition4() throws Exception {
        List<Part> parts = new PartsResolver().getParts("Spec cust conc 4 yr 8 mo $5666 conc Early not apply [2ewe wew[wwe] [wewe ew]");
        List<String> values = parts.stream().map(Part::getValueAsString).collect(toList());

        Knowledge knowledge = processor.processParts(values);
        assertThat(
                knowledge.getResultDefinitionParts().size()
                , is(3)
        );
        assertThat(
                knowledge.getResultPromptParts().size()
                , is(10)
        );
    }

    @Test
    public void processResultDefinition5() throws Exception {
        List<Part> parts = new PartsResolver().getParts("vs 2 yr 3m 3£3 02:25 30/11/1980");
        List<String> values = parts.stream().map(Part::getValueAsString).collect(toList());

        Knowledge knowledge = processor.processParts(values);

        assertThat(
                knowledge.getResultDefinitionParts().size()
                , is(1)
        );
        assertThat(
                knowledge.getResultPromptParts().size()
                , is(6)
        );
        assertThat(
                knowledge.getResultPromptParts().get("2").getType()
                , is(INT)
        );
        assertThat(
                knowledge.getResultPromptParts().get("yr").getType()
                , is(DURATION)
        );
        assertThat(
                knowledge.getResultPromptParts().get("3m").getType()
                , is(DURATION)
        );
        assertThat(
                knowledge.getResultPromptParts().get("3£3").getType()
                , is(TXT)
        );
        assertThat(
                knowledge.getResultPromptParts().get("02:25").getType()
                , is(TIME)
        );
        assertThat(
                knowledge.getResultPromptParts().get("30/11/1980").getType()
                , is(DATE)
        );


    }

    @Test
    public void processResultDefinition6() throws Exception {
        List<Part> parts = new PartsResolver().getParts("life imp release 2 yr mand conc £0 £");
        List<String> values = parts.stream().map(Part::getValueAsString).collect(toList());

        Knowledge knowledge = processor.processParts(values);

        assertThat(
                knowledge.getResultDefinitionParts().size()
                , is(3)
        );
        assertThat(
                knowledge.getResultPromptParts().size()
                , is(6)
        );
        assertThat(
                knowledge.getResultDefinitionParts().get("life").getState()
                , is(RESOLVED)
        );
        assertThat(
                knowledge.getResultDefinitionParts().get("mand").getState()
                , is(RESOLVED)
        );
        assertThat(
                knowledge.getResultPromptParts().get("yr").getType()
                , is(DURATION)
        );
        assertThat(
                knowledge.getResultPromptParts().get("£0").getType()
                , is(CURR)
        );
        assertThat(
                knowledge.getResultPromptParts().get("£").getType()
                , is(TXT)
        );
        assertThat(
                knowledge.getResultPromptParts().get("release").getType()
                , is(TXT)
        );
        assertThat(
                knowledge.getResultPromptParts().get("conc").getType()
                , is(BOOLEAN)
        );
    }

    @Test
    public void processResultDefinition7() throws Exception {
        List<Part> parts = new PartsResolver().getParts("alc req pard shope 2 yr mand conc £300 344");
        List<String> values = parts.stream().map(Part::getValueAsString).collect(toList());

        Knowledge knowledge = processor.processParts(values);

        assertThat(
                knowledge.getResultDefinitionParts().size()
                , is(5)
        );
        assertThat(
                knowledge.getResultPromptParts().size()
                , is(5)
        );
        assertThat(
                knowledge.getResultDefinitionParts().get("pard").getResultChoices().size()
                , is(1)
        );
        assertThat(
                knowledge.getResultDefinitionParts().get("shope").getResultChoices().size()
                , is(1)
        );
        assertThat(
                knowledge.getResultDefinitionParts().get("req").getResultChoices().size()
                , is(11)
        );
        assertThat(
                knowledge.getResultDefinitionParts().get("alc").getResultChoices().size()
                , is(5)
        );
        assertThat(
                knowledge.getResultPromptParts().get("yr").getType()
                , is(DURATION)
        );
        assertThat(
                knowledge.getResultPromptParts().get("£300").getType()
                , is(CURR)
        );
        assertThat(
                knowledge.getResultPromptParts().get("344").getType()
                , is(INT)
        );
        assertThat(
                knowledge.getResultPromptParts().get("conc").getType()
                , is(BOOLEAN)
        );
    }
    @Test
    public void processResultPrompt1() throws Exception {
        List<Part> parts = new PartsResolver().getParts("imp sus");
        List<String> values = parts.stream().map(Part::getValueAsString).collect(toList());

        Knowledge knowledge = processor.processResultPrompt(processor.processParts(values).getResultDefinitionParts().get("imp").getCode());


        assertThat(
                knowledge.getPromptChoices().size()
                , is(13)
        );
    }

    @Test
    public void processResultPrompt2() throws Exception {
        List<Part> parts = new PartsResolver().getParts("f");
        List<String> values = parts.stream().map(Part::getValueAsString).collect(toList());

        Knowledge knowledge = processor.processResultPrompt(processor.processParts(values).getResultDefinitionParts().get("f").getCode());


        assertThat(
                knowledge.getPromptChoices().size()
                , is(1)
        );
    }

    @Test
    public void processResultPrompt3() throws Exception {
        List<Part> parts = new PartsResolver().getParts("imp");
        List<String> values = parts.stream().map(Part::getValueAsString).collect(toList());

        Knowledge knowledge = processor.processResultPrompt(processor.processParts(values).getResultDefinitionParts().get("imp").getCode());


        assertThat(
                knowledge.getPromptChoices().size()
                , is(14)
        );
    }

    @Ignore
    public void processResultPromptWithFixedList() throws Exception {
        List<Part> parts = new PartsResolver().getParts("restraop conv");
        List<String> values = parts.stream().map(Part::getValueAsString).collect(toList());

        Knowledge knowledge = processor.processResultPrompt(processor.processParts(values).getResultDefinitionParts().get("restraop").getCode());
        Optional<PromptChoice> pc = knowledge.getPromptChoices().stream().filter(p -> p.getLabel().equals("Conviction / acquittal")).findFirst();

        assertThat(pc.isPresent(), is(true));
        assertThat(pc.get().getType(), is(ResultType.FIXL));
        assertThat(pc.get().getFixedList(), is(Sets.newHashSet("Acquitted", "Convicted")));
    }

    public Processor getProcessor() {
        return processor;
    }
}