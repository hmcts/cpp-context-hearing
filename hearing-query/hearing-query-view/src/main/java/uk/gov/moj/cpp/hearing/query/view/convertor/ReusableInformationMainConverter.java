package uk.gov.moj.cpp.hearing.query.view.convertor;

import static javax.json.Json.createObjectBuilder;
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

import com.jayway.jsonpath.InvalidPathException;
import com.jayway.jsonpath.JsonPath;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

public class ReusableInformationMainConverter {

    private static final Logger LOGGER = getLogger(ReusableInformationMainConverter.class);

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
        populateRequiredDetailsForCustomValueConverter(customPromptValues);

        defendants.forEach(defendant -> {
            final List<JsonObject> jsonObjects = new ArrayList<>();

            final JsonObject defendantJsonObject = objectToJsonObjectConverter.convert(defendant);
            final String defendantJsonObjectString = defendantJsonObject.toString();

            addReusableInformationForObjectTypeIfPresent(prompts, DEFENDANT, defendant.getMasterDefendantId(), jsonObjects, defendantJsonObjectString, ADDRESS);
            addReusableInformationForObjectTypeIfPresent(prompts, DEFENDANT, defendant.getMasterDefendantId(), jsonObjects, defendantJsonObjectString, NAMEADDRESS);

            addReusableInformationForNonObjectTypeIfPresent(prompts, DEFENDANT, defendant.getMasterDefendantId(), jsonObjects, defendantJsonObjectString);

            defendantListMap.put(defendant, jsonObjects);
        });

