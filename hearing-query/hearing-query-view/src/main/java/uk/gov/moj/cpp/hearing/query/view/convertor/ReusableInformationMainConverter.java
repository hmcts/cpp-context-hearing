package uk.gov.moj.cpp.hearing.query.view.convertor;

import static uk.gov.justice.services.messaging.JsonObjects.createObjectBuilder;
import static org.apache.commons.lang3.StringUtils.getCommonPrefix;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.slf4j.LoggerFactory.getLogger;
import static uk.gov.moj.cpp.hearing.common.ReusableInformation.IdType.APPLICATION;
import static uk.gov.moj.cpp.hearing.common.ReusableInformation.IdType.CASE;
import static uk.gov.moj.cpp.hearing.common.ReusableInformation.IdType.DEFENDANT;
import static uk.gov.moj.cpp.hearing.common.ReusableInformationConverterType.ADDRESS;
import static uk.gov.moj.cpp.hearing.common.ReusableInformationConverterType.FIXL;
import static uk.gov.moj.cpp.hearing.common.ReusableInformationConverterType.FIXLM;
import static uk.gov.moj.cpp.hearing.common.ReusableInformationConverterType.FIXLOM;
import static uk.gov.moj.cpp.hearing.common.ReusableInformationConverterType.INT;
import static uk.gov.moj.cpp.hearing.common.ReusableInformationConverterType.INTC;
import static uk.gov.moj.cpp.hearing.common.ReusableInformationConverterType.NAMEADDRESS;
import static uk.gov.moj.cpp.hearing.common.ReusableInformationConverterType.TXT;
import static uk.gov.moj.cpp.hearing.query.view.service.ReusableInfoService.NATIONALITY;

import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.MasterDefendant;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.moj.cpp.hearing.common.ReusableInformation;
import uk.gov.moj.cpp.hearing.common.ReusableInformation.IdType;
import uk.gov.moj.cpp.hearing.common.ReusableInformationConverterType;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.InvalidPathException;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.JsonPathException;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

public class ReusableInformationMainConverter {

    private static final Logger LOGGER = getLogger(ReusableInformationMainConverter.class);

    /**
     * Use Jackson instead of json-smart. json-smart rejects deeply nested (but valid) case payloads
     * with "Malicious payload, having non natural depths", which breaks reusable-info queries.
     */
    private static final Configuration JSON_PATH_CONFIGURATION = Configuration.builder()
            .jsonProvider(new JacksonJsonProvider())
            .mappingProvider(new JacksonMappingProvider())
            .build();

    @Inject
    private ReusableInformationIntConverter reusableInformationIntConverter;

    @Inject
    private ReusableInformationTxtConverter reusableInformationTxtConverter;

    @Inject
    private ReusableInformationFixlConverter reusableInformationFixlConverter;

    @Inject
    private ReusableInformationFixlmConverter reusableInformationFixlmConverter;

    @Inject
    private ReusableInformationFixlomConverter reusableInformationFixlomConverter;

    @Inject
    private ReusableInformationINTCConverter reusableInformationINTCConverter;

    @Inject
    private ReusableInformationObjectTypeConverter reusableInformationObjectTypeConverter;

    @Inject
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @Inject
    private CustomReusableInfoConverter customReusableInfoConverter;

    private static final String DELIMITER = "$.";

    private static final String PATH_SPLITTER = ";";

    public Map<Defendant, List<JsonObject>> convertDefendant(final Collection<Defendant> defendants, final List<Prompt> prompts, final Map<String, Map<String, String>> customPromptValues) {

        final Map<Defendant, List<JsonObject>> defendantListMap = new HashMap<>();
        final Map<String, String> countryCodesMap = getCountryCodesMap(customPromptValues);

        defendants.forEach(defendant -> {
            final List<JsonObject> jsonObjects = new ArrayList<>();

            final DocumentContext defendantDocumentContext = parseJson(objectToJsonObjectConverter.convert(defendant).toString());

            addReusableInformationForObjectTypeIfPresent(prompts, DEFENDANT, defendant.getMasterDefendantId(), jsonObjects, defendantDocumentContext, ADDRESS);
            addReusableInformationForObjectTypeIfPresent(prompts, DEFENDANT, defendant.getMasterDefendantId(), jsonObjects, defendantDocumentContext, NAMEADDRESS);

            addReusableInformationForNonObjectTypeIfPresent(prompts, DEFENDANT, defendant.getMasterDefendantId(), jsonObjects, defendantDocumentContext, countryCodesMap);

            defendantListMap.put(defendant, jsonObjects);
        });

        return defendantListMap;
    }

