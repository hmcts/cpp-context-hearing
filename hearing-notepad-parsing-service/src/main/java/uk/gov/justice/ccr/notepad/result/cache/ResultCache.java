package uk.gov.justice.ccr.notepad.result.cache;

import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.justice.ccr.notepad.result.cache.model.ResultDefinition;
import uk.gov.justice.ccr.notepad.result.cache.model.ResultDefinitionSynonym;
import uk.gov.justice.ccr.notepad.result.cache.model.ResultPrompt;
import uk.gov.justice.ccr.notepad.result.cache.model.ResultPromptSynonym;
import uk.gov.justice.ccr.notepad.result.exception.CacheItemNotFoundException;
import uk.gov.justice.ccr.notepad.result.loader.ReadStoreResultLoader;
import uk.gov.justice.ccr.notepad.result.loader.ResultLoader;
import uk.gov.justice.services.common.converter.LocalDates;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.annotation.PostConstruct;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.AccessTimeout;
import javax.inject.Inject;
import javax.inject.Named;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;
import static java.lang.String.format;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;

@Startup
@Singleton
@AccessTimeout(value = 60000)
public class ResultCache {

    public static final String RESULT_DEFINITION_KEY = "resultDefinitionKey";
    public static final String RESULT_DEFINITIONS_GROUP_BY_KEYWORD_KEY = "resultDefinitionsGroupByKeywordKey";
    public static final String RESULT_PROMPTS_GROUP_BY_KEYWORD_KEY = "resultPromptsGroupByKeywordKey";
    public static final String RESULT_DEFINITION_SYNONYM_KEY = "resultDefinitionSynonymKey";
    public static final String RESULT_PROMPT_KEY = "resultPromptKey";
    public static final String RESULT_PROMPT_SYNONYM_KEY = "resultPromptSynonymKey";

    private static final Logger LOGGER = LoggerFactory.getLogger(ResultCache.class);

    @Inject
    @Named("readStoreResultLoader")
    private ResultLoader resultLoader;

    @Inject
    @Named("ResultCacheFactory")
    private CacheFactory cacheFactory;

    private LoadingCache<String, Object> cache;

    @PostConstruct
    public void setUp() {
        cache = cacheFactory.build();
    }

    @Lock(LockType.WRITE)
    @AccessTimeout(value = 120000)
    public void lazyLoad(final JsonEnvelope envelope, final LocalDate orderedDate) {
        if (!isCacheLoadedByDate(orderedDate)) {
            synchronized (this) {
                if (!isCacheLoadedByDate(orderedDate)) {
                    if (resultLoader instanceof ReadStoreResultLoader) {
                        ((ReadStoreResultLoader) resultLoader).setJsonEnvelope(envelope);
                    }
                    loadCache(orderedDate);
                }
            }
        }
    }

    @Lock(LockType.READ)
    public ResultDefinition getResultDefinitionsById(final String resultDefinitionId, final LocalDate orderedDate) {
        final List<ResultDefinition> resultDefinitionList = (List<ResultDefinition>) getCacheEntry(orderedDate, RESULT_DEFINITION_KEY);

        return resultDefinitionList.stream()
                .filter(resultDefinition -> resultDefinition.getId().equals(resultDefinitionId))
                .findFirst()
                .orElse(null);
    }

    @Lock(LockType.READ)
    public List<ResultDefinition> getResultDefinitions(final LocalDate orderedDate) {
        return (List<ResultDefinition>) getCacheEntry(orderedDate, RESULT_DEFINITION_KEY);
    }

    @Lock(LockType.READ)
    public Map<String, List<Long>> getResultDefinitionsIndexGroupByKeyword(final LocalDate orderedDate) {
        return (Map<String, List<Long>>) getCacheEntry(orderedDate, RESULT_DEFINITIONS_GROUP_BY_KEYWORD_KEY);
    }

    @Lock(LockType.READ)
    public Map<String, List<Long>> getResultPromptsIndexGroupByKeyword(final LocalDate orderedDate) {
        return (Map<String, List<Long>>) getCacheEntry(orderedDate, RESULT_PROMPTS_GROUP_BY_KEYWORD_KEY);
    }

