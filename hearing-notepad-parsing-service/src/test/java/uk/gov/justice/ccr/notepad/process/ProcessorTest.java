package uk.gov.justice.ccr.notepad.process;

import static java.time.LocalDate.now;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.justice.ccr.notepad.result.cache.model.ResultType.BOOLEAN;
import static uk.gov.justice.ccr.notepad.result.cache.model.ResultType.CURR;
import static uk.gov.justice.ccr.notepad.result.cache.model.ResultType.DATE;
import static uk.gov.justice.ccr.notepad.result.cache.model.ResultType.DURATION;
import static uk.gov.justice.ccr.notepad.result.cache.model.ResultType.INT;
import static uk.gov.justice.ccr.notepad.result.cache.model.ResultType.TIME;
import static uk.gov.justice.ccr.notepad.result.cache.model.ResultType.TXT;
import static uk.gov.justice.ccr.notepad.view.Part.State.RESOLVED;

import uk.gov.justice.ccr.notepad.result.cache.CacheFactory;
import uk.gov.justice.ccr.notepad.result.cache.ResultCache;
import uk.gov.justice.ccr.notepad.result.cache.model.ChildResultDefinition;
import uk.gov.justice.ccr.notepad.result.cache.model.ResultDefinition;
import uk.gov.justice.ccr.notepad.result.cache.model.ResultType;
import uk.gov.justice.ccr.notepad.result.loader.FileResultLoader;
import uk.gov.justice.ccr.notepad.view.Part;
import uk.gov.justice.ccr.notepad.view.PromptChoice;
import uk.gov.justice.ccr.notepad.view.parser.PartsResolver;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