    public Map<MasterDefendant, List<JsonObject>> convertMasterDefendant(final Collection<MasterDefendant> defendants, final List<Prompt> prompts) {

        final Map<MasterDefendant, List<JsonObject>> defendantListMap = new HashMap<>();

        defendants.forEach(defendant -> {
            final List<JsonObject> jsonObjects = new ArrayList<>();

            final DocumentContext defendantDocumentContext = parseJson(objectToJsonObjectConverter.convert(defendant).toString());

            addReusableInformationForNonObjectTypeIfPresent(prompts, DEFENDANT, defendant.getMasterDefendantId(), jsonObjects, defendantDocumentContext, Collections.emptyMap());

            defendantListMap.put(defendant, jsonObjects);
        });

        return defendantListMap;
    }


    public Map<ProsecutionCase, List<JsonObject>> convertCase(final Collection<ProsecutionCase> cases, final List<Prompt> prompts, final Map<String, Map<String, String>> customPromptValues) {

        final Map<ProsecutionCase, List<JsonObject>> caseListMap = new HashMap<>();
        final Map<String, String> countryCodesMap = getCountryCodesMap(customPromptValues);

        cases.forEach(prosecutionCase -> {
            final List<JsonObject> jsonObjects = new ArrayList<>();

            final DocumentContext caseDocumentContext = parseJson(objectToJsonObjectConverter.convert(prosecutionCase).toString());

            addReusableInformationForObjectTypeIfPresent(prompts, CASE, prosecutionCase.getId(), jsonObjects, caseDocumentContext, ADDRESS);
            addReusableInformationForObjectTypeIfPresent(prompts, CASE, prosecutionCase.getId(), jsonObjects, caseDocumentContext, NAMEADDRESS);

            addReusableInformationForNonObjectTypeIfPresent(prompts, CASE, prosecutionCase.getId(), jsonObjects, caseDocumentContext, countryCodesMap);

            caseListMap.put(prosecutionCase, jsonObjects);
        });

        return caseListMap;
    }


    public Map<CourtApplication, List<JsonObject>> convertApplication(final Collection<CourtApplication> courtApplications, final List<Prompt> allPrompts) {

        final Map<CourtApplication, List<JsonObject>> applicationListMap = new HashMap<>();
        courtApplications.forEach(courtApplication -> {
            final List<JsonObject> jsonObjects = new ArrayList<>();
            final DocumentContext applicationDocumentContext = parseJson(objectToJsonObjectConverter.convert(courtApplication).toString());

            final List<Prompt> promptsByType = getPromptsByType(allPrompts, NAMEADDRESS);

            getPromptsGroupedByReferencePrefix(promptsByType)
                    .forEach((promptRef, promptsByReference) -> {

                        final JsonObject objectTypeValueJsonObject = promptsToJsonObjectCacheDataPathList(promptsByReference, applicationDocumentContext, NAMEADDRESS);
                        final Integer cacheable = promptsByReference.get(0).getCacheable();
                        final String cacheDataPath = promptsByReference.get(0).getCacheDataPath();

                        generateObjectTypeJsonObject(APPLICATION, courtApplication.getId(), objectTypeValueJsonObject, NAMEADDRESS, cacheable, cacheDataPath, promptRef)
                                .ifPresent(jsonObjects::add);
                    });

            applicationListMap.put(courtApplication, jsonObjects);
        });
        return applicationListMap;
    }

    private Map<String, String> getCountryCodesMap(final Map<String, Map<String, String>> customPromptValues) {
        if (customPromptValues == null) {
            return Collections.emptyMap();
        }
        for (final Map.Entry<String, Map<String, String>> customPromptValue : customPromptValues.entrySet()) {
            if (NATIONALITY.equalsIgnoreCase(customPromptValue.getKey()) && customPromptValue.getValue() != null) {
                return customPromptValue.getValue();
            }
        }
        return Collections.emptyMap();
    }

    private void addReusableInformationForNonObjectTypeIfPresent(final List<Prompt> prompts,
                                                                 final IdType idType,
                                                                 final UUID id,
                                                                 final List<JsonObject> jsonObjects,
                                                                 final DocumentContext documentContext,
                                                                 final Map<String, String> countryCodesMap) {
        prompts.stream()
                .filter(prompt -> !StringUtils.equals(ADDRESS.name(), prompt.getType()))
                .filter(prompt -> !StringUtils.equals(NAMEADDRESS.name(), prompt.getType()))
                .forEach(prompt -> processReusableInformationForPrompt(idType, id, jsonObjects, documentContext, prompt, countryCodesMap));
    }

