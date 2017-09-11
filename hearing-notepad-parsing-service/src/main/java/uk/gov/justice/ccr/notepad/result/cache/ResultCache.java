package uk.gov.justice.ccr.notepad.result.cache;


import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.stream.Collectors.toList;

import uk.gov.justice.ccr.notepad.result.cache.model.ResultDefinition;
import uk.gov.justice.ccr.notepad.result.cache.model.ResultDefinitionSynonym;
import uk.gov.justice.ccr.notepad.result.cache.model.ResultPrompt;
import uk.gov.justice.ccr.notepad.result.cache.model.ResultPromptSynonym;
import uk.gov.justice.ccr.notepad.result.loader.ReadStoreResultLoader;
import uk.gov.justice.ccr.notepad.result.loader.ResultLoader;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;

import javax.ejb.Startup;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Startup
@ApplicationScoped
public class ResultCache {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(ResultCache.class.getName());

    private static final String RESULT_DEFINITION_KEY = "resultDefinitionKey";
    private static final String RESULT_DEFINITIONS_GROUP_BY_KEYWORD_KEY = "resultDefinitionsGroupByKeywordKey";
    private static final String RESULT_PROMPTS_GROUP_BY_KEYWORD_KEY = "resultPromptsGroupByKeywordKey";
    private static final String RESULT_DEFINITION_SYNONYM_KEY = "resultDefinitionSynonymKey";
    private static final String RESULT_PROMPT_KEY = "resultPromptKey";
    private static final String RESULT_PROMPT_SYNONYM_KEY = "resultPromptSynonymKey";

    @Inject
    @Named("readStoreResultLoader")
    ResultLoader resultLoader;

    final LoadingCache<String, Object> cache = CacheBuilder
            .newBuilder()
            .concurrencyLevel(20)
            .maximumSize(10)
            .build(new CacheLoader<String, Object>() {
                @Override
                public Object load(String key) throws Exception {
                    return null;
                }
            });


    public void lazyLoad(final JsonEnvelope envelope) throws ExecutionException {
        if (cache.asMap().size() == 0) {
            synchronized (this) {
                //Double protection to stop multiple cache loads
                if (cache.asMap().size() == 0) {
                    if (resultLoader instanceof ReadStoreResultLoader) {
                        ((ReadStoreResultLoader) resultLoader).setJsonEnvelope(envelope);
                    }
                    loadResultCache();
                }
            }
        }
    }

    private void loadResultCache() throws ExecutionException {

        addValueToCache(RESULT_DEFINITION_KEY, resultLoader.loadResultDefinition());

        Map<String, List<Long>> resultDefinitionsIndexByKeyWord = getResultDefinitionsIndexByKeywords();

        addValueToCache(RESULT_DEFINITIONS_GROUP_BY_KEYWORD_KEY, resultDefinitionsIndexByKeyWord);

        addValueToCache(RESULT_DEFINITION_SYNONYM_KEY, resultLoader.loadResultDefinitionSynonym());

        addValueToCache(RESULT_PROMPT_KEY, resultLoader.loadResultPrompt());

        addValueToCache(RESULT_PROMPT_SYNONYM_KEY, resultLoader.loadResultPromptSynonym());

        Map<String, List<Long>> resultPromptsIndexByKeyWord = getPromptsIndexByKeyword();

        addValueToCache(RESULT_PROMPTS_GROUP_BY_KEYWORD_KEY, resultPromptsIndexByKeyWord);

    }

    private void addValueToCache(final String key, final Object value) {
        LOGGER.info("Add to cache key {} value {}", key, value);
        cache.asMap().put(key, value);
    }

    private Map<String, List<Long>> getPromptsIndexByKeyword() throws ExecutionException {
        Map<String, List<Long>> resultPromptsIndexByKeyWord = Maps.newHashMap();
        AtomicLong indexPromptIncrementer = new AtomicLong();
        getCachedResultPrompt()
                .forEach((ResultPrompt resultPrompt) -> {
                    long index = indexPromptIncrementer.getAndIncrement();
                    resultPrompt.getKeywords().stream().filter(v -> !v.isEmpty()).forEach(word -> {
                        resultPromptsIndexByKeyWord.putIfAbsent(word, newArrayList());
                        resultPromptsIndexByKeyWord.computeIfPresent(word, (s, l) -> {
                            l.add(index);
                            return l;
                        });

                    });
                });
        return resultPromptsIndexByKeyWord;
    }

    private Map<String, List<Long>> getResultDefinitionsIndexByKeywords() throws ExecutionException {
        Map<String, List<Long>> resultDefinitionsIndexByKeyWord = Maps.newHashMap();
        AtomicLong indexIncrementer = new AtomicLong();
        getCachedResultDefinition()
                .forEach((ResultDefinition resultDefinition) -> {
                    long index = indexIncrementer.getAndIncrement();
                    resultDefinition.getKeywords().forEach(word -> {
                        resultDefinitionsIndexByKeyWord.putIfAbsent(word, newArrayList());
                        resultDefinitionsIndexByKeyWord.computeIfPresent(word, (s, resultDefinitions) -> {
                            resultDefinitions.add(index);
                            return resultDefinitions;
                        });

                    });
                });
        return resultDefinitionsIndexByKeyWord;
    }