import com.google.common.cache.LoadingCache;
import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ProcessorTest {

    @InjectMocks
    Processor processor = new Processor();

    @Spy
    FileResultLoader fileResultLoader = new FileResultLoader();

    @Spy
    @InjectMocks
    ResultCache resultCache = new ResultCache();

    ResultPromptMatcher resultPromptMatcher = new ResultPromptMatcher();
    @Spy
    ResultDefinitionMatcher resultDefinitionMatcher = new ResultDefinitionMatcher();
    @Spy
    FindPromptsIndexesByKeyword findPromptsIndexesByKeyword = new FindPromptsIndexesByKeyword();
    @Spy
    ComparePromptKeywordsUsingIndexes comparePromptKeywordsUsingIndexes = new ComparePromptKeywordsUsingIndexes();
    @Spy
    FindPromptSynonyms findPromptSynonyms = new FindPromptSynonyms();
    @Spy
    FindDefinitionsIndexesByKeyword findDefinitionsIndexesByKeyword = new FindDefinitionsIndexesByKeyword();
    @Spy
    CompareDefinitionKeywordsUsingIndexes compareDefinitionKeywordsUsingIndexes = new CompareDefinitionKeywordsUsingIndexes();
    @Spy
    FindDefinitionPartialMatchSynonyms findDefinitionPartialMatchSynonyms = new FindDefinitionPartialMatchSynonyms();
    @Spy
    FindDefinitionsByShortCodes findDefinitionsByShortCodes = new FindDefinitionsByShortCodes();
    FindDefinitionExactMatchSynonyms findDefinitionExactMatchSynonyms = new FindDefinitionExactMatchSynonyms();
    GroupResultByIndex groupResultByIndex = new GroupResultByIndex();
    Time24HoursMatcher time24HoursMatcher = new Time24HoursMatcher();
    DateMatcher dateMatcher = new DateMatcher();
    @Mock
    private CacheFactory cacheFactory;
    @Mock
    private LoadingCache<String, Object> cache;

    @Before
    public void init() throws ExecutionException {
        when(cacheFactory.build()).thenReturn(cache);
        final ConcurrentHashMap<String, Object> cacheValue = new ConcurrentHashMap<>();
        when(cache.asMap()).thenReturn(cacheValue);

        resultCache.lazyLoad(null, now());

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
        processor.time24HoursMatcher = time24HoursMatcher;
        processor.dateMatcher = dateMatcher;
        processor.resultCache = resultCache;
    }

    @Test
    public void processResultDefinition1() throws Exception {
        final List<Part> parts = new PartsResolver().getParts("Imp 2 years");
        final List<String> values = parts.stream().map(Part::getValueAsString).collect(toList());

        final Knowledge knowledge = processor.processParts(values, now());

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
        final List<Part> parts = new PartsResolver().getParts("Pard $367 conc conc [33] m");
        final List<String> values = parts.stream().map(Part::getValueAsString).collect(toList());

        final Knowledge knowledge = processor.processParts(values, now());

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
        final List<Part> parts = new PartsResolver().getParts("sus imp 4 yr 8 mo $5666 conc Early not apply [2ewe wew[wwe] [wewe ew]");
        final List<String> values = parts.stream().map(Part::getValueAsString).collect(toList());

        final Knowledge knowledge = processor.processParts(values, now());
        assertThat(knowledge.getResultDefinitionParts().entrySet(), hasSize(2));
        assertThat(knowledge.getResultPromptParts().entrySet(), hasSize(11));
    }

    @Test
    public void processResultDefinition4() throws Exception {
        final List<Part> parts = new PartsResolver().getParts("Spec cust conc 4 yr 8 mo $5666 conc Early not apply [2ewe wew[wwe] [wewe ew]");
        final List<String> values = parts.stream().map(Part::getValueAsString).collect(toList());

        final Knowledge knowledge = processor.processParts(values, now());
        assertThat(knowledge.getResultDefinitionParts().entrySet(), hasSize(4));
        assertThat(knowledge.getResultPromptParts().entrySet(), hasSize(9));
    }

    @Test
    public void processResultDefinition5() throws Exception {
        final List<Part> parts = new PartsResolver().getParts("vs 2 yr 3m 3£3 02:25 30/11/1980");
        final List<String> values = parts.stream().map(Part::getValueAsString).collect(toList());

        final Knowledge knowledge = processor.processParts(values, now());

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
        final List<Part> parts = new PartsResolver().getParts("life imp release 2 yr mand conc £0 £");
        final List<String> values = parts.stream().map(Part::getValueAsString).collect(toList());

        final Knowledge knowledge = processor.processParts(values, now());

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
        final List<Part> parts = new PartsResolver().getParts("alc req pard shope 2 yr mand conc £300 344");
        final List<String> values = parts.stream().map(Part::getValueAsString).collect(toList());

        final Knowledge knowledge = processor.processParts(values, now());

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
                knowledge.getResultDefinitionParts().get("alc").getResultChoices()
                , hasSize(7)
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
        final List<Part> parts = new PartsResolver().getParts("imp sus");
        final List<String> values = parts.stream().map(Part::getValueAsString).collect(toList());

        final Knowledge knowledge = processor.processResultPrompt(processor.processParts(values, now()).getResultDefinitionParts().get("imp").getCode(), now());

        assertThat(knowledge.getPromptChoices(), hasSize(13));
    }

    @Test
    public void processResultPrompt2() throws Exception {
        final List<Part> parts = new PartsResolver().getParts("f");
        final List<String> values = parts.stream().map(Part::getValueAsString).collect(toList());

        final Knowledge knowledge = processor.processResultPrompt(processor.processParts(values, now()).getResultDefinitionParts().get("f").getCode(), now());


        assertThat(
                knowledge.getPromptChoices().size()
                , is(1)
        );
    }

    @Test
    public void processResultPrompt3() throws Exception {
        final List<Part> parts = new PartsResolver().getParts("imp");
        final List<String> values = parts.stream().map(Part::getValueAsString).collect(toList());

        final Knowledge knowledge = processor.processResultPrompt(processor.processParts(values, now()).getResultDefinitionParts().get("imp").getCode(), now());


        assertThat(
                knowledge.getPromptChoices().size()
                , is(14)
        );
    }

    @Test
    public void processResultPromptWithFixedList() throws Exception {
        final List<Part> parts = new PartsResolver().getParts("restraop conv");
        final List<String> values = parts.stream().map(Part::getValueAsString).collect(toList());

        final Knowledge knowledge = processor.processResultPrompt(processor.processParts(values, now()).getResultDefinitionParts().get("restraop").getCode(), now());
        final Optional<PromptChoice> pc = knowledge.getPromptChoices().stream().filter(p -> p.getLabel().equals("Conviction / acquittal")).findFirst();

        assertThat(pc.isPresent(), is(true));
        assertThat(pc.get().getType(), is(ResultType.FIXL));
        assertThat(pc.get().getFixedList(), is(Sets.newHashSet("Acquitted", "Convicted")));
    }

    @Test
    public void retrieveChildResultDefinitionDetailWhenNoResultDefinitionRules() {

        final List<Part> parts = new PartsResolver().getParts("imp sus");
        final List<String> values = parts.stream().map(Part::getValueAsString).collect(toList());
        final String resultDefinitionId = processor.processParts(values, now()).getResultDefinitionParts().get("imp").getCode();

        final ChildResultDefinitionDetail childResultDefinitionDetail = processor.retrieveChildResultDefinitionDetail(resultDefinitionId, now());

        assertThat(childResultDefinitionDetail, nullValue());
    }

    @Test
    public void shouldSetComponentTypeIfPromptReferenceIsHCROOM() {
        final Knowledge knowledge = processor.processResultPrompt("fbed768b-ee95-4434-87c8-e81cbc8d24c8", now());
        assertThat(knowledge.getPromptChoices().stream().filter(promptChoice -> "HCROOM".equals(promptChoice.getComponentType())).count(), is(1L));
    }

    @Test
    public void shouldSetComponentTypeIfPromptReferenceIsHCHOUSE() {
        final Knowledge knowledge = processor.processResultPrompt("fbed768b-ee95-4434-87c8-e81cbc8d24c8", now());
        assertThat(knowledge.getPromptChoices().stream().filter(promptChoice -> "HCHOUSE".equals(promptChoice.getComponentType())).count(), is(1L));
    }

    @Test
    public void shouldSetComponentTypeIfResultPromptRuleIsOneOf() {
        final Knowledge knowledge = processor.processResultPrompt("7f80f0f4-ae8a-4965-9cc6-ef5c2c5caef7", now());
        assertThat(knowledge.getPromptChoices().stream().filter(promptChoice -> "ONEOF".equals(promptChoice.getComponentType())).count(), is(6L));
    }

    public Processor getProcessor() {
        return processor;
    }
}