    private void processReusableInformationForPrompt(final IdType idType,
                                                     final UUID id,
                                                     final List<JsonObject> jsonObjects,
                                                     final DocumentContext documentContext,
                                                     final Prompt prompt,
                                                     final Map<String, String> countryCodesMap) {
        if (TXT.name().equals(prompt.getType())) {

            addReusableInformationForTxtIfPresent(idType, id, jsonObjects, documentContext, prompt);
        } else if (INT.name().equals(prompt.getType())) {

            addReusableInformationForIntIfPresent(idType, id, jsonObjects, documentContext, prompt);
        } else if (FIXL.name().equals(prompt.getType())) {

            addReusableInformationForFixlIfPresent(idType, id, jsonObjects, documentContext, prompt);
        } else if (FIXLM.name().equals(prompt.getType())) {

            addReusableInformationForFixlm(idType, id, jsonObjects, documentContext, prompt, countryCodesMap);
        } else if (FIXLOM.name().equals(prompt.getType())) {

            addReusableInformationForFixlom(idType, id, jsonObjects, documentContext, prompt, countryCodesMap);
        } else if (INTC.name().equals(prompt.getType())) {

            addReusableInformationForINTCIfPresent(idType, id, jsonObjects, documentContext, prompt);
        } else {
            LOGGER.warn("Unsupported Prompt Type for Prompt Id: {}", prompt.getId());
        }
    }

    private void addReusableInformationForObjectTypeIfPresent(final List<Prompt> prompts,
                                                              final IdType idType,
                                                              final UUID id,
                                                              final List<JsonObject> jsonObjects,
                                                              final DocumentContext documentContext,
                                                              final ReusableInformationConverterType reusableInformationConverterType) {

        getPromptsGroupedByReferencePrefix(getPromptsByType(prompts, reusableInformationConverterType))
                .forEach((promptRef, addressPrompts) -> {

                    final JsonObject objectTypeValueJsonObject = promptsToJsonObject(addressPrompts, documentContext, reusableInformationConverterType);
                    final Integer cacheable = addressPrompts.get(0).getCacheable();
                    final String cacheDataPath = addressPrompts.get(0).getCacheDataPath();

                    generateObjectTypeJsonObject(idType, id, objectTypeValueJsonObject, reusableInformationConverterType, cacheable, cacheDataPath, promptRef)
                            .ifPresent(jsonObjects::add);
                });
    }

    private void addReusableInformationForFixlom(final IdType idType,
                                                 final UUID id,
                                                 final List<JsonObject> jsonObjects,
                                                 final DocumentContext documentContext,
                                                 final Prompt prompt,
                                                 final Map<String, String> countryCodesMap) {
        final JsonObject jsonObject = reusableInformationFixlomConverter.toJsonObject(getStringListReusableInformation(prompt.getReference(),
                idType,
                id,
                prompt.getCacheDataPath(),
                documentContext,
                prompt.getCacheable(),
                countryCodesMap));

        jsonObjects.add(jsonObject);
    }

    private void addReusableInformationForFixlm(final IdType idType,
                                                final UUID id,
                                                final List<JsonObject> jsonObjects,
                                                final DocumentContext documentContext,
                                                final Prompt prompt,
                                                final Map<String, String> countryCodesMap) {
        final JsonObject jsonObject = reusableInformationFixlmConverter.toJsonObject(getStringListReusableInformation(prompt.getReference(),
                idType,
                id,
                prompt.getCacheDataPath(),
                documentContext,
                prompt.getCacheable(),
                countryCodesMap));

        jsonObjects.add(jsonObject);
    }

    private void addReusableInformationForFixlIfPresent(final IdType idType, final UUID id, final List<JsonObject> jsonObjects, final DocumentContext documentContext, final Prompt prompt) {
        final Optional<String> promptValueOptional = toTxtValue(documentContext, prompt.getCacheDataPath());

        promptValueOptional.ifPresent(promptValue -> jsonObjects.add(reusableInformationFixlConverter.toJsonObject(getStringReusableInformation(prompt.getReference(),
                idType,
                id,
                promptValue,
                prompt.getCacheable(),
                prompt.getCacheDataPath()))));
    }

