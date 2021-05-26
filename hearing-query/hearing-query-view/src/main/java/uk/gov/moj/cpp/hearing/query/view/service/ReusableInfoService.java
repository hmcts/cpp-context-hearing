package uk.gov.moj.cpp.hearing.query.view.service;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt;
import uk.gov.moj.cpp.hearing.persist.entity.ha.ReusableInfo;
import uk.gov.moj.cpp.hearing.query.view.convertor.ReusableInformationMainConverter;
import uk.gov.moj.cpp.hearing.repository.ReusableInfoRepository;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReusableInfoService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReusableInfoService.class);

    public static final String REUSABLE_PROMPTS = "reusablePrompts";
    public static final String REUSABLE_RESULTS = "reusableResults";
    public static final String NATIONALITY = "nationality";

    @Inject
    private ObjectMapper mapper;

    @Inject
    private ReusableInfoRepository reusableInfoRepository;

    @Inject
    private ReusableInformationMainConverter reusableInformationMainConverter;

    public List<JsonObject> getCaseDetailReusableInformation(final Collection<ProsecutionCase> cases, final List<Prompt> resultPrompts, final Map<String, String> countryCodesMap) {
        final Map<String, Map<String, String>> customPromptValues = new HashMap<>();
        customPromptValues.put(NATIONALITY, countryCodesMap);

        final Map<UUID, Defendant> defendants = cases.stream()
                .flatMap(prosecutionCase -> prosecutionCase.getDefendants().stream())
                .collect(toMap(Defendant::getMasterDefendantId, defendant -> defendant, (defendant1, defendant2) -> defendant1));

        final Map<Defendant, List<JsonObject>> reusableInfoMapForDefendant = reusableInformationMainConverter
                .convertDefendant(defendants.values(), resultPrompts.stream()
                        .filter(prompt -> isNotBlank(prompt.getCacheDataPath())).collect(toList()), customPromptValues);

        final Map<ProsecutionCase, List<JsonObject>> reusableInfoMapForCase = reusableInformationMainConverter
                .convertCase(cases, resultPrompts.stream()
                        .filter(prompt -> isNotBlank(prompt.getCacheDataPath())).collect(toList()), customPromptValues);

        final List<JsonObject> defendantList = reusableInfoMapForDefendant.values().stream().flatMap(List::stream).collect(toList());
        final List<JsonObject> caseList = reusableInfoMapForCase.values().stream().flatMap(List::stream).collect(toList());
        defendantList.addAll(caseList);

        return defendantList;
    }

    public JsonObject getViewStoreReusableInformation(final Collection<Defendant> defendants, final List<JsonObject> reusableCaseDetailPrompts) {
        final List<ReusableInfo> reusableInfoList = getReusableInfoForDefendants(defendants);
        final JsonObjectBuilder builder = createObjectBuilder();
        final JsonArrayBuilder reusablePromptsArray = createArrayBuilder();
        final JsonArrayBuilder reusableResultsArray = createArrayBuilder();
        reusableCaseDetailPrompts.stream().forEach(reusablePromptsArray::add);

        reusableInfoList.stream().forEach(reusableInfo -> {
            try {
                final JsonObject payloadasJson = mapper.treeToValue(reusableInfo.getPayload(), JsonObject.class);
                if (payloadasJson.containsKey(REUSABLE_PROMPTS)) {
                    payloadasJson.getJsonArray(REUSABLE_PROMPTS).getValuesAs(JsonObject.class).stream().forEach(reusablePromptsArray::add);
                }
                if (payloadasJson.containsKey(REUSABLE_RESULTS)) {
                    payloadasJson.getJsonArray(REUSABLE_RESULTS).getValuesAs(JsonObject.class).stream().forEach(reusableResultsArray::add);
                }
            } catch (final JsonProcessingException e) {
                LOGGER.error(String.format("Json Parsing exception when parsing %s", reusableInfo.getPayload()), e);
            }
        });

        builder.add(REUSABLE_PROMPTS, reusablePromptsArray.build());
        builder.add(REUSABLE_RESULTS, reusableResultsArray.build());

        return builder.build();
    }

    public List<ReusableInfo> getReusableInfoForDefendants(final Collection<Defendant> defendants) {
        final List<UUID> masterDefendantIds = defendants.stream()
                .map(Defendant::getMasterDefendantId)
                .collect(toList());

        if (masterDefendantIds.isEmpty()) {
            return emptyList();
        }
        return reusableInfoRepository.findReusableInfoByMasterDefendantIds(masterDefendantIds);
    }
}
