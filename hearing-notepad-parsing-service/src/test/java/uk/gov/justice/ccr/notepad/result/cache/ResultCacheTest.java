package uk.gov.justice.ccr.notepad.result.cache;

import com.google.common.cache.LoadingCache;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.justice.ccr.notepad.result.cache.model.ResultDefinition;
import uk.gov.justice.ccr.notepad.result.cache.model.ResultPrompt;
import uk.gov.justice.ccr.notepad.result.cache.model.ResultPromptSynonym;
import uk.gov.justice.ccr.notepad.result.cache.model.ResultType;
import uk.gov.justice.ccr.notepad.result.exception.CacheItemNotFoundException;
import uk.gov.justice.ccr.notepad.result.loader.ReadStoreResultLoader;
import uk.gov.justice.services.common.converter.LocalDates;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.test.utils.core.random.RandomGenerator;

import java.time.LocalDate;
import java.util.UUID;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newTreeSet;
import static java.lang.String.format;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.ccr.notepad.result.cache.ResultCache.RESULT_DEFINITIONS_GROUP_BY_KEYWORD_KEY;
import static uk.gov.justice.ccr.notepad.result.cache.ResultCache.RESULT_DEFINITION_KEY;
import static uk.gov.justice.ccr.notepad.result.cache.ResultCache.RESULT_DEFINITION_SYNONYM_KEY;
import static uk.gov.justice.ccr.notepad.result.cache.ResultCache.RESULT_PROMPTS_GROUP_BY_KEYWORD_KEY;
import static uk.gov.justice.ccr.notepad.result.cache.ResultCache.RESULT_PROMPT_KEY;
import static uk.gov.justice.ccr.notepad.result.cache.ResultCache.RESULT_PROMPT_SYNONYM_KEY;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUIDAndName;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.INTEGER;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.randomEnum;