    private void addReusableInformationForIntIfPresent(final IdType idType, final UUID id, final List<JsonObject> jsonObjects, final DocumentContext documentContext, final Prompt prompt) {
        final Optional<String> promptValueOptional = toTxtValue(documentContext, prompt.getCacheDataPath());

        promptValueOptional.ifPresent(promptValue -> jsonObjects.add(reusableInformationIntConverter.toJsonObject(getIntegerReusableInformation(prompt.getReference(),
                idType,
                id,
                promptValue,
                prompt.getCacheable(),
                prompt.getCacheDataPath()))));
    }

    private void addReusableInformationForTxtIfPresent(final IdType idType, final UUID id, final List<JsonObject> jsonObjects, final DocumentContext documentContext, final Prompt prompt) {
        final StringBuilder promptValue = new StringBuilder();

        final List<String> cacheDataPathList = Arrays.asList(prompt.getCacheDataPath().split(PATH_SPLITTER));
        cacheDataPathList.forEach(promptPath ->
                toTxtValue(documentContext, promptPath).ifPresent(val -> promptValue.append(StringUtils.SPACE).append(val))
        );

        if (promptValue.length() > 0) {
            jsonObjects.add(reusableInformationTxtConverter.toJsonObject(getStringReusableInformation(prompt.getReference(),
                    idType,
                    id,
                    promptValue.toString().trim(),
                    prompt.getCacheable(),
                    prompt.getCacheDataPath())));
        }
    }

    private void addReusableInformationForINTCIfPresent(final IdType idType, final UUID id, final List<JsonObject> jsonObjects, final DocumentContext documentContext, final Prompt prompt) {
        final StringBuilder promptValue = new StringBuilder();

        final List<String> cacheDataPathList = Arrays.asList(prompt.getCacheDataPath().split(PATH_SPLITTER));
        cacheDataPathList.forEach(promptPath ->
                toTxtValue(documentContext, promptPath).ifPresent(val -> promptValue.append(StringUtils.SPACE).append(val))
        );

        if (promptValue.length() > 0) {
            jsonObjects.add(reusableInformationINTCConverter.toJsonObject(getStringReusableInformation(prompt.getReference(),
                    idType,
                    id,
                    promptValue.toString().trim(),
                    prompt.getCacheable(),
                    prompt.getCacheDataPath())));
        }
    }

