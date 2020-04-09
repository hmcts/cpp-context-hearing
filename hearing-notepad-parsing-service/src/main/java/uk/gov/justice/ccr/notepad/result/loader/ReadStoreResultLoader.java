package uk.gov.justice.ccr.notepad.result.loader;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.UUID.fromString;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static uk.gov.justice.ccr.notepad.result.cache.model.ResultType.DURATION;
import static uk.gov.justice.ccr.notepad.result.cache.model.ResultType.FIXL;
import static uk.gov.justice.ccr.notepad.result.cache.model.ResultType.isFixedListType;
import static uk.gov.justice.ccr.notepad.result.cache.model.ResultType.valueOf;
import static uk.gov.justice.ccr.notepad.result.loader.ResultPromptReferenceDynamicFixListUUIDMapper.HCHOUSE;
import static uk.gov.justice.ccr.notepad.result.loader.ResultPromptReferenceDynamicFixListUUIDMapper.HTYPE;
import static uk.gov.justice.ccr.notepad.result.loader.ResultPromptReferenceDynamicFixListUUIDMapper.getPromptReferenceDynamicFixListUuids;

import uk.gov.justice.ccr.notepad.result.cache.model.ChildResultDefinition;
import uk.gov.justice.ccr.notepad.result.cache.model.ResultDefinition;
import uk.gov.justice.ccr.notepad.result.cache.model.ResultDefinitionSynonym;
import uk.gov.justice.ccr.notepad.result.cache.model.ResultPrompt;
import uk.gov.justice.ccr.notepad.result.cache.model.ResultPromptFixedList;
import uk.gov.justice.ccr.notepad.result.cache.model.ResultPromptSynonym;
import uk.gov.justice.ccr.notepad.service.ResultingQueryService;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Named;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonString;

import org.apache.commons.lang3.StringUtils;

@SuppressWarnings("squid:S1188")
@Named("readStoreResultLoader")
public class ReadStoreResultLoader implements ResultLoader {

    private static final String FIELD_WORD_GROUP = "wordGroup";

    @Inject
    private ResultingQueryService resultingQueryService;

    private JsonEnvelope jsonEnvelope;

    public void setJsonEnvelope(final JsonEnvelope jsonEnvelope) {
        this.jsonEnvelope = jsonEnvelope;
    }

    @Override
    public List<ResultDefinition> loadResultDefinition(final LocalDate orderedDate) {
        final List<ResultDefinition> resultDefinitions = newArrayList();
        resultingQueryService.getAllDefinitions(jsonEnvelope, orderedDate).payloadAsJsonObject().getJsonArray("resultDefinitions").getValuesAs(JsonObject.class)
                .forEach(jsonObjectResultDefinition ->
                        getKeywordsGroups(jsonObjectResultDefinition).forEach(keywords -> {
                                    final ResultDefinition resultDefinition = new ResultDefinition();
                                    resultDefinition.setId(jsonObjectResultDefinition.getString("id"));
                                    resultDefinition.setLabel(jsonObjectResultDefinition.getString("label").trim());
                                    resultDefinition.setShortCode(jsonObjectResultDefinition.getString("shortCode").toLowerCase().trim());
                                    resultDefinition.setLevel(jsonObjectResultDefinition.getString("level"));
                                    resultDefinition.setKeywords(keywords);
                                    resultDefinition.setTerminatesOffenceProceedings(getBooleanOrNull(jsonObjectResultDefinition, "terminatesOffenceProceedings"));
                                    resultDefinition.setLifeDuration(getBooleanOrNull(jsonObjectResultDefinition, "lifeDuration"));
                                    resultDefinition.getChildResultDefinitions().addAll(getChildResultDefinitions(jsonObjectResultDefinition));
                                    resultDefinition.setPublishedAsAPrompt(getBooleanOrNull(jsonObjectResultDefinition, "publishedAsAPrompt"));
                                    resultDefinition.setExcludedFromResults(getBooleanOrNull(jsonObjectResultDefinition, "excludedFromResults"));
                                    resultDefinition.setAlwaysPublished(getBooleanOrNull(jsonObjectResultDefinition, "alwaysPublished"));
                                    resultDefinition.setUrgent(getBooleanOrNull(jsonObjectResultDefinition, "urgent"));
                                    resultDefinition.setD20(getBooleanOrNull(jsonObjectResultDefinition, "d20"));
                                    resultDefinitions.add(resultDefinition);
                                }
                        ));

        return resultDefinitions;
    }