    public List<ResultDefinition> getResultDefinition() throws ExecutionException {
        return getCachedResultDefinition();
    }

    public Map<String, List<Long>> getResultDefinitionsIndexGroupByKeyword() throws ExecutionException {
        return getCachedResultDefinitionsGroupByKeyword();
    }

    public Map<String, List<Long>> getResultPromptsIndexGroupByKeyword() throws ExecutionException {
        return getCachedResultPromptsGroupByKeyword();
    }

    public List<ResultDefinitionSynonym> getResultDefinitionSynonym() throws ExecutionException {
        Set<String> allKeyWords = newHashSet();
        getResultDefinition().stream().map(ResultDefinition::getKeywords).filter(v -> !v.isEmpty()).forEach(allKeyWords::addAll);
        List<ResultDefinitionSynonym> resultDefinitionKeyWordsSynonyms =
                allKeyWords.stream().map(s -> {
                    ResultDefinitionSynonym resultDefinitionSynonym = new ResultDefinitionSynonym();
                    resultDefinitionSynonym.setSynonym(s);
                    resultDefinitionSynonym.setWord(s);
                    return resultDefinitionSynonym;
                }).collect(toList());
        //Adding all keywords as default entry with itself
        resultDefinitionKeyWordsSynonyms.addAll(getCachedResultDefinitionSynonym());
        return resultDefinitionKeyWordsSynonyms;
    }

    public List<ResultPrompt> getResultPrompt() throws ExecutionException {
        return getCachedResultPrompt();
    }

    public List<ResultPrompt> getResultPromptByResultDefinitionId(final String resultDefinitionId) throws ExecutionException {
        String resultDefinitionLabel = getResultDefinitionById(resultDefinitionId).get().getLabel();
        return getResultPromptByResultDefinitionLabel(resultDefinitionLabel);
    }

    public List<ResultPromptSynonym> getResultPromptSynonym() throws ExecutionException {
        Set<String> allKeyWords = newHashSet();
        getCachedResultPrompt().stream().map(ResultPrompt::getKeywords).filter(v -> !v.isEmpty()).forEach(allKeyWords::addAll);
        List<ResultPromptSynonym> resultPromptSynonyms =
                allKeyWords.stream().map(s -> {
                    ResultPromptSynonym resultPromptSynonym = new ResultPromptSynonym();
                    resultPromptSynonym.setWord(s);
                    resultPromptSynonym.setSynonym(s);
                    return resultPromptSynonym;
                }).collect(toList());
        //Adding all keywords as default entry with itself
        resultPromptSynonyms.addAll(getCachedResultPromptSynonym());
        return resultPromptSynonyms;
    }

    private Optional<ResultDefinition> getResultDefinitionById(final String resultDefinitionId) throws ExecutionException {
        return getResultDefinition().stream()
                .filter(resultDefinition -> resultDefinition.getId().equals(resultDefinitionId)).findFirst();

    }

    private List<ResultPrompt> getResultPromptByResultDefinitionLabel(final String resultDefinitionLabel) throws ExecutionException {
        return getCachedResultPrompt().stream()
                .filter(resultPrompt -> resultPrompt.getResultDefinitionLabel().equalsIgnoreCase(resultDefinitionLabel)).collect(toList());

    }

    private List<ResultDefinition> getCachedResultDefinition() throws ExecutionException {

        return (List<ResultDefinition>) cache.get(RESULT_DEFINITION_KEY);
    }

    private Map<String, List<Long>> getCachedResultDefinitionsGroupByKeyword() throws ExecutionException {
        return (Map<String, List<Long>>) cache.get(RESULT_DEFINITIONS_GROUP_BY_KEYWORD_KEY);
    }

    private Map<String, List<Long>> getCachedResultPromptsGroupByKeyword() throws ExecutionException {
        return (Map<String, List<Long>>) cache.get(RESULT_PROMPTS_GROUP_BY_KEYWORD_KEY);
    }

    private List<ResultDefinitionSynonym> getCachedResultDefinitionSynonym() throws ExecutionException {

        return (List<ResultDefinitionSynonym>) cache.get(RESULT_DEFINITION_SYNONYM_KEY);
    }

    private List<ResultPrompt> getCachedResultPrompt() throws ExecutionException {

        return (List<ResultPrompt>) cache.get(RESULT_PROMPT_KEY);
    }

    private List<ResultPromptSynonym> getCachedResultPromptSynonym() throws ExecutionException {

        return (List<ResultPromptSynonym>) cache.get(RESULT_PROMPT_SYNONYM_KEY);
    }

    public void setResultLoader(ResultLoader resultLoader) {
        this.resultLoader = resultLoader;
    }


    public void reload() throws ExecutionException {
        if (cache.asMap().size() != 0) {
            LOGGER.info("Reloading cache by MidnightScheduler ");
            loadResultCache();
        }
    }
}