    @Lock(LockType.READ)
    public List<ResultDefinitionSynonym> getResultDefinitionSynonym(final LocalDate orderedDate) {
        final Set<String> allKeyWords = newHashSet();
        final List<ResultDefinition> resultDefinitions = (List<ResultDefinition>) getCacheEntry(orderedDate, RESULT_DEFINITION_KEY);

        resultDefinitions.stream().map(ResultDefinition::getKeywords).filter(v -> !v.isEmpty()).forEach(allKeyWords::addAll);
        final List<ResultDefinitionSynonym> resultDefinitionKeyWordsSynonyms =
                allKeyWords.stream().map(s -> {
                    ResultDefinitionSynonym resultDefinitionSynonym = new ResultDefinitionSynonym();
                    resultDefinitionSynonym.setSynonym(s);
                    resultDefinitionSynonym.setWord(s);
                    return resultDefinitionSynonym;
                }).collect(toList());

        final List<ResultDefinitionSynonym> resultDefinitionSynonyms = (List<ResultDefinitionSynonym>) getCacheEntry(orderedDate, RESULT_DEFINITION_SYNONYM_KEY);
        resultDefinitionKeyWordsSynonyms.addAll(resultDefinitionSynonyms);
        return resultDefinitionKeyWordsSynonyms;
    }

    @Lock(LockType.READ)
    public List<ResultPrompt> getResultPrompt(final LocalDate orderedDate) {
        return (List<ResultPrompt>) getCacheEntry(orderedDate, RESULT_PROMPT_KEY);
    }

    @Lock(LockType.READ)
    public List<ResultPrompt> getResultPromptByResultDefinitionId(final String resultDefinitionId, final LocalDate orderedDate) {
        final List<ResultPrompt> resultPromptList = (List<ResultPrompt>) getCacheEntry(orderedDate, RESULT_PROMPT_KEY);

        return resultPromptList.stream()
                .filter(resultPrompt -> resultPrompt.getResultDefinitionId().toString().equals(resultDefinitionId))
                .collect(toList());
    }

    @Lock(LockType.READ)
    public List<ResultPromptSynonym> getResultPromptSynonym(final LocalDate orderedDate) {
        final Set<String> allKeyWords = newHashSet();
        final List<ResultPrompt> resultPromptList = (List<ResultPrompt>) getCacheEntry(orderedDate, RESULT_PROMPT_KEY);
        final List<ResultPromptSynonym> resultPromptSynonymList = (List<ResultPromptSynonym>) getCacheEntry(orderedDate, RESULT_PROMPT_SYNONYM_KEY);

        resultPromptList.stream().map(ResultPrompt::getKeywords).filter(v -> !v.isEmpty()).forEach(allKeyWords::addAll);
        final List<ResultPromptSynonym> resultPromptSynonyms =
                allKeyWords.stream().map(s -> {
                    ResultPromptSynonym resultPromptSynonym = new ResultPromptSynonym();
                    resultPromptSynonym.setWord(s);
                    resultPromptSynonym.setSynonym(s);
                    return resultPromptSynonym;
                }).collect(toList());
        resultPromptSynonyms.addAll(resultPromptSynonymList);
        return resultPromptSynonyms;
    }

    @SuppressWarnings({"squid:S2221"})
    public void reloadCache() {
        final Instant first = Instant.now();
        LOGGER.info("Reloading cache started at: {}", first);
        try {
            loadCache(LocalDate.now());
            final Instant second = Instant.now();
            LOGGER.info("Reloading cache completed in {} seconds", Duration.between(first, second).getSeconds());
        } catch (Exception ex) {
            final Instant second = Instant.now();
            LOGGER.error(format("Reloading cache failed in %s seconds", Duration.between(first, second).getSeconds()), ex);
        }
    }

    private void loadCache(final LocalDate orderedDate) {
        if (!cacheContains(orderedDate, RESULT_DEFINITION_KEY)) {
            putEntry(orderedDate, RESULT_DEFINITION_KEY, resultLoader.loadResultDefinition(orderedDate));
        }
        if (!cacheContains(orderedDate, RESULT_DEFINITIONS_GROUP_BY_KEYWORD_KEY)) {
            putEntry(orderedDate, RESULT_DEFINITIONS_GROUP_BY_KEYWORD_KEY, getResultDefinitionsIndexByKeywords(orderedDate));
        }
        if (!cacheContains(orderedDate, RESULT_DEFINITION_SYNONYM_KEY)) {
            putEntry(orderedDate, RESULT_DEFINITION_SYNONYM_KEY, resultLoader.loadResultDefinitionSynonym(orderedDate));
        }
        if (!cacheContains(orderedDate, RESULT_PROMPT_KEY)) {
            putEntry(orderedDate, RESULT_PROMPT_KEY, resultLoader.loadResultPrompt(orderedDate));
        }
        if (!cacheContains(orderedDate, RESULT_PROMPT_SYNONYM_KEY)) {
            putEntry(orderedDate, RESULT_PROMPT_SYNONYM_KEY, resultLoader.loadResultPromptSynonym(orderedDate));
        }
        if (!cacheContains(orderedDate, RESULT_PROMPTS_GROUP_BY_KEYWORD_KEY)) {
            putEntry(orderedDate, RESULT_PROMPTS_GROUP_BY_KEYWORD_KEY, getPromptsIndexByKeyword(orderedDate));
        }
    }