import uk.gov.justice.ccr.notepad.result.cache.model.ResultDefinition;
import uk.gov.justice.ccr.notepad.result.cache.model.ResultPrompt;
import uk.gov.justice.ccr.notepad.result.cache.model.ResultPromptDynamicListNameAddress;
import uk.gov.justice.ccr.notepad.result.cache.model.ResultPromptSynonym;
import uk.gov.justice.ccr.notepad.result.cache.model.ResultType;
import uk.gov.justice.ccr.notepad.result.exception.CacheItemNotFoundException;
import uk.gov.justice.ccr.notepad.result.loader.ReadStoreResultLoader;
import uk.gov.justice.services.common.converter.LocalDates;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.test.utils.core.random.RandomGenerator;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.cache.LoadingCache;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ResultCacheTest {

    private static final UUID ID = randomUUID();
    private static final UUID PROMPT_ID = randomUUID();
    private static final UUID PROMPT_ID_2 = randomUUID();
    private static final String RESULT_DEFINITION_LABEL = STRING.next();
    private static final String LABEL = STRING.next();
    private static final ResultType TYPE = randomEnum(ResultType.class).next();
    private static final String RESULT_PROMPT_RULE = STRING.next();
    private static final String DURATION = STRING.next();
    private static final String WELSH_DURATION = STRING.next();
    private static final Set<String> KEYWORDS = newTreeSet(newArrayList(STRING.next(), STRING.next()));
    private static final Set<String> FIXED_LIST = newTreeSet(newArrayList(STRING.next(), STRING.next()));
    private static final String NAME = STRING.next();
    private static  Set<ResultPromptDynamicListNameAddress> NAMEADDRESS_LIST = new HashSet<>();
    private static final int SEQUENCE = INTEGER.next();
    private static final String REFERENCE = STRING.next();
    private static final int DURATION_SEQUENCE = INTEGER.next();
    private static final boolean HIDDEN = true;
    private static final String CACHE_KEY_FORMAT = "%s-%s";
    private static final String COMPONENT_LABEL = STRING.next();
    private static final String LIST_LABEL = STRING.next();
    private static final String ADDRESS_TYPE = STRING.next();
    private static final String PARTNAME1 = STRING.next();
    private static final String PARTNAME2 = STRING.next();
    private static final Boolean NAMEEMAIL = true;
    private final JsonEnvelope envelope = envelopeFrom(metadataWithRandomUUIDAndName(), createObjectBuilder().build());
    private final LocalDate hearingDate = LocalDate.parse("2018-06-01");

    @Mock
    private ReadStoreResultLoader resultLoader;
    @Mock
    private CacheFactory cacheFactory;
    @Mock
    private LoadingCache<String, Object> cache;
    @Mock
    private List<ResultDefinition> resultDefinitionList;
    @InjectMocks
    private ResultCache target;

    @Spy
    private final ConcurrentHashMap<String, Object> cacheMap = new ConcurrentHashMap<>();

    @Before
    public void setUp() {
        when(cacheFactory.build()).thenReturn(cache);
        when(cache.asMap()).thenReturn(cacheMap);
    }

    @Test
    public void shouldNotLoadTheCacheForExistingEntry() {
        when(resultLoader.loadResultDefinition(hearingDate)).thenReturn(resultDefinitionList);
        cacheMap.put("resultDefinitionKey-2018-06-01", resultDefinitionList);

        target.lazyLoad(envelope, hearingDate);

        verify(cacheMap).put("resultDefinitionKey-2018-06-01", resultDefinitionList);
        assertThat(cacheMap.size(), is(greaterThan(1)));
    }

    @Test
    public void shouldNotLoadCacheWithExistingKeysByDate() {
        when(resultLoader.loadResultDefinition(hearingDate)).thenReturn(resultDefinitionList);
        when(cacheMap.containsKey(anyString())).thenReturn(true);

        target.lazyLoad(envelope, hearingDate);

        String resultDefinitionKey = format(CACHE_KEY_FORMAT, RESULT_DEFINITION_KEY, LocalDates.to(hearingDate));
        verify(cacheMap, never()).put(resultDefinitionKey, resultDefinitionList);
        assertThat(cacheMap.size(), is(0));
    }

    @Test
    public void shouldLoadResultDefinitionsWithDataFromServiceForGivenDate() {
        final ConcurrentHashMap<String, Object> cacheValue = new ConcurrentHashMap<>();
        when(cache.asMap()).thenReturn(cacheValue);
        final List mockResultDefinitions = mock(List.class);

        when(resultLoader.loadResultDefinition(hearingDate)).thenReturn(mockResultDefinitions);

        target.lazyLoad(envelope, hearingDate);

        assertThat(cacheValue.size(), is(greaterThan(0)));
        assertThat(cacheValue, hasKey("resultDefinitionKey-2018-06-01"));
        assertThat(cacheValue.get("resultDefinitionKey-2018-06-01"), is(mockResultDefinitions));
    }

    @Test
    public void shouldReloadCacheSuccessfully() {
        target.reloadCache();
        assertThat(cacheMap.size(), is(greaterThan(0)));
        assertThat(cacheMap, hasKey(format(CACHE_KEY_FORMAT, RESULT_DEFINITION_KEY, LocalDates.to(LocalDate.now()))));
        assertThat(cacheMap, hasKey(format(CACHE_KEY_FORMAT, RESULT_DEFINITION_SYNONYM_KEY, LocalDates.to(LocalDate.now()))));
        assertThat(cacheMap, hasKey(format(CACHE_KEY_FORMAT, RESULT_DEFINITIONS_GROUP_BY_KEYWORD_KEY, LocalDates.to(LocalDate.now()))));
        assertThat(cacheMap, hasKey(format(CACHE_KEY_FORMAT, RESULT_PROMPT_KEY, LocalDates.to(LocalDate.now()))));
        assertThat(cacheMap, hasKey(format(CACHE_KEY_FORMAT, RESULT_PROMPT_SYNONYM_KEY, LocalDates.to(LocalDate.now()))));
        assertThat(cacheMap, hasKey(format(CACHE_KEY_FORMAT, RESULT_PROMPTS_GROUP_BY_KEYWORD_KEY, LocalDates.to(LocalDate.now()))));
    }

    @Test
    public void shouldReloadCacheFailWhenExceptionOccurs() {
        when(resultLoader.loadResultDefinition(any(LocalDate.class))).thenThrow(new RuntimeException());
        target.reloadCache();
        assertThat(cacheMap.size(), is(0));
        assertFalse(cacheMap.containsKey(format(CACHE_KEY_FORMAT, RESULT_DEFINITION_KEY, LocalDates.to(LocalDate.now()))));
    }

    @Test
    public void shouldLoadPromptsWithDataFromServiceForGivenDate() {
        final ConcurrentHashMap<String, Object> cacheValue = new ConcurrentHashMap<>();
        when(cache.asMap()).thenReturn(cacheValue);
        final List mockPrompts = mock(List.class);

        when(resultLoader.loadResultPrompt(hearingDate)).thenReturn(mockPrompts);

        target.lazyLoad(envelope, hearingDate);

        assertThat(cacheValue.size(), is(greaterThan(0)));
        assertThat(cacheValue, hasKey("resultPromptKey-2018-06-01"));
        assertThat(cacheValue.get("resultPromptKey-2018-06-01"), is(mockPrompts));
    }

    @Test
    public void shouldLoadPromptSynonymsWithDataFromServiceForGivenDate() {
        final ConcurrentHashMap<String, Object> cacheValue = new ConcurrentHashMap<>();
        when(cache.asMap()).thenReturn(cacheValue);
        final List mockPromptSynonyms = mock(List.class);

        when(resultLoader.loadResultPromptSynonym(hearingDate)).thenReturn(mockPromptSynonyms);

        target.lazyLoad(envelope, hearingDate);

        assertThat(cacheValue.size(), is(greaterThan(0)));
        assertThat(cacheValue, hasKey("resultPromptSynonymKey-2018-06-01"));
        assertThat(cacheValue.get("resultPromptSynonymKey-2018-06-01"), is(mockPromptSynonyms));
    }

    @Test
    public void shouldComputeResultDefinitionIndexByKeywordsForGivenDate() {
        final ConcurrentHashMap<String, Object> cacheValue = new ConcurrentHashMap<>();

        final ArrayList<ResultDefinition> resultDefinitions = new ArrayList<>();
        final String id1 = randomUUID().toString();
        final String label1 = STRING.next();
        final String shortCode1 = STRING.next();
        final HashSet<String> keywords1 = new HashSet<>();
        final String keyword1_1 = STRING.next();
        final String keyword1_2 = STRING.next();
        keywords1.add(keyword1_1);
        keywords1.add(keyword1_2);

        final String id2 = randomUUID().toString();
        final String label2 = STRING.next();
        final String shortCode2 = STRING.next();
        final HashSet<String> keywords2 = new HashSet<>();
        final String keyword2_1 = STRING.next();
        keywords2.add(keyword2_1);


        resultDefinitions.add(ResultDefinition.builder().withId(id1).withLabel(label1).withShortCode(shortCode1)
                .withLevel(STRING.next()).withKeywords(keywords1).withTerminatesOffenceProceedings(true)
                .withLifeDuration(true).withPublishedAsAPrompt(true).withExcludedFromResults(true)
                .withAlwaysPublished(true).withUrgent(true).withD20(true)
                .withPublishedForNows(true).withRollUpPrompts(true).build());

        resultDefinitions.add(ResultDefinition.builder().withId(id2).withLabel(label2).withShortCode(shortCode2)
                .withLevel(STRING.next()).withKeywords(keywords2).build());

        when(resultLoader.loadResultDefinition(hearingDate)).thenReturn(resultDefinitions);
        when(cache.asMap()).thenReturn(cacheValue);

        target.lazyLoad(envelope, hearingDate);

        assertThat(cacheValue.size(), is(greaterThan(0)));
        assertThat(cacheValue, hasKey("resultDefinitionsGroupByKeywordKey-2018-06-01"));
        final Map<String, List<Long>> resultDefinitionsIndexGroupByKeyword = target.getResultDefinitionsIndexGroupByKeyword(hearingDate);
        assertThat(resultDefinitionsIndexGroupByKeyword.get(keyword1_1.toLowerCase()).size(), equalTo(1));
        assertThat(resultDefinitionsIndexGroupByKeyword.get(keyword1_1.toLowerCase()).get(0), equalTo(0L));
        assertThat(resultDefinitionsIndexGroupByKeyword.get(keyword1_2.toLowerCase()).size(), equalTo(1));
        assertThat(resultDefinitionsIndexGroupByKeyword.get(keyword1_2.toLowerCase()).get(0), equalTo(0L));
        assertThat(resultDefinitionsIndexGroupByKeyword.get(keyword2_1.toLowerCase()).size(), equalTo(1));
        assertThat(resultDefinitionsIndexGroupByKeyword.get(keyword2_1.toLowerCase()).get(0), equalTo(1L));
    }

    @Test
    public void shouldLoadResultDefinitionSynonymsWithDataFromServiceForGivenDate() {
        final ConcurrentHashMap<String, Object> cacheValue = new ConcurrentHashMap<>();
        when(cache.asMap()).thenReturn(cacheValue);
        final List mockResultDefinitionSynonym = mock(List.class);

        when(resultLoader.loadResultDefinitionSynonym(hearingDate)).thenReturn(mockResultDefinitionSynonym);

        target.lazyLoad(envelope, hearingDate);

        assertThat(cacheValue.size(), is(greaterThan(0)));
        assertThat(cacheValue, hasKey("resultDefinitionSynonymKey-2018-06-01"));
        assertThat(cacheValue.get("resultDefinitionSynonymKey-2018-06-01"), is(mockResultDefinitionSynonym));
    }

    @Test
    public void shouldCacheThePromptSynonymIndexesForGivenDate() {
        final ConcurrentHashMap<String, Object> cacheValue = new ConcurrentHashMap<>();
        when(cache.asMap()).thenReturn(cacheValue);

        final List<ResultPrompt> resultPrompts = new ArrayList<>();
        final String id1 = randomUUID().toString();
        final UUID resultDefinitionId1 = randomUUID();
        final String resultDefinitionLabel1 = STRING.next();
        final String label1 = STRING.next();
        final String resultPromptRule1 = STRING.next();
        final String durationElement1 = STRING.next();
        final String welshDurationElement1 = STRING.next();
        final Integer promptOrder1 = INTEGER.next();
        final String reference1 = STRING.next();
        final Integer durationSequence1 = 0;
        final Boolean hidden1 = true;
        final String componentLabel = STRING.next();
        final String listLabel = STRING.next();
        final String addressType = STRING.next();
        final String partName1 = STRING.next();
        final String partName2 = STRING.next();
        final Boolean nameEmail1 = true;

        final HashSet<String> keywords1 = new HashSet<>();
        final String keyword_1_common = STRING.next();
        final String keyword_1_2 = STRING.next();
        final String keyword_1_3 = STRING.next();

        keywords1.add(keyword_1_common);
        keywords1.add(keyword_1_2);
        keywords1.add(keyword_1_3);

        final String id2 = randomUUID().toString();
        final UUID resultDefinitionId2 = randomUUID();
        final String resultDefinitionLabel2 = STRING.next();
        final String label2 = STRING.next();
        final String resultPromptRule2 = STRING.next();
        final String durationElement2 = STRING.next();
        final String welshDurationElement2 = STRING.next();
        final Integer promptOrder2 = INTEGER.next();
        final String reference2 = STRING.next();
        final Integer durationSequence2 = 0;
        final Boolean hidden2 = false;
        final Boolean nameEmail2 = false;

        final HashSet<String> keywords2 = new HashSet<>();
        final String keyword_2_2 = STRING.next();
        final String keyword_2_3 = STRING.next();

        keywords2.add(keyword_1_common);
        keywords2.add(keyword_2_2);
        keywords2.add(keyword_2_3);

        resultPrompts.add(new ResultPrompt(
                id1,
                resultDefinitionId1,
                resultDefinitionLabel1,
                label1,
                RandomGenerator.randomEnum(ResultType.class).next(),
                resultPromptRule1,
                durationElement1,
                keywords1,
                new HashSet<>(),
                new HashSet<>(),
                promptOrder1,
                reference1,
                durationSequence1,
                hidden1,
                componentLabel,
                listLabel,
                addressType,
                partName1,
                nameEmail1,
                welshDurationElement1
        ));

        resultPrompts.add(new ResultPrompt(
                id2,
                resultDefinitionId2,
                resultDefinitionLabel2,
                label2,
                RandomGenerator.randomEnum(ResultType.class).next(),
                resultPromptRule2,
                durationElement2,
                keywords2,
                new HashSet<>(),
                new HashSet<>(),
                promptOrder2,
                reference2,
                durationSequence2,
                hidden2,
                componentLabel,
                listLabel,
                addressType,
                partName2,
                nameEmail2,
                welshDurationElement2
        ));

        when(resultLoader.loadResultPrompt(hearingDate)).thenReturn(resultPrompts);

        target.lazyLoad(envelope, hearingDate);

        assertThat(cacheValue.size(), is(greaterThan(0)));
        assertThat(cacheValue, hasKey("resultPromptsGroupByKeywordKey-2018-06-01"));
        final Map<String, List<Integer>> cachedIndexesByKeywords = (Map<String, List<Integer>>) cacheValue.get("resultPromptsGroupByKeywordKey-2018-06-01");
        assertThat(cachedIndexesByKeywords.get(keyword_1_common.toLowerCase()), hasSize(2));
        assertThat(cachedIndexesByKeywords.get(keyword_1_common.toLowerCase()).get(0), equalTo(0L));
        assertThat(cachedIndexesByKeywords.get(keyword_1_common.toLowerCase()).get(1), equalTo(1L));
        assertThat(cachedIndexesByKeywords.get(keyword_1_2.toLowerCase()), hasSize(1));
        assertThat(cachedIndexesByKeywords.get(keyword_1_2.toLowerCase()).get(0), equalTo(0L));
        assertThat(cachedIndexesByKeywords.get(keyword_1_3.toLowerCase()), hasSize(1));
        assertThat(cachedIndexesByKeywords.get(keyword_1_3.toLowerCase()).get(0), equalTo(0L));
        assertThat(cachedIndexesByKeywords.get(keyword_2_2.toLowerCase()), hasSize(1));
        assertThat(cachedIndexesByKeywords.get(keyword_2_2.toLowerCase()).get(0), equalTo(1L));
        assertThat(cachedIndexesByKeywords.get(keyword_2_3.toLowerCase()), hasSize(1));
        assertThat(cachedIndexesByKeywords.get(keyword_2_3.toLowerCase()).get(0), equalTo(1L));
    }

    @Test
    public void shouldReturnThePromptSynonymIndexesForGivenDate() {
        final ConcurrentHashMap<String, Object> cacheValue = new ConcurrentHashMap<>();
        when(cache.asMap()).thenReturn(cacheValue);

        final String keyword_1_common = STRING.next();
        final String keyword_1_2 = STRING.next();
        final String keyword_1_3 = STRING.next();
        final String keyword_2_2 = STRING.next();
        final String keyword_2_3 = STRING.next();

        final Map<String, List<Long>> givenMap = new HashMap<>();
        givenMap.put(keyword_1_common, newArrayList(0L, 1L));
        givenMap.put(keyword_1_2, newArrayList(0L));
        givenMap.put(keyword_1_3, newArrayList(0L));
        givenMap.put(keyword_2_2, newArrayList(1L));
        givenMap.put(keyword_2_3, newArrayList(1L));

        cacheValue.put("resultPromptsGroupByKeywordKey-2018-06-01", givenMap);

        final Map<String, List<Long>> cachedIndexesByKeywords = target.getResultPromptsIndexGroupByKeyword(hearingDate);
        assertThat(cachedIndexesByKeywords.get(keyword_1_common), hasSize(2));
        assertThat(cachedIndexesByKeywords.get(keyword_1_common).get(0), equalTo(0L));
        assertThat(cachedIndexesByKeywords.get(keyword_1_common).get(1), equalTo(1L));
        assertThat(cachedIndexesByKeywords.get(keyword_1_2), hasSize(1));
        assertThat(cachedIndexesByKeywords.get(keyword_1_2).get(0), equalTo(0L));
        assertThat(cachedIndexesByKeywords.get(keyword_1_3), hasSize(1));
        assertThat(cachedIndexesByKeywords.get(keyword_1_3).get(0), equalTo(0L));
        assertThat(cachedIndexesByKeywords.get(keyword_2_2), hasSize(1));
        assertThat(cachedIndexesByKeywords.get(keyword_2_2).get(0), equalTo(1L));
        assertThat(cachedIndexesByKeywords.get(keyword_2_3), hasSize(1));
        assertThat(cachedIndexesByKeywords.get(keyword_2_3).get(0), equalTo(1L));
    }

    @Test
    public void shouldProvideTheCachedDefinitionsByDate() {
        final LocalDate hearingDate = LocalDate.parse("2017-05-08");
        final ConcurrentHashMap<String, Object> cacheValue = new ConcurrentHashMap<>();

        final ArrayList<ResultDefinition> resultDefinitions = new ArrayList<>();
        resultDefinitions.add(new ResultDefinition());
        resultDefinitions.add(new ResultDefinition());

        cacheValue.put("resultDefinitionKey-2017-05-08", resultDefinitions);
        when(cache.asMap()).thenReturn(cacheValue);

        final List<ResultDefinition> definitions = target.getResultDefinitions(hearingDate);

        assertThat(definitions, is(resultDefinitions));
    }

    @Test(expected = CacheItemNotFoundException.class)
    public void shouldThrowExceptionWhenCacheKeyNotFound() {
        when(resultLoader.loadResultDefinition(hearingDate)).thenReturn(resultDefinitionList);
        cacheMap.put("resultDefinitionKey-2018-06-01", resultDefinitionList);

        target.getResultDefinitions(LocalDate.now());
    }

    @Test
    public void shouldProvideTheCachedResultPromptsByDate() {
        final LocalDate hearingDate = LocalDate.parse("2017-05-08");
        final ConcurrentHashMap<String, Object> cacheValue = new ConcurrentHashMap<>();

        final ArrayList<ResultPrompt> resultPrompts = new ArrayList<>();
        resultPrompts.add(new ResultPrompt());
        resultPrompts.add(new ResultPrompt());

        cacheValue.put("resultPromptKey-2017-05-08", resultPrompts);
        when(cache.asMap()).thenReturn(cacheValue);

        final List<ResultPrompt> definitions = target.getResultPrompt(hearingDate);

        assertThat(definitions, is(resultPrompts));
    }

    @Test
    public void shouldProvideTheCachedResultPromptByResultDefinitionIdAndDate() {
        final LocalDate hearingDate = LocalDate.parse("2017-05-08");
        final ConcurrentHashMap<String, Object> cacheValue = new ConcurrentHashMap<>();

        final ArrayList<ResultPrompt> resultPrompts = new ArrayList<>();
        resultPrompts.add(new ResultPrompt(randomUUID().toString(), randomUUID(), null, null, null, STRING.next(), null,null, null, null, null, null, null, false, null, null,null, null, null, null));
        final UUID resultDefinitionIdToFind = randomUUID();
        final ResultPrompt expectedResultPrompt = new ResultPrompt(randomUUID().toString(), resultDefinitionIdToFind, null, null, null, STRING.next(), null, null, null, null, null, null, null, false, null, null, null, null,  null, null);
        resultPrompts.add(expectedResultPrompt);

        cacheValue.put("resultPromptKey-2017-05-08", resultPrompts);
        when(cache.asMap()).thenReturn(cacheValue);

        final List<ResultPrompt> resultPromptByResultDefinitionId = target.getResultPromptByResultDefinitionId(resultDefinitionIdToFind.toString(), hearingDate);

        assertThat(resultPromptByResultDefinitionId, hasSize(1));
        assertThat(resultPromptByResultDefinitionId.get(0), is(expectedResultPrompt));
    }

    @Test
    public void shouldRetrieveResultPromptSynonymsMergedWithPromptWordGroupsForGivenDate() {
        final ConcurrentHashMap<String, Object> cacheValue = new ConcurrentHashMap<>();
        when(cache.asMap()).thenReturn(cacheValue);

        final String SYNONYM = STRING.next();
        final String PROMPT_WORD = STRING.next();
        final String WORD = STRING.next();

        final HashSet<String> promptKeyWords = new HashSet<>();
        promptKeyWords.add(PROMPT_WORD);
        final ResultPrompt resultPrompt = new ResultPrompt(randomUUID().toString(), randomUUID(), null, null, null, STRING.next(),null,  promptKeyWords, null,null, null, null, null, false, null, null, null, null, null, null);
        final List<ResultPrompt> prompts = newArrayList(resultPrompt);

        final ResultPromptSynonym givenResultPromptSynonym = new ResultPromptSynonym();
        givenResultPromptSynonym.setSynonym(SYNONYM);
        givenResultPromptSynonym.setWord(WORD);
        final List<ResultPromptSynonym> promptSynonyms = newArrayList(givenResultPromptSynonym);

        when(cache.asMap()).thenReturn(cacheValue);
        cacheValue.put("resultPromptKey-2018-06-01", prompts);
        cacheValue.put("resultPromptSynonymKey-2018-06-01", promptSynonyms);

        final List<ResultPromptSynonym> resultPromptSynonym = target.getResultPromptSynonym(hearingDate);

        assertThat(resultPromptSynonym.size(), is(equalTo(2)));
        assertThat(resultPromptSynonym.get(0).getSynonym(), equalTo(PROMPT_WORD));
        assertThat(resultPromptSynonym.get(0).getWord(), equalTo(PROMPT_WORD));
        assertThat(resultPromptSynonym.get(1).getSynonym(), equalTo(SYNONYM));
        assertThat(resultPromptSynonym.get(1).getWord(), equalTo(WORD));
    }

    @Test
    public void shouldGetResultPromptByResultDefinitionIdIfPresent() {
        final LocalDate hearingDate = LocalDate.now();
        given(resultLoader.loadResultPrompt(any(LocalDate.class))).willReturn(prepareResultPrompts());
        final ConcurrentHashMap<String, Object> cacheValue = new ConcurrentHashMap<>();
        when(cache.asMap()).thenReturn(cacheValue);

        final JsonEnvelope envelope = envelopeFrom(metadataWithRandomUUIDAndName(), createObjectBuilder().build());
        target.lazyLoad(envelope, hearingDate);

        final List<ResultPrompt> prompts = target.getResultPromptByResultDefinitionId(ID.toString(), hearingDate);

        assertThat(prompts, is(notNullValue()));
        assertThat(prompts, hasSize(2));
        assertThat(prompts.get(0).getId(), is(PROMPT_ID.toString()));
        assertThat(prompts.get(1).getId(), is(PROMPT_ID_2.toString()));
    }

    private List<ResultPrompt> prepareResultPrompts() {
        NAMEADDRESS_LIST.add(ResultPromptDynamicListNameAddress.resultPromptDynamicListNameAddressBuilder()
                .withName(NAME)
                .withAddressLine1(STRING.next())
                .build());
        return newArrayList(
                new ResultPrompt(PROMPT_ID.toString(), ID, RESULT_DEFINITION_LABEL, LABEL, TYPE,
                        RESULT_PROMPT_RULE, DURATION, KEYWORDS, FIXED_LIST,NAMEADDRESS_LIST, SEQUENCE, REFERENCE, DURATION_SEQUENCE, HIDDEN, COMPONENT_LABEL,LIST_LABEL,ADDRESS_TYPE,PARTNAME1, NAMEEMAIL, WELSH_DURATION),
                new ResultPrompt(PROMPT_ID_2.toString(), ID, RESULT_DEFINITION_LABEL, LABEL, TYPE, RESULT_PROMPT_RULE, DURATION, KEYWORDS, FIXED_LIST,NAMEADDRESS_LIST, SEQUENCE, REFERENCE, DURATION_SEQUENCE, HIDDEN, COMPONENT_LABEL,LIST_LABEL,ADDRESS_TYPE,PARTNAME2, NAMEEMAIL, WELSH_DURATION)
        );
    }

    @Test
    public void shouldReturnCachedDefinitionById() {
        final LocalDate hearingDate = LocalDate.parse("2017-05-08");
        final ConcurrentHashMap<String, Object> cacheValue = new ConcurrentHashMap<>();
        final String resultDefinitionId = randomUUID().toString();

        final ArrayList<ResultDefinition> resultDefinitions = new ArrayList<>();
        ResultDefinition resultDefinition1 = new ResultDefinition();
        resultDefinition1.setId(resultDefinitionId);
        ResultDefinition resultDefinition2 = new ResultDefinition();
        resultDefinition2.setId(randomUUID().toString());
        resultDefinitions.addAll(Arrays.asList(resultDefinition1, resultDefinition2));

        cacheValue.put("resultDefinitionKey-2017-05-08", resultDefinitions);
        when(cache.asMap()).thenReturn(cacheValue);

        final ResultDefinition response = target.getResultDefinitionsById(resultDefinitionId, hearingDate);

        assertThat(response.getId(), is(resultDefinitionId));
    }

}