    private List<String> getPromptValues(final DocumentContext documentContext, final String promptPath) {
        final List<String> promptPathList = Arrays.asList(promptPath.split(PATH_SPLITTER));

        return promptPathList.stream()
                .map(path -> toTxtValue(documentContext, path))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private ReusableInformation<String> getIntegerReusableInformation(final String promptReference,
                                                                      final IdType idType,
                                                                      final UUID id,
                                                                      final String promptValue,
                                                                      final Integer cacheable,
                                                                      final String cacheDataPath) {
        return new ReusableInformation.Builder<String>()
                .withPromptRef(promptReference)
                .withIdType(idType)
                .withId(id)
                .withValue(promptValue)
                .withCacheable(cacheable)
                .withCacheDataPath(cacheDataPath)
                .build();
    }

    private ReusableInformation<String> getStringReusableInformation(final String promptReference,
                                                                     final IdType idType,
                                                                     final UUID id,
                                                                     final String promptValue,
                                                                     final Integer cacheable,
                                                                     final String cacheDataPath) {

        return new ReusableInformation.Builder<String>()
                .withPromptRef(promptReference)
                .withIdType(idType)
                .withId(id)
                .withValue(promptValue)
                .withCacheable(cacheable)
                .withCacheDataPath(cacheDataPath)
                .build();
    }

    private ReusableInformation<List<String>> getStringListReusableInformation(final String promptReference,
                                                                               final IdType idType,
                                                                               final UUID id,
                                                                               final String promptPath,
                                                                               final DocumentContext documentContext,
                                                                               final Integer cacheable,
                                                                               final Map<String, String> countryCodesMap) {
        return new ReusableInformation.Builder<List<String>>()
                .withIdType(idType)
                .withId(id)
                .withPromptRef(promptReference)
                .withValue(customReusableInfoConverter.getConvertedValues(getPromptValues(documentContext, promptPath), promptReference, countryCodesMap))
                .withCacheable(cacheable)
                .withCacheDataPath(promptPath)
                .build();
    }

    private Optional<JsonObject> generateObjectTypeJsonObject(final IdType idType,
                                                              final UUID id,
                                                              final JsonObject objectTypeValueJsonObject,
                                                              final ReusableInformationConverterType reusableInformationConverterType,
                                                              final Integer cacheable,
                                                              final String cacheDataPath,
                                                              final String promptRef) {

        if (objectTypeValueJsonObject.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(reusableInformationObjectTypeConverter.toJsonObject(new ReusableInformation.Builder<JsonObject>()
                .withPromptRef(promptRef)
                .withIdType(idType)
                .withId(id)
                .withValue(objectTypeValueJsonObject)
                .withCacheable(cacheable)
                .withCacheDataPath(cacheDataPath)
                .build(), reusableInformationConverterType));
    }

    private JsonObject promptsToJsonObject(final List<Prompt> prompts, final DocumentContext documentContext, final ReusableInformationConverterType reusableInformationConverterType) {

        final JsonObjectBuilder objectTypeValueJsonObjectBuilder = createObjectBuilder();

        prompts.stream()
                .filter(prompt -> StringUtils.equalsIgnoreCase(reusableInformationConverterType.name(), prompt.getType()))
                .forEach(prompt -> {
                    final Optional<String> value = toTxtValue(documentContext, prompt.getCacheDataPath());
                    value.ifPresent(v -> objectTypeValueJsonObjectBuilder.add(prompt.getReference(), v));
                });

        return objectTypeValueJsonObjectBuilder.build();
    }

    private JsonObject promptsToJsonObjectCacheDataPathList(final List<Prompt> prompts, final DocumentContext documentContext, final ReusableInformationConverterType reusableInformationConverterType) {

        final JsonObjectBuilder objectTypeValueJsonObjectBuilder = createObjectBuilder();

        prompts.stream()
                .filter(prompt -> StringUtils.equalsIgnoreCase(reusableInformationConverterType.name(), prompt.getType()))
                .forEach(prompt -> {
                    final List<String> cacheDataPathList = Arrays.asList(prompt.getCacheDataPath().split(PATH_SPLITTER));
                    for (final String promptPath : cacheDataPathList) {
                        final Optional<String> value = toTxtValue(documentContext, promptPath.trim());
                        if (value.isPresent()) {
                            objectTypeValueJsonObjectBuilder.add(prompt.getReference(), value.get().trim());
                            break; // first matching path for this prompt reference
                        }
                    }
                });

        return objectTypeValueJsonObjectBuilder.build();
    }

    private List<Prompt> getPromptsByType(final List<Prompt> prompts, final ReusableInformationConverterType reusableInformationConverterType) {
        return prompts.stream()
                .filter(prompt -> StringUtils.equalsIgnoreCase(reusableInformationConverterType.name(), prompt.getType()))
                .collect(Collectors.toList());
    }

    private Map<String, List<Prompt>> getPromptsGroupedByReferencePrefix(final List<Prompt> allPrompts) {
        final Map<UUID, List<Prompt>> promptIdPromptListMap = allPrompts.stream().collect(Collectors.groupingByConcurrent(Prompt::getId));

        final Map<String, List<Prompt>> addressTypePromptsAsGrouped = new HashMap<>();
        promptIdPromptListMap.forEach((promptId, promptList) -> {
            final boolean hasPartName =  promptList.stream().anyMatch(p -> isNotBlank(p.getPartName()));
            final Set<String> promptReferenceSet = promptList.stream().map(Prompt::getReference).filter(StringUtils::isNotEmpty).collect(Collectors.toSet());
            if(!promptReferenceSet.isEmpty()) {
                if (hasPartName) {
                    addressTypePromptsAsGrouped.put(getCommonPrefix(promptReferenceSet.toArray(new String[0])), promptList);
                } else {
                    addressTypePromptsAsGrouped.put(promptList.get(0).getReference(), promptList);
                }
            }

        });

        return addressTypePromptsAsGrouped;
    }

    private DocumentContext parseJson(final String json) {
        return JsonPath.using(JSON_PATH_CONFIGURATION).parse(json);
    }

    @SuppressWarnings("squid:S1166")
    private Optional<String> toTxtValue(final DocumentContext documentContext, final String promptPath) {
        try {
            final Object objectValue = documentContext.read(DELIMITER + promptPath);
            if (objectValue instanceof Collection<?>) {
                return ((Collection<?>) objectValue).stream()
                        .findFirst()
                        .map(String::valueOf);
            }
            if (objectValue == null) {
                return Optional.empty();
            }
            return Optional.of(String.valueOf(objectValue));
        } catch (final InvalidPathException e) {
            LOGGER.debug("Cannot find path in documentContext. Exception: {}", e.getMessage());
            return Optional.empty();
        } catch (final JsonPathException e) {
            LOGGER.warn("Unable to evaluate JsonPath '{}'. Exception: {}", promptPath, e.getMessage());
            return Optional.empty();
        }
    }

}