    private List<ChildResultDefinition> getChildResultDefinitions(final JsonObject jsonObjectResultDefinition) {
        if (jsonObjectResultDefinition.containsKey("resultDefinitionRules")) {
            return jsonObjectResultDefinition.getJsonArray("resultDefinitionRules").getValuesAs(JsonObject.class)
                    .stream()
                    .map(crd -> new ChildResultDefinition(UUID.fromString(crd.getString("childResultDefinitionId")), crd.getString("ruleType")))
                    .collect(toList());
        }

        return emptyList();
    }

    private Boolean getBooleanOrNull(final JsonObject jsonObject, final String key) {
        return jsonObject.containsKey(key) ? jsonObject.getBoolean(key) : null;
    }

    private List<List<String>> getKeywordsGroups(final JsonObject resultDefinition) {
        if (!resultDefinition.containsKey("wordGroups")) {
            return singletonList(emptyList());
        }

        return resultDefinition.getJsonArray("wordGroups").getValuesAs(JsonObject.class)
                .stream()
                .map(wordGroup -> wordGroup.getJsonArray(FIELD_WORD_GROUP).getValuesAs(JsonString.class)
                        .stream()
                        .map(word -> word.getString().toLowerCase())
                        .collect(toList()))
                .collect(toList());
    }

    @Override
    public List<ResultDefinitionSynonym> loadResultDefinitionSynonym(final LocalDate orderedDate) {
        final List<ResultDefinitionSynonym> resultDefinitionSynonyms = newArrayList();
        final JsonArray resultDefinitionSynonymsJson = resultingQueryService.getAllDefinitionWordSynonyms(jsonEnvelope, orderedDate).payloadAsJsonObject().getJsonArray("synonymCollection");
        resultDefinitionSynonymsJson.forEach(jsonValue -> {
            final ResultDefinitionSynonym resultDefinitionSynonym = new ResultDefinitionSynonym();
            resultDefinitionSynonym.setWord(((JsonObject) jsonValue).getString("word").replaceAll(" ", "").trim().toLowerCase());
            resultDefinitionSynonym.setSynonym(((JsonObject) jsonValue).getString("synonym").trim().toLowerCase());
            resultDefinitionSynonyms.add(resultDefinitionSynonym);
        });
        return resultDefinitionSynonyms;
    }

    @Override
    public List<ResultPrompt> loadResultPrompt(final LocalDate orderedDate) {
        final List<ResultPrompt> resultPrompts = newArrayList();
        final Map<String, Set<String>> resultPromptFixedListMap = loadResultPromptFixedList(orderedDate);
        final JsonObject resultDefinitionsJson = resultingQueryService.getAllDefinitions(jsonEnvelope, orderedDate).payloadAsJsonObject();

        final Map<UUID, List<JsonObject>> resultPromptsByIdMap = resultDefinitionsJson.getJsonArray("resultDefinitions").getValuesAs(JsonObject.class)
                .stream()
                .filter(jsonObject -> jsonObject.containsKey("prompts"))
                .collect(toMap(jsonObject -> fromString(jsonObject.getString("id")),
                        jsonObject -> jsonObject.getJsonArray("prompts").getValuesAs(JsonObject.class)));

        resultPromptsByIdMap.forEach((id, prompts) ->
                prompts.forEach(promptJson -> {
                    final ResultPrompt resultPrompt = new ResultPrompt();
                    resultPrompt.setId(promptJson.getString("id"));
                    resultPrompt.setResultDefinitionId(id);
                    resultPrompt.setLabel(promptJson.getString("label").trim());
                    final String durationElement = promptJson.getString("duration", null);
                    if (durationElement != null && !durationElement.isEmpty()) {
                        resultPrompt.setType(DURATION);
                    } else {
                        resultPrompt.setType(valueOf(promptJson.getString("type").trim().toUpperCase()));
                    }
                    final String promptReference = promptJson.getString("reference", null);
                    resultPrompt.setReference(promptReference);
                    resultPrompt.setPromptOrder(promptJson.getInt("sequence"));
                    setResultPromptRule(resultPrompt, promptJson);
                    resultPrompt.setDurationElement(durationElement);
                    resultPrompt.setKeywords(getKeywordsForPrompts(promptJson));

                    final String fixedListId = promptJson.getString("fixedListId", null);
                    if (fixedListId != null && isFixedListType(resultPrompt.getType())) {
                        resultPrompt.setFixedList(resultPromptFixedListMap.get(fixedListId.trim()));
                    } else if (HCHOUSE.equals(promptReference) || HTYPE.equals(promptReference)) {
                        setFixedListValues(resultPromptFixedListMap, resultPrompt, promptReference);
                    }
                    resultPrompt.setDurationSequence(promptJson.getInt("durationSequence", 0));
                    resultPrompts.add(resultPrompt);

                }));
        return resultPrompts;
    }
    
