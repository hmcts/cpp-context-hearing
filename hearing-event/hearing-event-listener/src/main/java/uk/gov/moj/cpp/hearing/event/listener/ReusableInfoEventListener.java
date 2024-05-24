package uk.gov.moj.cpp.hearing.event.listener;

import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toSet;
import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.moj.cpp.hearing.command.ReusableInfo;
import uk.gov.moj.cpp.hearing.command.ReusableInfoResults;
import uk.gov.moj.cpp.hearing.domain.event.ReusableInfoSaved;
import uk.gov.moj.cpp.hearing.repository.ReusableInfoRepository;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@ServiceComponent(EVENT_LISTENER)
public class ReusableInfoEventListener {

    public static final String PROMPT_REF = "promptRef";
    public static final String TYPE = "type";
    public static final String MASTER_DEFENDANT_ID = "masterDefendantId";
    public static final String VALUE = "value";
    public static final String OFFENCE_ID = "offenceId";
    public static final String CACHEABLE = "cacheable";
    public static final String CACHE_DATA_PATH = "cacheDataPath";
    public static final String SHORT_CODE = "shortCode";
    private final ObjectMapper mapper = new ObjectMapperProducer().objectMapper();

    @Inject
    private ReusableInfoRepository reusableInfoRepository;

    @Handles("hearing.event.reusable-info-saved")
    public void saveReusableInfo(final Envelope<ReusableInfoSaved> event) {
        final ReusableInfoSaved reusableInfoSaved = event.payload();


        final Set<UUID> defendantIds = reusableInfoSaved.getPromptList().stream().map(ReusableInfo::getMasterDefendantId).collect(toSet());
        defendantIds.addAll(reusableInfoSaved.getResultsList().stream().map(ReusableInfoResults::getMasterDefendantId).collect(toSet()));

        final List<uk.gov.moj.cpp.hearing.persist.entity.ha.ReusableInfo> existingReusableInfos = reusableInfoRepository.findReusableInfoByMasterDefendantIds(new ArrayList<>(defendantIds));
        for (final UUID defendantId : defendantIds) {

            final List<ReusableInfo> existingReusablePromptsForDefendant = getExistingReusablePrompts(defendantId, existingReusableInfos);
            final List<ReusableInfoResults> existingReusableResultsForDefendant = getExistingReusableResults(defendantId, existingReusableInfos);

            final JsonObjectBuilder cacheBuilder = createObjectBuilder();
            final JsonArrayBuilder newReusablePromptsBuilder = createArrayBuilder();
            final JsonArrayBuilder newReusableResultsBuilder = createArrayBuilder();

            if(isNotEmpty(reusableInfoSaved.getPromptList())){
                populateReusablePrompts(reusableInfoSaved, defendantId, newReusablePromptsBuilder, existingReusablePromptsForDefendant);
                cacheBuilder.add("reusablePrompts", newReusablePromptsBuilder.build());
            }

            if(isNotEmpty(reusableInfoSaved.getResultsList())){
                populateReusableResults(reusableInfoSaved, defendantId, newReusableResultsBuilder, existingReusableResultsForDefendant);
                cacheBuilder.add("reusableResults", newReusableResultsBuilder.build());
            }

            final uk.gov.moj.cpp.hearing.persist.entity.ha.ReusableInfo reusableInfo = new uk.gov.moj.cpp.hearing.persist.entity.ha.ReusableInfo(defendantId, mapper.valueToTree(cacheBuilder.build()), ZonedDateTime.now());
            reusableInfoRepository.save(reusableInfo);
        }
    }

    private  List<ReusableInfo> getExistingReusablePrompts(final UUID defendantId, final List<uk.gov.moj.cpp.hearing.persist.entity.ha.ReusableInfo> existingReusableInfos) {
        final Optional<uk.gov.moj.cpp.hearing.persist.entity.ha.ReusableInfo> reusableInfo = existingReusableInfos.stream().filter(re -> re.getId().equals(defendantId)).findFirst();
        final List<JsonNode> list = new ArrayList();
        reusableInfo.ifPresent(re -> {
            final JsonNode reusablePrompts = re.getPayload().get("reusablePrompts");
            if (nonNull(reusablePrompts) && reusablePrompts.isArray()) {
                reusablePrompts.forEach(list::add);
            }
        });

        return list.stream().map(e -> {
                            final ReusableInfo.Builder reusableInfoBuilder = ReusableInfo.builder()
                            .withPromptRef(e.get(PROMPT_REF).asText())
                            .withMasterDefendantId(UUID.fromString(e.get(MASTER_DEFENDANT_ID).asText()))
                            .withType(e.get(TYPE).asText())
                            .withValue(e.get(VALUE).asText())
                            .withOffenceId(UUID.fromString(e.get(OFFENCE_ID).asText()));
                            ofNullable(e.get(CACHEABLE)).ifPresent(cacheable -> reusableInfoBuilder.withCacheable(e.get(CACHEABLE).asInt()));
                            ofNullable(e.get(CACHE_DATA_PATH)).ifPresent(cacheable -> reusableInfoBuilder.withCacheDataPath(e.get(CACHE_DATA_PATH).asText()));
                            return reusableInfoBuilder.build();
                }
        ).collect(Collectors.toList());

    }

