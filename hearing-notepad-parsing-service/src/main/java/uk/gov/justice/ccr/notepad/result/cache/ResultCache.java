package uk.gov.justice.ccr.notepad.result.cache;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;
import static java.lang.String.format;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;

import uk.gov.justice.ccr.notepad.result.cache.model.ResultDefinition;
import uk.gov.justice.ccr.notepad.result.cache.model.ResultDefinitionSynonym;
import uk.gov.justice.ccr.notepad.result.cache.model.ResultPrompt;
import uk.gov.justice.ccr.notepad.result.cache.model.ResultPromptSynonym;
import uk.gov.justice.ccr.notepad.result.exception.CacheItemNotFoundException;
import uk.gov.justice.ccr.notepad.result.loader.ReadStoreResultLoader;
import uk.gov.justice.ccr.notepad.result.loader.ResultLoader;
import uk.gov.justice.services.common.converter.LocalDates;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.PostConstruct;
import javax.ejb.Lock;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ejb.LockType;

import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Startup
@Singleton
@Lock(LockType.READ)
@SuppressWarnings("squid:S134")
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

    private Map<LocalDate, ReentrantLock> lockByDate = new ConcurrentHashMap<>();

    @PostConstruct
    public void setUp() {
        cache = cacheFactory.build();
    }


    /**
     * This method hydrate result cache.
     * It load the cache based on orderedDate.
     * If multiple threads with same orderedDate
     * try to access loadCache(orderedDate) method then
     * only one of these would be able to actually enter the
     * ReEntrant lock block which is based on orderDate
     * Thread with different orderedDate would execute parallelly.
     * It lock the thread
     * @param envelope JsonEnvelope of event
     * @param orderedDate date for which cache is loaded
     */
    public void lazyLoad(final JsonEnvelope envelope, final LocalDate orderedDate) {
        if (!isCacheLoadedByDate(orderedDate)) {
            final Instant first = Instant.now();
            LOGGER.info("result cache attempting to lock at: {} for order date: {}", first, orderedDate);
            lockByDate.putIfAbsent(orderedDate, new ReentrantLock());
            lockByDate.get(orderedDate).lock();
            LOGGER.info("result cache locked at: {} for order date: {}", first, orderedDate);
            try {
                if (!isCacheLoadedByDate(orderedDate)) {
                    if (resultLoader instanceof ReadStoreResultLoader) {
                        ((ReadStoreResultLoader) resultLoader).setJsonEnvelope(envelope);
                    }
                    loadCache(orderedDate);
                }
            } finally {
                lockByDate.get(orderedDate).unlock();
                final Instant second = Instant.now();
                LOGGER.info("result cache lock released in {} seconds for order date: {}", Duration.between(first, second).getSeconds(), orderedDate);
            }
            LOGGER.info("Finished loading cache for {}", orderedDate);
        }
    }


    public ResultDefinition getResultDefinitionsById(final String resultDefinitionId, final LocalDate orderedDate) {
        final List<ResultDefinition> resultDefinitionList = (List<ResultDefinition>) getCacheEntry(orderedDate, RESULT_DEFINITION_KEY);

        return resultDefinitionList.stream()
                .filter(resultDefinition -> resultDefinition.getId().equals(resultDefinitionId))
                .findFirst()
                .orElse(null);
    }


    public List<ResultDefinition> getResultDefinitions(final LocalDate orderedDate) {
        return (List<ResultDefinition>) getCacheEntry(orderedDate, RESULT_DEFINITION_KEY);
    }


    public Map<String, List<Long>> getResultDefinitionsIndexGroupByKeyword(final LocalDate orderedDate) {
        return (Map<String, List<Long>>) getCacheEntry(orderedDate, RESULT_DEFINITIONS_GROUP_BY_KEYWORD_KEY);
    }


    public Map<String, List<Long>> getResultPromptsIndexGroupByKeyword(final LocalDate orderedDate) {
        return (Map<String, List<Long>>) getCacheEntry(orderedDate, RESULT_PROMPTS_GROUP_BY_KEYWORD_KEY);
    }


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


    public List<ResultPrompt> getResultPrompt(final LocalDate orderedDate) {
        return (List<ResultPrompt>) getCacheEntry(orderedDate, RESULT_PROMPT_KEY);
    }


    public List<ResultPrompt> getResultPromptByResultDefinitionId(final String resultDefinitionId, final LocalDate orderedDate) {
        final List<ResultPrompt> resultPromptList = (List<ResultPrompt>) getCacheEntry(orderedDate, RESULT_PROMPT_KEY);

        return resultPromptList.stream()
                .filter(resultPrompt -> resultPrompt.getResultDefinitionId().toString().equals(resultDefinitionId))
                .collect(toList());
    }


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

    /**
     * This method hydrate result cache.
     * It load the cache based on today's date.
     * If multiple threads with same date
     * try to access loadCache(date) method then
     * only one of these would be able to actually enter the
     * ReEntrant lock block which is based on date
     * Thread with different orderedDate would execute parallelly.
     * It lock the thread
     */
    @SuppressWarnings({"squid:S2221"})
    public void reloadCache() {
        final Instant first = Instant.now();
        LOGGER.info("Reloading cache started at: {}", first);
        try {
            final LocalDate today = LocalDate.now();
            lockByDate.putIfAbsent(today,new ReentrantLock());
            lockByDate.get(today).lock();
            try {
                loadCache(today);
            } finally {
                lockByDate.get(today).unlock();
            }
            final Instant second = Instant.now();
            LOGGER.info("Reloading cache completed in {} seconds", Duration.between(first, second).getSeconds());
        } catch (Exception ex) {
            final Instant second = Instant.now();
            LOGGER.error(format("Reloading cache failed in %s seconds", Duration.between(first, second).getSeconds()), ex);
        }
    }

    private void loadCache(final LocalDate orderedDate) {
        final Instant first = Instant.now();
        LOGGER.info("loading cache started at: {} for order date: {}", first, orderedDate);
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
        final Instant second = Instant.now();
        LOGGER.info("loading cache completed in {} seconds  for order date: {}", Duration.between(first, second).getSeconds(), orderedDate);
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