    private void setResultPromptRule(final ResultPrompt resultPrompt, final JsonObject promptJson) {
        String resultPromptRule = promptJson.getString("resultPromptRule", null);
        if (StringUtils.isBlank(resultPromptRule)) {
            resultPromptRule = promptJson.getBoolean("mandatory") ? "mandatory" : "optional";
        }
        resultPrompt.setResultPromptRule(resultPromptRule);
    }

    private void setFixedListValues(final Map<String, Set<String>> resultPromptFixedListMap, final ResultPrompt resultPrompt, final String promptReference) {
        resultPrompt.setFixedList(resultPromptFixedListMap.get(getPromptReferenceDynamicFixListUuids().get(promptReference)));
        //overriding type as this is dynamicFixedList discovered by system
        resultPrompt.setType(FIXL);
    }

    private List<String> getKeywordsForPrompts(final JsonObject resultPromptJson) {
        if (!resultPromptJson.containsKey(FIELD_WORD_GROUP)) {
            return emptyList();
        }

        return resultPromptJson.getJsonArray(FIELD_WORD_GROUP).getValuesAs(JsonString.class).stream()
                .map(word -> word.getString().toLowerCase())
                .collect(toList());
    }

    private Map<String, Set<String>> loadResultPromptFixedList(final LocalDate orderedDate) {
        final Map<String, Set<String>> staticFixedList = resultingQueryService.getAllFixedLists(this.jsonEnvelope, orderedDate).payloadAsJsonObject()
                .getJsonArray("fixedListCollection").getValuesAs(JsonObject.class)
                .stream()
                .flatMap(fixedList -> fixedList.getJsonArray("elements").getValuesAs(JsonObject.class)
                        .stream()
                        .map(element -> {
                            final ResultPromptFixedList resultPromptFixedList = new ResultPromptFixedList();
                            resultPromptFixedList.setId(fixedList.getString("id").trim());
                            resultPromptFixedList.setValue(element.getString("value").trim());
                            return resultPromptFixedList;
                        })
                ).collect(groupingBy(ResultPromptFixedList::getId, mapping(ResultPromptFixedList::getValue, toCollection(TreeSet::new))));
        final Map<String, Set<String>> dynaMicFixedList = loadDynamicPromptFixedList();
        //removing clashes between static and dynamic(dynamic has higher priority
        staticFixedList.keySet().removeAll(dynaMicFixedList.keySet());
        return Stream.concat(dynaMicFixedList.entrySet().stream(), staticFixedList.entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public Map<String, Set<String>> loadDynamicPromptFixedList() {
        final Map<String, Set<String>> result = new ConcurrentHashMap<>();
        result.put(getPromptReferenceDynamicFixListUuids().get(HCHOUSE), getCourtCentres());
        result.put(getPromptReferenceDynamicFixListUuids().get(HTYPE), getHearingTypes());
        return result;
    }

    private Set<String> getCourtCentres() {
        return resultingQueryService.getAllCourtCentre(this.jsonEnvelope).payloadAsJsonObject()
                .getJsonArray("organisationunits").getValuesAs(JsonObject.class)
                .stream()
                .map(element -> element.getString("oucodeL3Name", null))
                .filter(Objects::nonNull)
                .collect(toCollection(TreeSet::new));
    }

    private Set<String> getHearingTypes() {
        return resultingQueryService.getHearingTypes(this.jsonEnvelope).payloadAsJsonObject()
                .getJsonArray("hearingTypes").getValuesAs(JsonObject.class)
                .stream()
                .map(element -> element.getString("hearingDescription", null))
                .filter(Objects::nonNull)
                .collect(toCollection(TreeSet::new));
    }

    @Override
    public List<ResultPromptSynonym> loadResultPromptSynonym(final LocalDate orderedDate) {
        return resultingQueryService.getAllResultPromptWordSynonyms(jsonEnvelope, orderedDate).payloadAsJsonObject()
                .getJsonArray("synonymCollection").getValuesAs(JsonObject.class)
                .stream()
                .map(wordSynonymJson -> {
                    final ResultPromptSynonym resultPromptSynonym = new ResultPromptSynonym();
                    resultPromptSynonym.setWord(wordSynonymJson.getString("word").trim().toLowerCase());
                    resultPromptSynonym.setSynonym(wordSynonymJson.getString("synonym").trim().toLowerCase());
                    return resultPromptSynonym;
                })
                .collect(toList());
    }
}