    private Object getCacheEntry(final LocalDate orderedDate, final String key) {
        final ConcurrentMap<String, Object> cacheMap = cache.asMap();
        final String keyWithOrderedDate = getFormattedKey(key, orderedDate);

        if (nonNull(cacheMap) && cacheMap.containsKey(keyWithOrderedDate)) {
            return cacheMap.get(keyWithOrderedDate);
        } else {
            throw new CacheItemNotFoundException(format("No item found in cache with key: %s", keyWithOrderedDate));
        }
    }

    private void putEntry(final LocalDate orderedDate, final String key, final Object value) {
        final String keyWithOrderedDate = getFormattedKey(key, orderedDate);
        LOGGER.debug("Add to cache key {} value {}", keyWithOrderedDate, value);
        cache.asMap().put(keyWithOrderedDate, value);
    }

    private String getFormattedKey(String key, LocalDate orderedDate) {
        return format("%s-%s", key, LocalDates.to(orderedDate));
    }

    private boolean cacheContains(LocalDate orderedDate, String key) {
        final String keyWithOrderedDate = getFormattedKey(key, orderedDate);
        return cache.asMap().containsKey(keyWithOrderedDate);
    }

    private boolean isCacheLoadedByDate(LocalDate orderedDate) {
        final List<String> keyMap = Arrays.asList(RESULT_DEFINITION_KEY,
                RESULT_PROMPT_KEY,
                RESULT_DEFINITION_SYNONYM_KEY,
                RESULT_PROMPT_SYNONYM_KEY,
                RESULT_DEFINITIONS_GROUP_BY_KEYWORD_KEY,
                RESULT_PROMPTS_GROUP_BY_KEYWORD_KEY);

        return keyMap.stream().allMatch(key -> cacheContains(orderedDate, key));
    }

    private Map<String, List<Long>> getPromptsIndexByKeyword(final LocalDate orderedDate) {
        final Map<String, List<Long>> resultPromptsIndexByKeyWord = newHashMap();
        final AtomicLong indexPromptIncrementer = new AtomicLong();
        final List<ResultPrompt> resultPromptList = (List<ResultPrompt>) getCacheEntry(orderedDate, RESULT_PROMPT_KEY);

        resultPromptList.forEach((ResultPrompt resultPrompt) -> {
            final long index = indexPromptIncrementer.getAndIncrement();
            resultPrompt.getKeywords().stream().filter(word -> !word.isEmpty()).forEach(word -> {
                resultPromptsIndexByKeyWord.putIfAbsent(word.toLowerCase(), newArrayList());
                resultPromptsIndexByKeyWord.computeIfPresent(word.toLowerCase(), (inputStr, resultPrompts) -> {
                    resultPrompts.add(index);
                    return resultPrompts;
                });
            });
        });
        return resultPromptsIndexByKeyWord;
    }

    private Map<String, List<Long>> getResultDefinitionsIndexByKeywords(final LocalDate orderedDate) {
        final Map<String, List<Long>> resultDefinitionsIndexByKeyWord = newHashMap();
        final AtomicLong indexIncrementer = new AtomicLong();
        final List<ResultDefinition> resultDefinitionList = (List<ResultDefinition>) getCacheEntry(orderedDate, RESULT_DEFINITION_KEY);

        resultDefinitionList.forEach((ResultDefinition resultDefinition) -> {
            final long index = indexIncrementer.getAndIncrement();
            resultDefinition.getKeywords().forEach(word -> {
                resultDefinitionsIndexByKeyWord.putIfAbsent(word.toLowerCase(), newArrayList());
                resultDefinitionsIndexByKeyWord.computeIfPresent(word.toLowerCase(), (inputStr, resultDefinitions) -> {
                    resultDefinitions.add(index);
                    return resultDefinitions;
                });
            });
        });
        return resultDefinitionsIndexByKeyWord;
    }
}
