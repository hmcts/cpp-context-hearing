package uk.gov.justice.ccr.notepad.result.loader;

import static com.google.common.collect.Lists.newArrayList;
import static java.lang.Boolean.TRUE;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Optional.ofNullable;
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
import uk.gov.justice.ccr.notepad.result.cache.model.ResultPromptDynamicListNameAddress;
import uk.gov.justice.ccr.notepad.result.cache.model.ResultPromptFixedList;
import uk.gov.justice.ccr.notepad.result.cache.model.ResultPromptSynonym;
import uk.gov.justice.ccr.notepad.result.cache.model.ResultType;
import uk.gov.justice.ccr.notepad.service.ResultsQueryService;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("squid:S1188")
@Named("readStoreResultLoader")
public class ReadStoreResultLoader implements ResultLoader {


    private static final Logger LOGGER = LoggerFactory.getLogger(ReadStoreResultLoader.class);

    //Label
    private static final String FIELD_WORD_GROUP = "wordGroup";
    private static final String ORG_TYPE = "organisationTypes";
    private static final String ORGANISATIONUNITS = "organisationunits";
    private static final String OUCODE_L_3_NAME = "oucodeL3Name";

    //Reference Data Key
    private static final String CONVICTING_COURT_KEY = "convictingCourt".toLowerCase();
    private static final String LOCAL_JUSTICE_AREA_FIXED_PENALTY_ISSUE_KEY = "fixedPenaltyIssuedByLocalJusticeArea".toLowerCase();
    private static final String LOCAL_JUSTICE_AREA_KEY = "localJusticeArea".toLowerCase();
    private static final String LOCAL_JUSTICE_AREA_DEFENDANT_LIVES_KEY = "theLocalJusticeAreaTheDefendantLivesIn".toLowerCase();
    private static final String COUNTRIES_KEY = "countries".toLowerCase();
    private static final String NATIONALITY_KEY = "nationality".toLowerCase();
    private static final String LANGUAGE_KEY = "language".toLowerCase();
    private static final String NAME_OF_PRISON_DEFENDANT_REMANDED_OR_COMMITTED_KEY = "nameOfPrisonDefendantRemandedOrCommittedTo".toLowerCase();
    private static final String DESIGNATED_LOCAL_AUTHORITY_KEY = "designatedLocalAuthority".toLowerCase();
    private static final String LOCAL_AUTHORITY_KEY = "localAuthority".toLowerCase();
    private static final String NAME_OF_LOCAL_AUTHORITY_KEY = "nameOfLocalAuthority".toLowerCase();
    private static final String COURT_THAT_HAS_MADE_ORIG_ORDER_KEY = "courtThatMadeTheOriginalOrder".toLowerCase();
    private static final String COURT_WHICH_IMPOSED_ORIG_SENTENCE_KEY = "courtWhichImposedTheOriginalSentence".toLowerCase();
    private static final String COURT_WHICH_ORDERED_REMAND_KEY = "courtWhichOrderedTheRemand".toLowerCase();
    private static final String IMPOSING_COURT_KEY = "imposingCourt".toLowerCase();
    private static final String NAME_OF_COURT_THAT_IMPOSED_ELECTRONIC_MONITORING_KEY = "nameOfCourtThatImposedElectronicMonitoring".toLowerCase();
    private static final String NAME_OF_COURT_THAT_IMPOSED_SUSPENDED_SENTENCE_KEY = "nameOfCourtThatImposedSuspendedSentence".toLowerCase();
    private static final String NEW_SUPERVISING_COURT_KEY = "newSupervisingCourt".toLowerCase();
    private static final String ORIGINAL_COURT_KEY = "originalCourt".toLowerCase();
    private static final String ORIGINAL_COURT_THAT_MADE_THE_ORDER_KEY = "originalCourtThatMadeTheOrder".toLowerCase();
    private static final String ORIGINAL_ORDER_MADE_BY_KEY = "originalOrderMadeBy".toLowerCase();
    private static final String PETTY_SESSION_DISTRICT_KEY = "pettySessionsDistrict".toLowerCase();
    private static final String SENTENCING_COURT_IF_DIFFERENT_FROM_CONVICTING_KEY = "sentencingCourtIfDifferentFromConvictingCourt".toLowerCase();
    private static final String SUPERVISING_COURT_KEY = "supervisingCourt".toLowerCase();
    private static final String WHICH_WAS_IMPOSED_BY_KEY = "whichWasImpBy".toLowerCase();
    private static final String COURT_NAME = "courtName";


