package uk.gov.justice.ccr.notepad.result.cache;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newTreeSet;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUIDAndName;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.BOOLEAN;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.INTEGER;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.randomEnum;

import uk.gov.justice.ccr.notepad.result.cache.model.ResultDefinition;
import uk.gov.justice.ccr.notepad.result.cache.model.ResultPrompt;
import uk.gov.justice.ccr.notepad.result.cache.model.ResultPromptSynonym;
import uk.gov.justice.ccr.notepad.result.cache.model.ResultType;
import uk.gov.justice.ccr.notepad.result.loader.ResultLoader;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.test.utils.core.random.RandomGenerator;

import java.time.LocalDate;
import java.util.ArrayList;
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
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ResultCacheTest {

    private static final UUID ID = randomUUID();

    private static final UUID PROMPT_ID = randomUUID();
    private static final UUID PROMPT_ID_2 = randomUUID();
    private static final String RESULT_DEFINITION_LABEL = STRING.next();
    private static final String LABEL = STRING.next();
    private static final ResultType TYPE = randomEnum(ResultType.class).next();
    private static final boolean MANDATORY = BOOLEAN.next();
    private static final String DURATION = STRING.next();
    private static final Set<String> KEYWORDS = newTreeSet(newArrayList(STRING.next(), STRING.next()));
    private static final Set<String> FIXED_LIST = newTreeSet(newArrayList(STRING.next(), STRING.next()));
    private static final int SEQUENCE = INTEGER.next();
    private static final String REFERENCE = STRING.next();

    @Mock
    private ResultLoader resultLoader;

    @Mock
    private CacheFactory cacheFactory;

    @Mock
    private LoadingCache<String, Object> cache;

    @InjectMocks
    private ResultCache underTest;

    private JsonEnvelope envelope = envelopeFrom(metadataWithRandomUUIDAndName(), createObjectBuilder().build());
    private LocalDate hearingDate = LocalDate.parse("2018-06-01");

//    @Test(expected = CacheLoader.InvalidCacheLoadException.class)
//    public void getResultLoaderWithKeyNotFound() throws Exception {
//        underTest.cache.get("UNKNOWN");
//    }


    @Before
    public void setUp() {
        when(cacheFactory.build()).thenReturn(cache);
    }

    @Test
    public void shouldNotLoadTheCacheWhenNotEmpty() throws Exception {
        final ConcurrentHashMap<String, Object> cacheValue = new ConcurrentHashMap<>();
        cacheValue.put(STRING.next(), new Object());
        when(cache.asMap()).thenReturn(cacheValue);

        underTest.lazyLoad(envelope, hearingDate);

        assertThat(cacheValue.size(), equalTo(1));
    }

    @Test
    public void shouldLoadResultDefinitionsWithDataFromServiceForGivenDate() throws Exception {
        final ConcurrentHashMap<String, Object> cacheValue = new ConcurrentHashMap<>();
        when(cache.asMap()).thenReturn(cacheValue);
        final List mockResultDefinitions = mock(List.class);

        when(resultLoader.loadResultDefinition(hearingDate)).thenReturn(mockResultDefinitions);

        underTest.lazyLoad(envelope, hearingDate);

        assertThat(cacheValue.size(), is(greaterThan(0)));
        assertThat(cacheValue, hasKey("resultDefinitionKey-2018-06-01"));
        assertThat(cacheValue.get("resultDefinitionKey-2018-06-01"), is(mockResultDefinitions));
    }

    @Test
    public void shouldLoadPromptsWithDataFromServiceForGivenDate() throws Exception {
        final ConcurrentHashMap<String, Object> cacheValue = new ConcurrentHashMap<>();
        when(cache.asMap()).thenReturn(cacheValue);
        final List mockPrompts = mock(List.class);

        when(resultLoader.loadResultPrompt(hearingDate)).thenReturn(mockPrompts);

        underTest.lazyLoad(envelope, hearingDate);

        assertThat(cacheValue.size(), is(greaterThan(0)));
        assertThat(cacheValue, hasKey("resultPromptKey-2018-06-01"));
        assertThat(cacheValue.get("resultPromptKey-2018-06-01"), is(mockPrompts));
    }

    @Test
    public void shouldLoadPromptSynonymsWithDataFromServiceForGivenDate() throws Exception {
        final ConcurrentHashMap<String, Object> cacheValue = new ConcurrentHashMap<>();
        when(cache.asMap()).thenReturn(cacheValue);
        final List mockPromptSynonyms = mock(List.class);

        when(resultLoader.loadResultPromptSynonym(hearingDate)).thenReturn(mockPromptSynonyms);

        underTest.lazyLoad(envelope, hearingDate);

        assertThat(cacheValue.size(), is(greaterThan(0)));
        assertThat(cacheValue, hasKey("resultPromptSynonymKey-2018-06-01"));
        assertThat(cacheValue.get("resultPromptSynonymKey-2018-06-01"), is(mockPromptSynonyms));
    }

    @Test
    public void shouldComputeResultDefinitionIndexByKeywordsForGivenDate() throws Exception {
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

        resultDefinitions.add(new ResultDefinition(id1, label1, shortCode1, STRING.next(), keywords1));
        resultDefinitions.add(new ResultDefinition(id2, label2, shortCode2, STRING.next(), keywords2));

        when(resultLoader.loadResultDefinition(hearingDate)).thenReturn(resultDefinitions);
        when(cache.asMap()).thenReturn(cacheValue);

        underTest.lazyLoad(envelope, hearingDate);

        assertThat(cacheValue.size(), is(greaterThan(0)));
        assertThat(cacheValue, hasKey("resultDefinitionsGroupByKeywordKey-2018-06-01"));
        final Map<String, List<Long>> resultDefinitionsIndexGroupByKeyword = underTest.getResultDefinitionsIndexGroupByKeyword(hearingDate);
        assertThat(resultDefinitionsIndexGroupByKeyword.get(keyword1_1.toLowerCase()).size(), equalTo(1));
        assertThat(resultDefinitionsIndexGroupByKeyword.get(keyword1_1.toLowerCase()).get(0), equalTo(0L));
        assertThat(resultDefinitionsIndexGroupByKeyword.get(keyword1_2.toLowerCase()).size(), equalTo(1));
        assertThat(resultDefinitionsIndexGroupByKeyword.get(keyword1_2.toLowerCase()).get(0), equalTo(0L));
        assertThat(resultDefinitionsIndexGroupByKeyword.get(keyword2_1.toLowerCase()).size(), equalTo(1));
        assertThat(resultDefinitionsIndexGroupByKeyword.get(keyword2_1.toLowerCase()).get(0), equalTo(1L));
    }

    @Test
    public void shouldLoadResultDefinitionSynonymsWithDataFromServiceForGivenDate() throws Exception {
        final ConcurrentHashMap<String, Object> cacheValue = new ConcurrentHashMap<>();
        when(cache.asMap()).thenReturn(cacheValue);
        final List mockResultDefinitionSynonym = mock(List.class);

        when(resultLoader.loadResultDefinitionSynonym(hearingDate)).thenReturn(mockResultDefinitionSynonym);

        underTest.lazyLoad(envelope, hearingDate);

        assertThat(cacheValue.size(), is(greaterThan(0)));
        assertThat(cacheValue, hasKey("resultDefinitionSynonymKey-2018-06-01"));
        assertThat(cacheValue.get("resultDefinitionSynonymKey-2018-06-01"), is(mockResultDefinitionSynonym));
    }

    @Test
    public void shouldCacheThePromptSynonymIndexesForGivenDate() throws Exception {
        final ConcurrentHashMap<String, Object> cacheValue = new ConcurrentHashMap<>();
        when(cache.asMap()).thenReturn(cacheValue);

        final List<ResultPrompt> resultPrompts = new ArrayList<>();
        final String id1 = UUID.randomUUID().toString();
        final UUID resultDefinitionId1 = UUID.randomUUID();
        final String resultDefinitionLabel1 = STRING.next();
        final String label1 = STRING.next();
        final Boolean mandatory1 = BOOLEAN.next();
        final String durationElement1 = STRING.next();
        final Integer promptOrder1 = INTEGER.next();
        final String reference1 = STRING.next();

        final HashSet<String> keywords1 = new HashSet<>();
        final String keyword_1_common = STRING.next();
        final String keyword_1_2 = STRING.next();
        final String keyword_1_3 = STRING.next();

        keywords1.add(keyword_1_common);
        keywords1.add(keyword_1_2);
        keywords1.add(keyword_1_3);

        final String id2 = UUID.randomUUID().toString();
        final UUID resultDefinitionId2 = UUID.randomUUID();
        final String resultDefinitionLabel2 = STRING.next();
        final String label2 = STRING.next();
        final Boolean mandatory2 = BOOLEAN.next();
        final String durationElement2 = STRING.next();
        final Integer promptOrder2 = INTEGER.next();
        final String reference2 = STRING.next();

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
                mandatory1,
                durationElement1,
                keywords1,
                new HashSet<>(),
                promptOrder1,
                reference1
        ));

        resultPrompts.add(new ResultPrompt(
                id2,
                resultDefinitionId2,
                resultDefinitionLabel2,
                label2,
                RandomGenerator.randomEnum(ResultType.class).next(),
                mandatory2,
                durationElement2,
                keywords2,
                new HashSet<>(),
                promptOrder2,
                reference2
        ));

        when(resultLoader.loadResultPrompt(hearingDate)).thenReturn(resultPrompts);

        underTest.lazyLoad(envelope, hearingDate);

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
    public void shouldReturnThePromptSynonymIndexesForGivenDate() throws Exception {
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

        final Map<String, List<Long>> cachedIndexesByKeywords = underTest.getResultPromptsIndexGroupByKeyword(hearingDate);
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
    public void shouldProvideTheCachedDefinitionsByDate() throws Exception {
        final LocalDate hearingDate = LocalDate.parse("2017-05-08");
        final ConcurrentHashMap<String, Object> cacheValue = new ConcurrentHashMap<>();

        final ArrayList<ResultDefinition> resultDefinitions = new ArrayList<>();
        resultDefinitions.add(new ResultDefinition());
        resultDefinitions.add(new ResultDefinition());

        cacheValue.put("resultDefinitionKey-2017-05-08", resultDefinitions);
        when(cache.asMap()).thenReturn(cacheValue);

        final List<ResultDefinition> definitions = underTest.getResultDefinitions(hearingDate);

        assertThat(definitions, is(resultDefinitions));
    }

    @Test
    public void shouldProvideTheCachedResultPromptsByDate() throws Exception {
        final LocalDate hearingDate = LocalDate.parse("2017-05-08");
        final ConcurrentHashMap<String, Object> cacheValue = new ConcurrentHashMap<>();

        final ArrayList<ResultPrompt> resultPrompts = new ArrayList<>();
        resultPrompts.add(new ResultPrompt());
        resultPrompts.add(new ResultPrompt());

        cacheValue.put("resultPromptKey-2017-05-08", resultPrompts);
        when(cache.asMap()).thenReturn(cacheValue);

        final List<ResultPrompt> definitions = underTest.getResultPrompt(hearingDate);

        assertThat(definitions, is(resultPrompts));
    }

    @Test
    public void shouldProvideTheCachedResultPromptByResultDefinitionIdAndDate() throws Exception {
        final LocalDate hearingDate = LocalDate.parse("2017-05-08");
        final ConcurrentHashMap<String, Object> cacheValue = new ConcurrentHashMap<>();

        final ArrayList<ResultPrompt> resultPrompts = new ArrayList<>();
        resultPrompts.add(new ResultPrompt(UUID.randomUUID().toString(), UUID.randomUUID(), null, null, null, BOOLEAN.next(), null, null, null, null, null));
        final UUID resultDefinitionIdToFind = UUID.randomUUID();
        final ResultPrompt expectedResultPrompt = new ResultPrompt(UUID.randomUUID().toString(), resultDefinitionIdToFind, null, null, null, BOOLEAN.next(), null, null, null, null, null);
        resultPrompts.add(expectedResultPrompt);

        cacheValue.put("resultPromptKey-2017-05-08", resultPrompts);
        when(cache.asMap()).thenReturn(cacheValue);

        final List<ResultPrompt> resultPromptByResultDefinitionId = underTest.getResultPromptByResultDefinitionId(resultDefinitionIdToFind.toString(), hearingDate);

        assertThat(resultPromptByResultDefinitionId, hasSize(1));
        assertThat(resultPromptByResultDefinitionId.get(0), is(expectedResultPrompt));
    }

    @Test
    public void shouldRetrieveResultPromptSynonymsMergedWithPromptWordGroupsForGivenDate() throws Exception {
        final ConcurrentHashMap<String, Object> cacheValue = new ConcurrentHashMap<>();
        when(cache.asMap()).thenReturn(cacheValue);

        final String SYNONYM = STRING.next();
        final String PROMPT_WORD = STRING.next();
        final String WORD = STRING.next();

        final HashSet<String> promptKeyWords = new HashSet<>();
        promptKeyWords.add(PROMPT_WORD);
        final ResultPrompt resultPrompt = new ResultPrompt(UUID.randomUUID().toString(), UUID.randomUUID(), null, null, null, BOOLEAN.next(), null, promptKeyWords, null, null, null);
        final List<ResultPrompt> prompts = newArrayList(resultPrompt);

        final ResultPromptSynonym givenResultPromptSynonym = new ResultPromptSynonym();
        givenResultPromptSynonym.setSynonym(SYNONYM);
        givenResultPromptSynonym.setWord(WORD);
        final List<ResultPromptSynonym> promptSynonyms = newArrayList(givenResultPromptSynonym);

        when(cache.asMap()).thenReturn(cacheValue);
        cacheValue.put("resultPromptKey-2018-06-01", prompts);
        cacheValue.put("resultPromptSynonymKey-2018-06-01", promptSynonyms);

        final List<ResultPromptSynonym> resultPromptSynonym = underTest.getResultPromptSynonym(hearingDate);

        assertThat(resultPromptSynonym.size(), is(equalTo(2)));
        assertThat(resultPromptSynonym.get(0).getSynonym(), equalTo(PROMPT_WORD));
        assertThat(resultPromptSynonym.get(0).getWord(), equalTo(PROMPT_WORD));
        assertThat(resultPromptSynonym.get(1).getSynonym(), equalTo(SYNONYM));
        assertThat(resultPromptSynonym.get(1).getWord(), equalTo(WORD));
    }

    @Test
    public void shouldGetResultPromptByResultDefinitionIdIfPresent() throws Exception {
        final LocalDate hearingDate = LocalDate.now();
        given(resultLoader.loadResultPrompt(any(LocalDate.class))).willReturn(prepareResultPrompts());
        final ConcurrentHashMap<String, Object> cacheValue = new ConcurrentHashMap<>();
        when(cache.asMap()).thenReturn(cacheValue);

        final JsonEnvelope envelope = envelopeFrom(metadataWithRandomUUIDAndName(), createObjectBuilder().build());
        underTest.lazyLoad(envelope, hearingDate);

        final List<ResultPrompt> prompts = underTest.getResultPromptByResultDefinitionId(ID.toString(), hearingDate);

        assertThat(prompts, is(notNullValue()));
        assertThat(prompts, hasSize(2));
        assertThat(prompts.get(0).getId(), is(PROMPT_ID.toString()));
        assertThat(prompts.get(1).getId(), is(PROMPT_ID_2.toString()));
    }

    private List<ResultPrompt> prepareResultPrompts() {
        return newArrayList(
                new ResultPrompt(PROMPT_ID.toString(), ID, RESULT_DEFINITION_LABEL, LABEL, TYPE,
                        MANDATORY, DURATION, KEYWORDS, FIXED_LIST, SEQUENCE, REFERENCE),
                new ResultPrompt(PROMPT_ID_2.toString(), ID, RESULT_DEFINITION_LABEL, LABEL, TYPE,
                        MANDATORY, DURATION, KEYWORDS, FIXED_LIST, SEQUENCE, REFERENCE)
        );
    }


}