        return defendantListMap;
    }

    public Map<MasterDefendant, List<JsonObject>> convertMasterDefendant(final Collection<MasterDefendant> defendants, final List<Prompt> prompts) {

        final Map<MasterDefendant, List<JsonObject>> defendantListMap = new HashMap<>();

        defendants.forEach(defendant -> {
            final List<JsonObject> jsonObjects = new ArrayList<>();

            final JsonObject defendantJsonObject = objectToJsonObjectConverter.convert(defendant);
            final String defendantJsonObjectString = defendantJsonObject.toString();

            addReusableInformationForNonObjectTypeIfPresent(prompts, DEFENDANT, defendant.getMasterDefendantId(), jsonObjects, defendantJsonObjectString);

            defendantListMap.put(defendant, jsonObjects);
        });

        return defendantListMap;
    }


    public Map<ProsecutionCase, List<JsonObject>> convertCase(final Collection<ProsecutionCase> cases, final List<Prompt> prompts, final Map<String, Map<String, String>> customPromptValues) {

        final Map<ProsecutionCase, List<JsonObject>> caseListMap = new HashMap<>();
        populateRequiredDetailsForCustomValueConverter(customPromptValues);

        cases.forEach(prosecutionCase -> {
            final List<JsonObject> jsonObjects = new ArrayList<>();

            final JsonObject caseJsonObject = objectToJsonObjectConverter.convert(prosecutionCase);
            final String caseJsonObjectString = caseJsonObject.toString();

            addReusableInformationForObjectTypeIfPresent(prompts, CASE, prosecutionCase.getId(), jsonObjects, caseJsonObjectString, ADDRESS);
            addReusableInformationForObjectTypeIfPresent(prompts, CASE, prosecutionCase.getId(), jsonObjects, caseJsonObjectString, NAMEADDRESS);

            addReusableInformationForNonObjectTypeIfPresent(prompts, CASE, prosecutionCase.getId(), jsonObjects, caseJsonObjectString);

            caseListMap.put(prosecutionCase, jsonObjects);
        });

        return caseListMap;
    }


    public Map<CourtApplication, List<JsonObject>> convertApplication(final Collection<CourtApplication> courtApplications, final List<Prompt> allPrompts) {

        final Map<CourtApplication, List<JsonObject>> applicationListMap = new HashMap<>();
        courtApplications.forEach(courtApplication -> {
            final List<JsonObject> jsonObjects = new ArrayList<>();
            final JsonObject applicationJsonObject = objectToJsonObjectConverter.convert(courtApplication);
            final String applicationJsonObjectString = applicationJsonObject.toString();

            final List<Prompt> promptsByType = getPromptsByType(allPrompts, NAMEADDRESS);

            getPromptsGroupedByReferencePrefix(promptsByType)
                    .forEach((promptRef, promptsByReference) -> {

                        final JsonObject objectTypeValueJsonObject = promptsToJsonObjectCacheDataPathList(promptsByReference, applicationJsonObjectString, NAMEADDRESS);
                        final Integer cacheable = promptsByReference.get(0).getCacheable();
                        final String cacheDataPath = promptsByReference.get(0).getCacheDataPath();

                        generateObjectTypeJsonObject(APPLICATION, courtApplication.getId(), objectTypeValueJsonObject, NAMEADDRESS, cacheable, cacheDataPath, promptRef)
                                .ifPresent(jsonObjects::add);
                    });

            applicationListMap.put(courtApplication, jsonObjects);
        });
        return applicationListMap;
    }

    private void populateRequiredDetailsForCustomValueConverter(final Map<String, Map<String, String>> customPromptValues) {
        for (final Map.Entry<String, Map<String, String>> customPromptValue : customPromptValues.entrySet()) {
            if (NATIONALITY.equalsIgnoreCase(customPromptValue.getKey())) {
                customReusableInfoConverter.setCountryCodesMap(customPromptValue.getValue());
            }
        }
    }

    private void addReusableInformationForNonObjectTypeIfPresent(final List<Prompt> prompts, final IdType idType, final UUID id, final List<JsonObject> jsonObjects, final String defendantJsonObjectString) {
        prompts.stream()
                .filter(prompt -> !StringUtils.equals(ADDRESS.name(), prompt.getType()))
                .filter(prompt -> !StringUtils.equals(NAMEADDRESS.name(), prompt.getType()))
                .forEach(prompt -> processReusableInformationForPrompt(idType, id, jsonObjects, defendantJsonObjectString, prompt));
    }

    private void processReusableInformationForPrompt(final IdType idType, final UUID id, final List<JsonObject> jsonObjects, final String defendantJsonObjectString, final Prompt prompt) {
        if (TXT.name().equals(prompt.getType())) {

            addReusableInformationForTxtIfPresent(idType, id, jsonObjects, defendantJsonObjectString, prompt);
        } else if (INT.name().equals(prompt.getType())) {

            addReusableInformationForIntIfPresent(idType, id, jsonObjects, defendantJsonObjectString, prompt);
        } else if (FIXL.name().equals(prompt.getType())) {

            addReusableInformationForFixlIfPresent(idType, id, jsonObjects, defendantJsonObjectString, prompt);
        } else if (FIXLM.name().equals(prompt.getType())) {

            addReusableInformationForFixlm(idType, id, jsonObjects, defendantJsonObjectString, prompt);
        } else if (FIXLOM.name().equals(prompt.getType())) {

            addReusableInformationForFixlom(idType, id, jsonObjects, defendantJsonObjectString, prompt);
        } else if (INTC.name().equals(prompt.getType())) {

            addReusableInformationForINTCIfPresent(idType, id, jsonObjects, defendantJsonObjectString, prompt);
        } else {
            LOGGER.warn("Unsupported Prompt Type for Prompt Id: {}", prompt.getId());
        }
    }

    private void addReusableInformationForObjectTypeIfPresent(final List<Prompt> prompts,
                                                              final IdType idType,
                                                              final UUID id,
                                                              final List<JsonObject> jsonObjects,
                                                              final String defendantJsonObjectString,
                                                              final ReusableInformationConverterType reusableInformationConverterType) {

        getPromptsGroupedByReferencePrefix(getPromptsByType(prompts, reusableInformationConverterType))
                .forEach((promptRef, addressPrompts) -> {

                    final JsonObject objectTypeValueJsonObject = promptsToJsonObject(addressPrompts, defendantJsonObjectString, reusableInformationConverterType);
                    final Integer cacheable = addressPrompts.get(0).getCacheable();
                    final String cacheDataPath = addressPrompts.get(0).getCacheDataPath();

                    generateObjectTypeJsonObject(idType, id, objectTypeValueJsonObject, reusableInformationConverterType, cacheable, cacheDataPath, promptRef)
                            .ifPresent(jsonObjects::add);
                });
    }

    private void addReusableInformationForFixlom(final IdType idType, final UUID id, final List<JsonObject> jsonObjects, final String defendantJsonObjectString, final Prompt prompt) {
        final JsonObject jsonObject = reusableInformationFixlomConverter.toJsonObject(getStringListReusableInformation(prompt.getReference(),
                idType,
                id,
                prompt.getCacheDataPath(),
                defendantJsonObjectString,
                prompt.getCacheable()));

        jsonObjects.add(jsonObject);
    }

    private void addReusableInformationForFixlm(final IdType idType, final UUID id, final List<JsonObject> jsonObjects, final String defendantJsonObjectString, final Prompt prompt) {
        final JsonObject jsonObject = reusableInformationFixlmConverter.toJsonObject(getStringListReusableInformation(prompt.getReference(),
                idType,
                id,
                prompt.getCacheDataPath(),
                defendantJsonObjectString,
                prompt.getCacheable()));

        jsonObjects.add(jsonObject);
    }

    private void addReusableInformationForFixlIfPresent(final IdType idType, final UUID id, final List<JsonObject> jsonObjects, final String defendantJsonObjectString, final Prompt prompt) {
        final Optional<String> promptValueOptional = toTxtValue(defendantJsonObjectString, prompt.getCacheDataPath());

        promptValueOptional.ifPresent(promptValue -> jsonObjects.add(reusableInformationFixlConverter.toJsonObject(getStringReusableInformation(prompt.getReference(),
                idType,
                id,
                promptValue,
                prompt.getCacheable(),
                prompt.getCacheDataPath()))));
    }

    private void addReusableInformationForIntIfPresent(final IdType idType, final UUID id, final List<JsonObject> jsonObjects, final String defendantJsonObjectString, final Prompt prompt) {
        final Optional<String> promptValueOptional = toTxtValue(defendantJsonObjectString, prompt.getCacheDataPath());

        promptValueOptional.ifPresent(promptValue -> jsonObjects.add(reusableInformationIntConverter.toJsonObject(getIntegerReusableInformation(prompt.getReference(),
                idType,
                id,
                promptValue,
                prompt.getCacheable(),
                prompt.getCacheDataPath()))));
    }

    private void addReusableInformationForTxtIfPresent(final IdType idType, final UUID id, final List<JsonObject> jsonObjects, final String defendantJsonObjectString, final Prompt prompt) {
        final StringBuilder promptValue = new StringBuilder(StringUtils.EMPTY);

        final List<String> cacheDataPathList = Arrays.asList(prompt.getCacheDataPath().split(PATH_SPLITTER));
        cacheDataPathList.forEach(promptPath ->
                toTxtValue(defendantJsonObjectString, promptPath).ifPresent(promptValueOptional -> promptValue.append(StringUtils.SPACE + promptValueOptional))
        );

        if (promptValue.capacity() > 0) {
            jsonObjects.add(reusableInformationTxtConverter.toJsonObject(getStringReusableInformation(prompt.getReference(),
                    idType,
                    id,
                    promptValue.toString().trim(),
                    prompt.getCacheable(),
                    prompt.getCacheDataPath())));
        }
    }
    private void addReusableInformationForINTCIfPresent(final IdType idType, final UUID id, final List<JsonObject> jsonObjects, final String defendantJsonObjectString, final Prompt prompt) {
        final StringBuilder promptValue = new StringBuilder(StringUtils.EMPTY);

        final List<String> cacheDataPathList = Arrays.asList(prompt.getCacheDataPath().split(PATH_SPLITTER));
        cacheDataPathList.forEach(promptPath ->
                toTxtValue(defendantJsonObjectString, promptPath).ifPresent(promptValueOptional -> promptValue.append(StringUtils.SPACE + promptValueOptional))
        );

        if (promptValue.capacity() > 0) {
            jsonObjects.add(reusableInformationINTCConverter.toJsonObject(getStringReusableInformation(prompt.getReference(),
                    idType,
                    id,
                    promptValue.toString().trim(),
                    prompt.getCacheable(),
                    prompt.getCacheDataPath())));
        }
    }

    private List<String> getPromptValues(final String defendantJsonObjectString, final String promptPath) {
        final List<String> promptPathList = Arrays.asList(promptPath.split(PATH_SPLITTER));

        return promptPathList.stream()
                .map(path -> toTxtValue(defendantJsonObjectString, path))
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
                                                                               final String defendantJsonObjectString,
                                                                               final Integer cacheable) {
        return new ReusableInformation.Builder<List<String>>()
                .withIdType(idType)
                .withId(id)
                .withPromptRef(promptReference)
                .withValue(customReusableInfoConverter.getConvertedValues(getPromptValues(defendantJsonObjectString, promptPath), promptReference))
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

        if (objectTypeValueJsonObject.size() == 0) {
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

    private JsonObject promptsToJsonObject(final List<Prompt> prompts, final String defendantJsonObjectString, final ReusableInformationConverterType reusableInformationConverterType) {

        final JsonObjectBuilder objectTypeValueJsonObjectBuilder = createObjectBuilder();

        prompts.stream()
                .filter(prompt -> StringUtils.equalsIgnoreCase(reusableInformationConverterType.name(), prompt.getType()))
                .forEach(prompt -> {
                    final Optional<String> value = toTxtValue(defendantJsonObjectString, prompt.getCacheDataPath());
                    if (value.isPresent()) {
                        objectTypeValueJsonObjectBuilder.add(prompt.getReference(), value.get());
                    }

                });

        return objectTypeValueJsonObjectBuilder.build();
    }

    private JsonObject promptsToJsonObjectCacheDataPathList(final List<Prompt> prompts, final String defendantJsonObjectString, final ReusableInformationConverterType reusableInformationConverterType) {

        final JsonObjectBuilder objectTypeValueJsonObjectBuilder = createObjectBuilder();

        prompts.stream()
                .filter(prompt -> StringUtils.equalsIgnoreCase(reusableInformationConverterType.name(), prompt.getType()))
                .forEach(prompt -> {
                    final StringBuilder promptValue = new StringBuilder(StringUtils.EMPTY);
                    final List<String> cacheDataPathList = Arrays.asList(prompt.getCacheDataPath().split(PATH_SPLITTER));
                    cacheDataPathList.forEach(promptPath -> {
                        final Optional<String> value = toTxtValue(defendantJsonObjectString, promptPath.trim());
                        if (value.isPresent() && StringUtils.EMPTY.contentEquals(promptValue)) {
                            promptValue.append(StringUtils.SPACE).append(value.get());
                            objectTypeValueJsonObjectBuilder.add(prompt.getReference(), promptValue.toString().trim());
                        }
                    });
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

    @SuppressWarnings("squid:S1166")
    private Optional<String> toTxtValue(final String defendantJsonObjectString, final String promptPath) {
        try {
            final Object objectValue = JsonPath.read(defendantJsonObjectString, DELIMITER + promptPath);
            if (objectValue instanceof Collection<?>) {
                final List<String> values = JsonPath.read(defendantJsonObjectString, DELIMITER + promptPath);
                return values.stream().findAny();
            } else {
                return Optional.of(String.valueOf(objectValue));
            }
        } catch (InvalidPathException e) {
            LOGGER.debug("Cannot find path in defendantJson. Exception: {}", e.getMessage());
            return Optional.empty();
        }
    }

}