    @Inject
    private ResultsQueryService resultsQueryService;

    @Inject
    private NameAddressRefDataEndPointMapper nameAddressRefDataEndPointMapper;

    private JsonEnvelope jsonEnvelope;


    public void setJsonEnvelope(final JsonEnvelope jsonEnvelope) {
        this.jsonEnvelope = jsonEnvelope;
    }

    @Override
    public List<ResultDefinition> loadResultDefinition(final LocalDate orderedDate) {
        final List<ResultDefinition> resultDefinitions = newArrayList();
        resultsQueryService.getAllDefinitions(jsonEnvelope, orderedDate).payload().getJsonArray("resultDefinitions").getValuesAs(JsonObject.class)
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
                                    resultDefinition.setRollUpPrompts(
                                            getRollUpPrompts(getBooleanOrNull(jsonObjectResultDefinition, "rollUpPrompts"),
                                                    getBooleanOrNull(jsonObjectResultDefinition, "publishedAsAPrompt"),
                                                    getBooleanOrNull(jsonObjectResultDefinition, "excludedFromResults"),
                                                    getBooleanOrNull(jsonObjectResultDefinition, "alwaysPublished")));
                                    resultDefinition.setPublishedForNows(getBooleanOrFalse(jsonObjectResultDefinition, "publishedForNows"));
                                    resultDefinition.setConditonalMandatory(getBooleanOrFalse(jsonObjectResultDefinition, "isBooleanResult"));
                                    if (jsonObjectResultDefinition.containsKey("dvlaCode")) {
                                        resultDefinition.setDvlaCode(jsonObjectResultDefinition.getString("dvlaCode"));
                                    }
                                    resultDefinitions.add(resultDefinition);
                                }
                        ));

        return resultDefinitions;
    }

    private Boolean getRollUpPrompts(final Boolean rollUpPrompts, final Boolean publishedAsAPrompt, final Boolean excludedFromResults, final Boolean alwaysPublished) {
        return ofNullable(rollUpPrompts)
                .orElse(!TRUE.equals(publishedAsAPrompt) && !TRUE.equals(excludedFromResults) && !TRUE.equals(alwaysPublished));
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

    private Boolean getBooleanOrFalse(final JsonObject jsonObject, final String key) {
        return jsonObject.containsKey(key) && jsonObject.getBoolean(key);
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
        final JsonArray resultDefinitionSynonymsJson = resultsQueryService.getAllDefinitionWordSynonyms(jsonEnvelope, orderedDate).payload().getJsonArray("synonymCollection");
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
        final Map<String, Set<ResultPromptDynamicListNameAddress>> resultPromptDynamicListNameAddress = loadResultPromptDynamicListNameAddressList();

        LOGGER.info("spiout:: refdata calling...");
        final JsonObject resultDefinitionsJson = resultsQueryService.getAllDefinitions(jsonEnvelope, orderedDate).payload();
        LOGGER.info("spiout:: refdata called...");

        final Map<UUID, List<JsonObject>> resultPromptsByIdMap = resultDefinitionsJson.getJsonArray("resultDefinitions").getValuesAs(JsonObject.class)
                .stream()
                .filter(jsonObject -> jsonObject.containsKey("prompts"))
                .collect(toMap(jsonObject -> fromString(jsonObject.getString("id")),
                        jsonObject -> jsonObject.getJsonArray("prompts").getValuesAs(JsonObject.class)));

        resultPromptsByIdMap.forEach((id, prompts) ->
                prompts.forEach(promptJson -> {
                    final ResultPrompt resultPrompt = new ResultPrompt();
                    LOGGER.info("spiout:: promptJson {}", promptJson);
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

                    resultPrompt.setWelshDurationElement(promptJson.getString("welshDuration", null));
                    resultPrompt.setKeywords(getKeywordsForPrompts(promptJson));

                    final String fixedListId = promptJson.getString("fixedListId", null);
                    setFixedList(resultPromptFixedListMap, promptJson, resultPrompt, promptReference, fixedListId);
                    final String referenceDataKey = promptJson.getString("referenceDataKey", null);
                    resultPrompt.setReferenceDataKey(referenceDataKey);
                    if (referenceDataKey != null && ResultType.NAMEADDRESS.equals(resultPrompt.getType())) {
                        resultPrompt.setNameAddressList(resultPromptDynamicListNameAddress.get(referenceDataKey.trim().toLowerCase()));
                    }
                    setNameAddressFields(promptJson, resultPrompt);
                    resultPrompt.setDurationSequence(promptJson.getInt("durationSequence", 0));
                    resultPrompt.setHidden(getBooleanOrNull(promptJson, "hidden"));
                    resultPrompt.setMinLength(promptJson.getString("min", null));
                    resultPrompt.setMaxLength(promptJson.getString("max", null));

                    resultPrompt.setDurationStartDate(promptJson.getBoolean("isDurationStartDate",false));
                    resultPrompt.setDurationEndDate(promptJson.getBoolean("isDurationEndDate",false));
                    resultPrompts.add(resultPrompt);

                }));
        return resultPrompts;
    }


    private void setNameAddressFields(JsonObject promptJson, ResultPrompt resultPrompt) {
        final String componentLabel = promptJson.getString("componentLabel", null);
        resultPrompt.setComponentLabel(componentLabel);
        final String addressType = promptJson.getString("nameAddressType", null);
        resultPrompt.setAddressType(addressType);
        final String fixListLabel = promptJson.getString("fixListLabel", null);
        resultPrompt.setListLabel(fixListLabel);
        final String partName = promptJson.getString("partName", null);
        resultPrompt.setPartName(partName);
        resultPrompt.setNameEmail(getBooleanOrNull(promptJson, "nameEmail"));
    }

    private void setFixedList(final Map<String, Set<String>> resultPromptFixedListMap, final JsonObject promptJson, final ResultPrompt resultPrompt, final String promptReference, final String fixedListId) {
        if (isFixedListType(resultPrompt.getType())) {
            if (fixedListId != null) {
                resultPrompt.setFixedList(resultPromptFixedListMap.get(fixedListId.trim()));
            } else {
                final String refDataKey = promptJson.getString("referenceDataKey", null);
                if (refDataKey != null) {
                    resultPrompt.setFixedList(resultPromptFixedListMap.get(refDataKey.trim().toLowerCase()));
                }
            }
        } else if (HCHOUSE.equals(promptReference) || HTYPE.equals(promptReference)) {
            setFixedListValues(resultPromptFixedListMap, resultPrompt, promptReference);
        }
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
        final Map<String, Set<String>> staticFixedList = resultsQueryService.getAllFixedLists(this.jsonEnvelope, orderedDate).payload()
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

    private Map<String, Set<ResultPromptDynamicListNameAddress>> loadResultPromptDynamicListNameAddressList() {
        nameAddressRefDataEndPointMapper.setJsonEnvelope(this.jsonEnvelope);
        nameAddressRefDataEndPointMapper.setResultsQueryService(this.resultsQueryService);
        return nameAddressRefDataEndPointMapper.loadAllNameAddressFromRefData();
    }


    public Map<String, Set<String>> loadDynamicPromptFixedList() {
        final Map<String, Set<String>> result = new ConcurrentHashMap<>();
        result.put(getPromptReferenceDynamicFixListUuids().get(HCHOUSE), getCourtCentres());
        result.put(getPromptReferenceDynamicFixListUuids().get(HTYPE), getHearingTypes());
        final Set<String> ljaNames = getLocalJusticeAreaNames();
        result.put(LOCAL_JUSTICE_AREA_FIXED_PENALTY_ISSUE_KEY, ljaNames);
        result.put(LOCAL_JUSTICE_AREA_KEY, ljaNames);
        result.put(LOCAL_JUSTICE_AREA_DEFENDANT_LIVES_KEY, ljaNames);
        final Set<String> countriesName = getCountriesNames();
        result.put(COUNTRIES_KEY, countriesName);
        result.put(NATIONALITY_KEY, countriesName);
        final Set<String> languages = getLanguages();
        result.put(LANGUAGE_KEY, languages);
        final Set<String> localAuthorities = getLocalAuthorities();
        result.put(DESIGNATED_LOCAL_AUTHORITY_KEY, localAuthorities);
        result.put(LOCAL_AUTHORITY_KEY, localAuthorities);
        result.put(NAME_OF_LOCAL_AUTHORITY_KEY, localAuthorities);
        final Set<String> prisonNames = getPrisonNames();
        result.put(NAME_OF_PRISON_DEFENDANT_REMANDED_OR_COMMITTED_KEY, prisonNames);
        final Set<String> judicialAuthorityNames = getJudicialAuthorityNames();
        result.put(CONVICTING_COURT_KEY, judicialAuthorityNames);
        result.put(COURT_THAT_HAS_MADE_ORIG_ORDER_KEY, judicialAuthorityNames);
        result.put(COURT_WHICH_IMPOSED_ORIG_SENTENCE_KEY, judicialAuthorityNames);
        result.put(COURT_WHICH_ORDERED_REMAND_KEY, judicialAuthorityNames);
        result.put(IMPOSING_COURT_KEY, judicialAuthorityNames);
        result.put(NAME_OF_COURT_THAT_IMPOSED_ELECTRONIC_MONITORING_KEY, judicialAuthorityNames);
        result.put(NAME_OF_COURT_THAT_IMPOSED_SUSPENDED_SENTENCE_KEY, judicialAuthorityNames);
        result.put(NEW_SUPERVISING_COURT_KEY, judicialAuthorityNames);
        result.put(ORIGINAL_COURT_KEY, judicialAuthorityNames);
        result.put(ORIGINAL_COURT_THAT_MADE_THE_ORDER_KEY, judicialAuthorityNames);
        result.put(ORIGINAL_ORDER_MADE_BY_KEY, judicialAuthorityNames);
        result.put(PETTY_SESSION_DISTRICT_KEY, judicialAuthorityNames);
        result.put(SENTENCING_COURT_IF_DIFFERENT_FROM_CONVICTING_KEY, judicialAuthorityNames);
        result.put(SUPERVISING_COURT_KEY, judicialAuthorityNames);
        result.put(WHICH_WAS_IMPOSED_BY_KEY, judicialAuthorityNames);
        return result;
    }

    private Set<String> getJudicialAuthorityNames() {
        final Set<String> judicialAuthorityNames = new TreeSet<>();
        final Set<String> localJusticeAreaNames = getLocalJusticeAreaNames();
        final Set<String> crownCourtNames = getCrownCourtNames();
        final Set<String> scottishNICourtNames = getScottishNICourtNames();
        final Set<String> youthCourtNames = getYouthCourtNames();
        judicialAuthorityNames.addAll(localJusticeAreaNames);
        judicialAuthorityNames.addAll(crownCourtNames);
        judicialAuthorityNames.addAll(scottishNICourtNames);
        judicialAuthorityNames.addAll(youthCourtNames);
        return judicialAuthorityNames;
    }

    private Set<String> getYouthCourtNames() {
        return resultsQueryService.getYouthCourtAddress(jsonEnvelope)
                .payload().getJsonArray("youthCourts").getValuesAs(JsonObject.class)
                .stream()
                .map(element -> element.getString(COURT_NAME, null))
                .filter(Objects::nonNull)
                .collect(toCollection(TreeSet::new));
    }

    private Set<String> getScottishNICourtNames() {
        return resultsQueryService.getScottishCourtAddress(jsonEnvelope)
                .payload().getJsonArray("scottish-ni-courts").getValuesAs(JsonObject.class)
                .stream()
                .map(element -> element.getString(COURT_NAME, null))
                .filter(Objects::nonNull)
                .collect(toCollection(TreeSet::new));

    }

    private Set<String> getCrownCourtNames() {
        return resultsQueryService.getCrownCourtsNameAddress(this.jsonEnvelope).payload()
                .getJsonArray(ORGANISATIONUNITS).getValuesAs(JsonObject.class)
                .stream()
                .map(element -> element.getString(OUCODE_L_3_NAME, null))
                .filter(Objects::nonNull)
                .collect(toCollection(TreeSet::new));
    }

    private Set<String> getCourtCentres() {
        return resultsQueryService.getAllCourtCentre(this.jsonEnvelope).payload()
                .getJsonArray(ORGANISATIONUNITS).getValuesAs(JsonObject.class)
                .stream()
                .map(element -> element.getString(OUCODE_L_3_NAME, null))
                .filter(Objects::nonNull)
                .collect(toCollection(TreeSet::new));
    }

    private Set<String> getHearingTypes() {
        return resultsQueryService.getHearingTypes(this.jsonEnvelope).payload()
                .getJsonArray("hearingTypes").getValuesAs(JsonObject.class)
                .stream()
                .map(element -> element.getString("hearingDescription", null))
                .filter(Objects::nonNull)
                .collect(toCollection(TreeSet::new));
    }

    private Set<String> getLocalJusticeAreaNames() {
        return resultsQueryService.getLocalJusticeAreas(this.jsonEnvelope).payload()
                .getJsonArray("localJusticeAreas").getValuesAs(JsonObject.class)
                .stream()
                .map(element -> element.getString("name", null))
                .filter(Objects::nonNull)
                .collect(toCollection(TreeSet::new));
    }

    private Set<String> getCountriesNames() {
        return resultsQueryService.getCountriesNames(this.jsonEnvelope).payload()
                .getJsonArray("countryNationality").getValuesAs(JsonObject.class)
                .stream()
                .map(element -> element.getString("countryName", null))
                .filter(Objects::nonNull)
                .collect(toCollection(TreeSet::new));
    }

    private Set<String> getLanguages() {
        return resultsQueryService.getLanguages(this.jsonEnvelope).payload()
                .getJsonArray("languages").getValuesAs(JsonObject.class)
                .stream()
                .map(element -> element.getString("description", null))
                .filter(Objects::nonNull)
                .collect(toCollection(TreeSet::new));
    }

    private Set<String> getLocalAuthorities() {
        return resultsQueryService.getLocalAuthorityNameAddress(this.jsonEnvelope).payload()
                .getJsonArray(ORG_TYPE).getValuesAs(JsonObject.class)
                .stream()
                .map(element -> element.getString("orgName", null))
                .filter(Objects::nonNull)
                .collect(toCollection(TreeSet::new));
    }

    private Set<String> getPrisonNames() {
        return resultsQueryService.getPrisonNameAddress(this.jsonEnvelope).payload()
                .getJsonArray("prisons").getValuesAs(JsonObject.class)
                .stream()
                .map(element -> element.getString("name", null))
                .filter(Objects::nonNull)
                .collect(toCollection(TreeSet::new));

    }

    @Override
    public List<ResultPromptSynonym> loadResultPromptSynonym(final LocalDate orderedDate) {
        return resultsQueryService.getAllResultPromptWordSynonyms(jsonEnvelope, orderedDate).payload()
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