    private  List<ReusableInfoResults> getExistingReusableResults(final UUID defendantId, final List<uk.gov.moj.cpp.hearing.persist.entity.ha.ReusableInfo> existingReusableInfos) {
        final Optional<uk.gov.moj.cpp.hearing.persist.entity.ha.ReusableInfo> reusableInfo = existingReusableInfos.stream().filter(re -> re.getId().equals(defendantId)).findFirst();
        final List<JsonNode> list = new ArrayList();
        reusableInfo.ifPresent(re -> {
            final JsonNode reusableResults = re.getPayload().get("reusableResults");
            if (nonNull(reusableResults) && reusableResults.isArray()) {
                reusableResults.forEach(list::add);
            }
        });

        return list.stream().map(e -> {
                    final ReusableInfoResults.Builder reusableInfoBuilder = ReusableInfoResults.builder()
                            .withMasterDefendantId(UUID.fromString(e.get(MASTER_DEFENDANT_ID).asText()))
                            .withShortCode(e.get(SHORT_CODE).asText())
                            .withValue(e.get(VALUE).asText())
                            .withOffenceId(UUID.fromString(e.get(OFFENCE_ID).asText()));
                    return reusableInfoBuilder.build();
                }
        ).collect(Collectors.toList());

    }

    private void populateReusableResults(final ReusableInfoSaved reusableInfoSaved, final UUID defendantId, final JsonArrayBuilder resultsArrBuilder, final List<ReusableInfoResults> existingReusableResultsForDefendant) {
        final List<UUID> inputOffenceListForDefendant  = new ArrayList();
        reusableInfoSaved.getResultsList().stream()
                .filter(resultsCache -> defendantId.equals(resultsCache.getMasterDefendantId()))
                .map(resultsCache -> {
                    final JsonObjectBuilder resultBuilder = createObjectBuilder()
                            .add(MASTER_DEFENDANT_ID, resultsCache.getMasterDefendantId().toString())
                            .add(OFFENCE_ID, resultsCache.getOffenceId().toString())
                            .add(SHORT_CODE,resultsCache.getShortCode())
                            .add(VALUE, resultsCache.getValue());
                    inputOffenceListForDefendant.add(resultsCache.getOffenceId());
                    return resultBuilder;
                })
                .forEach(resultsArrBuilder::add);
        existingReusableResultsForDefendant.stream()
                .filter(e -> !inputOffenceListForDefendant.contains(e.getOffenceId()))
                .forEach(re-> {
                    final JsonObjectBuilder resultBuilder = createObjectBuilder()
                            .add(MASTER_DEFENDANT_ID, re.getMasterDefendantId().toString())
                            .add(OFFENCE_ID, re.getOffenceId().toString())
                            .add(SHORT_CODE, re.getShortCode())
                            .add(VALUE, re.getValue());

                    resultsArrBuilder.add(resultBuilder);
                });
    }

    private void populateReusablePrompts(final ReusableInfoSaved reusableInfoSaved, final UUID defendantId, final JsonArrayBuilder promptsArrBuilder, final List<ReusableInfo> existingReusablePromptsForDefendant) {
        final List<UUID> inputOffenceListForDefendant  = new ArrayList();
        reusableInfoSaved.getPromptList().stream()
                .filter(prompt -> defendantId.equals(prompt.getMasterDefendantId()))
                .map(prompt -> {
                    final JsonObjectBuilder promptBuilder = createObjectBuilder()
                            .add(PROMPT_REF, prompt.getPromptRef())
                            .add(MASTER_DEFENDANT_ID, prompt.getMasterDefendantId().toString())
                            .add(OFFENCE_ID, prompt.getOffenceId().toString())
                            .add(TYPE, prompt.getType())
                            .add(VALUE, prompt.getValue().toString());
                    ofNullable(prompt.getCacheable()).ifPresent(cacheable -> promptBuilder.add(CACHEABLE, cacheable));
                    ofNullable(prompt.getCacheDataPath()).ifPresent(cacheDataPath -> promptBuilder.add(CACHE_DATA_PATH, cacheDataPath));
                    inputOffenceListForDefendant.add(prompt.getOffenceId());
                    return promptBuilder;
                })
                .forEach(promptsArrBuilder::add);

        existingReusablePromptsForDefendant.stream()
                .filter(e -> !inputOffenceListForDefendant.contains(e.getOffenceId()))
                .forEach(re-> {
                        final JsonObjectBuilder promptBuilder = createObjectBuilder()
                        .add(PROMPT_REF, re.getPromptRef())
                        .add(MASTER_DEFENDANT_ID, re.getMasterDefendantId().toString())
                        .add(OFFENCE_ID, re.getOffenceId().toString())
                        .add(TYPE, re.getType())
                        .add(VALUE, re.getValue().toString());
                        ofNullable(re.getCacheable()).ifPresent(cacheable -> promptBuilder.add(CACHEABLE, cacheable));
                        ofNullable(re.getCacheDataPath()).ifPresent(cacheDataPath -> promptBuilder.add(CACHE_DATA_PATH, cacheDataPath));
                        promptsArrBuilder.add(promptBuilder);
                });
    }
}
