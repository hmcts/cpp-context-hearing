package uk.gov.justice.ccr.notepad.result.cache;


import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.justice.ccr.notepad.result.cache.model.*;
import uk.gov.justice.ccr.notepad.result.loader.ReadStoreResultLoader;
import uk.gov.justice.ccr.notepad.result.loader.ResultLoader;
import uk.gov.justice.services.common.converter.LocalDates;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.annotation.PostConstruct;
import javax.ejb.AccessTimeout;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import javax.inject.Named;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

@Startup
@Singleton
@AccessTimeout(value = 60000)
public class ResultCache {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResultCache.class);

    private static final String RESULT_DEFINITION_KEY = "resultDefinitionKey";
    private static final String RESULT_DEFINITIONS_GROUP_BY_KEYWORD_KEY = "resultDefinitionsGroupByKeywordKey";
    private static final String RESULT_PROMPTS_GROUP_BY_KEYWORD_KEY = "resultPromptsGroupByKeywordKey";
    private static final String RESULT_DEFINITION_SYNONYM_KEY = "resultDefinitionSynonymKey";
    private static final String RESULT_PROMPT_KEY = "resultPromptKey";
    private static final String RESULT_PROMPT_SYNONYM_KEY = "resultPromptSynonymKey";

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
        if (!cache.asMap().containsKey(getKey(orderedDate, RESULT_DEFINITION_KEY))) {
            synchronized (this) {
                //Double protection to stop multiple cache loads
                if (!cache.asMap().containsKey(getKey(orderedDate, RESULT_DEFINITION_KEY))) {
                    if (resultLoader instanceof ReadStoreResultLoader) {
                        ((ReadStoreResultLoader) resultLoader).setJsonEnvelope(envelope);
                    }
                    loadResultCache(orderedDate);
                }
            }
        }
    }

    private void loadResultCache(final LocalDate orderedDate) {

        addValueToCache(getKey(orderedDate, RESULT_DEFINITION_KEY), resultLoader.loadResultDefinition(orderedDate));

        addValueToCache(getKey(orderedDate, RESULT_DEFINITIONS_GROUP_BY_KEYWORD_KEY), getResultDefinitionsIndexByKeywords(orderedDate));

        addValueToCache(getKey(orderedDate, RESULT_DEFINITION_SYNONYM_KEY), resultLoader.loadResultDefinitionSynonym(orderedDate));

        addValueToCache(getKey(orderedDate, RESULT_PROMPT_KEY), resultLoader.loadResultPrompt(orderedDate));

        addValueToCache(getKey(orderedDate, RESULT_PROMPT_SYNONYM_KEY), resultLoader.loadResultPromptSynonym(orderedDate));

        addValueToCache(getKey(orderedDate, RESULT_PROMPTS_GROUP_BY_KEYWORD_KEY), getPromptsIndexByKeyword(orderedDate));
    }

    private String getKey(final LocalDate orderedDate, final String key) {
        return format("%s-%s", key, LocalDates.to(orderedDate));
    }

    private void addValueToCache(final String key, final Object value) {
        LOGGER.info("Add to cache key {} value {}", key, value);
        cache.asMap().put(key, value);
    }

    private Map<String, List<Long>> getPromptsIndexByKeyword(final LocalDate orderedDate) {
        final Map<String, List<Long>> resultPromptsIndexByKeyWord = newHashMap();
        final AtomicLong indexPromptIncrementer = new AtomicLong();
        getCachedResultPrompt(orderedDate)
                .forEach((ResultPrompt resultPrompt) -> {
                    final long index = indexPromptIncrementer.getAndIncrement();
                    resultPrompt.getKeywords().stream().filter(v -> !v.isEmpty()).forEach(word -> {
                        resultPromptsIndexByKeyWord.putIfAbsent(word.toLowerCase(), newArrayList());
                        resultPromptsIndexByKeyWord.computeIfPresent(word.toLowerCase(), (s, l) -> {
                            l.add(index);
                            return l;
                        });

                    });
                });
        return resultPromptsIndexByKeyWord;
    }

    private Map<String, List<Long>> getResultDefinitionsIndexByKeywords(final LocalDate orderedDate) {
        final Map<String, List<Long>> resultDefinitionsIndexByKeyWord = newHashMap();
        final AtomicLong indexIncrementer = new AtomicLong();
        getCachedResultDefinitions(orderedDate)
                .forEach((ResultDefinition resultDefinition) -> {
                    final long index = indexIncrementer.getAndIncrement();
                    resultDefinition.getKeywords().forEach(word -> {
                        resultDefinitionsIndexByKeyWord.putIfAbsent(word.toLowerCase(), newArrayList());
                        resultDefinitionsIndexByKeyWord.computeIfPresent(word.toLowerCase(), (s, resultDefinitions) -> {
                            resultDefinitions.add(index);
                            return resultDefinitions;
                        });

                    });
                });
        return resultDefinitionsIndexByKeyWord;
    }

    @Lock(LockType.READ)
    public ResultDefinition getResultDefinitionsById(final String resultDefinitionId, final LocalDate orderedDate) {
        return getCachedResultDefinitions(orderedDate).stream()
                .filter(resultDefinition -> resultDefinition.getId().equals(resultDefinitionId))
                .findFirst()
                .orElse(null);
    }

    @Lock(LockType.READ)
    public List<ResultDefinition> getResultDefinitions(final LocalDate orderedDate) {
        return getCachedResultDefinitions(orderedDate);
    }

    @Lock(LockType.READ)
    public Map<String, List<Long>> getResultDefinitionsIndexGroupByKeyword(final LocalDate orderedDate) {
        return getCachedResultDefinitionsGroupByKeyword(orderedDate);
    }

    @Lock(LockType.READ)
    public Map<String, List<Long>> getResultPromptsIndexGroupByKeyword(final LocalDate orderedDate) {
        return getCachedResultPromptsGroupByKeyword(orderedDate);
    }

    @Lock(LockType.READ)
    public List<ResultDefinitionSynonym> getResultDefinitionSynonym(final LocalDate orderedDate) {
        final Set<String> allKeyWords = newHashSet();
        getResultDefinitions(orderedDate).stream().map(ResultDefinition::getKeywords).filter(v -> !v.isEmpty()).forEach(allKeyWords::addAll);
        final List<ResultDefinitionSynonym> resultDefinitionKeyWordsSynonyms =
                allKeyWords.stream().map(s -> {
                    ResultDefinitionSynonym resultDefinitionSynonym = new ResultDefinitionSynonym();
                    resultDefinitionSynonym.setSynonym(s);
                    resultDefinitionSynonym.setWord(s);
                    return resultDefinitionSynonym;
                }).collect(toList());
        //Adding all keywords as default entry with itself
        resultDefinitionKeyWordsSynonyms.addAll(getCachedResultDefinitionSynonym(orderedDate));
        return resultDefinitionKeyWordsSynonyms;
    }

    @Lock(LockType.READ)
    public List<ResultPrompt> getResultPrompt(final LocalDate orderedDate) {
        return getCachedResultPrompt(orderedDate);
    }

    @Lock(LockType.READ)
    public List<ResultPrompt> getResultPromptByResultDefinitionId(final String resultDefinitionId, final LocalDate orderedDate) {
        return getCachedResultPrompt(orderedDate)
                .stream()
                .filter(resultPrompt -> resultPrompt.getResultDefinitionId().toString().equals(resultDefinitionId))
                .collect(toList());
    }

    @Lock(LockType.READ)
    public List<ResultPromptSynonym> getResultPromptSynonym(final LocalDate orderedDate) {
        final Set<String> allKeyWords = newHashSet();
        getCachedResultPrompt(orderedDate).stream().map(ResultPrompt::getKeywords).filter(v -> !v.isEmpty()).forEach(allKeyWords::addAll);
        final List<ResultPromptSynonym> resultPromptSynonyms =
                allKeyWords.stream().map(s -> {
                    ResultPromptSynonym resultPromptSynonym = new ResultPromptSynonym();
                    resultPromptSynonym.setWord(s);
                    resultPromptSynonym.setSynonym(s);
                    return resultPromptSynonym;
                }).collect(toList());
        //Adding all keywords as default entry with itself
        resultPromptSynonyms.addAll(getCachedResultPromptSynonym(orderedDate));
        return resultPromptSynonyms;
    }

    private List<ResultDefinition> getCachedResultDefinitions(final LocalDate orderedDate) {
        return (List<ResultDefinition>) cache.asMap().get(getKey(orderedDate, RESULT_DEFINITION_KEY));
    }

    private Map<String, List<Long>> getCachedResultDefinitionsGroupByKeyword(final LocalDate orderedDate) {
        return (Map<String, List<Long>>) cache.asMap().get(getKey(orderedDate, RESULT_DEFINITIONS_GROUP_BY_KEYWORD_KEY));
    }

    private Map<String, List<Long>> getCachedResultPromptsGroupByKeyword(final LocalDate orderedDate) {
        return (Map<String, List<Long>>) cache.asMap().get(getKey(orderedDate, RESULT_PROMPTS_GROUP_BY_KEYWORD_KEY));
    }

    private List<ResultDefinitionSynonym> getCachedResultDefinitionSynonym(final LocalDate orderedDate) {

        return (List<ResultDefinitionSynonym>) cache.asMap().get(getKey(orderedDate, RESULT_DEFINITION_SYNONYM_KEY));
    }

    private List<ResultPrompt> getCachedResultPrompt(final LocalDate orderedDate) {
        return (List<ResultPrompt>) cache.asMap().get(getKey(orderedDate, RESULT_PROMPT_KEY));
    }

    private List<ResultPromptSynonym> getCachedResultPromptSynonym(final LocalDate orderedDate) {

        return (List<ResultPromptSynonym>) cache.asMap().get(getKey(orderedDate, RESULT_PROMPT_SYNONYM_KEY));
    }

    public void reload() {
        if (cache.asMap().size() != 0) {
            LOGGER.info("Reloading cache by MidnightScheduler ");
            loadResultCache(LocalDate.now());
        }
    }
}
