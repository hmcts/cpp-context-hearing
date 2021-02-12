package uk.gov.moj.cpp.hearing.event.listener;

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
import java.util.Set;
import java.util.UUID;

import javax.inject.Inject;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

@ServiceComponent(EVENT_LISTENER)
public class ReusableInfoEventListener {

    private final ObjectMapper mapper = new ObjectMapperProducer().objectMapper();

    @Inject
    private ReusableInfoRepository reusableInfoRepository;

    @Handles("hearing.event.reusable-info-saved")
    public void saveReusableInfo(final Envelope<ReusableInfoSaved> event) {
        final ReusableInfoSaved reusableInfoSaved = event.payload();

        final Set<UUID> defendantIds = reusableInfoSaved.getPromptList().stream().map(ReusableInfo::getMasterDefendantId).collect(toSet());
        defendantIds.addAll(reusableInfoSaved.getResultsList().stream().map(ReusableInfoResults::getMasterDefendantId).collect(toSet()));

        for (final UUID defendantId : defendantIds) {
            final JsonObjectBuilder cacheBuilder = createObjectBuilder();
            final JsonArrayBuilder reusablePromptsBuilder = createArrayBuilder();
            final JsonArrayBuilder reusableResultsBuilder = createArrayBuilder();

            if(isNotEmpty(reusableInfoSaved.getPromptList())){
                populateReusablePrompts(reusableInfoSaved, defendantId, reusablePromptsBuilder);
                cacheBuilder.add("reusablePrompts", reusablePromptsBuilder.build());
            }

            if(isNotEmpty(reusableInfoSaved.getResultsList())){
                populateReusableResults(reusableInfoSaved, defendantId, reusableResultsBuilder);
                cacheBuilder.add("reusableResults", reusableResultsBuilder.build());
            }

            final uk.gov.moj.cpp.hearing.persist.entity.ha.ReusableInfo reusableInfo = new uk.gov.moj.cpp.hearing.persist.entity.ha.ReusableInfo(defendantId, mapper.valueToTree(cacheBuilder.build()), ZonedDateTime.now());
            reusableInfoRepository.save(reusableInfo);
        }
    }

    private void populateReusableResults(final ReusableInfoSaved reusableInfoSaved, final UUID defendantId, final JsonArrayBuilder resultsArrBuilder) {
        reusableInfoSaved.getResultsList().stream()
                .filter(resultsCache -> defendantId.equals(resultsCache.getMasterDefendantId()))
                .map(resultsCache -> createObjectBuilder()
                        .add("masterDefendantId", resultsCache.getMasterDefendantId().toString())
                        .add("offenceId", resultsCache.getOffenceId().toString())
                        .add("shortCode",resultsCache.getShortCode())
                        .add("value", resultsCache.getValue()))
                .forEach(resultsArrBuilder::add);
    }

    private void populateReusablePrompts(final ReusableInfoSaved reusableInfoSaved, final UUID defendantId, final JsonArrayBuilder promptsArrBuilder) {
        reusableInfoSaved.getPromptList().stream()
                .filter(prompt -> defendantId.equals(prompt.getMasterDefendantId()))
                .map(prompt -> {
                    final JsonObjectBuilder promptBuilder = createObjectBuilder()
                            .add("promptRef", prompt.getPromptRef())
                            .add("masterDefendantId", prompt.getMasterDefendantId().toString())
                            .add("offenceId", prompt.getOffenceId().toString())
                            .add("type", prompt.getType())
                            .add("value", prompt.getValue().toString());
                    ofNullable(prompt.getCacheable()).ifPresent(cacheable -> promptBuilder.add("cacheable", cacheable));
                    ofNullable(prompt.getCacheDataPath()).ifPresent(cacheDataPath -> promptBuilder.add("cacheDataPath", cacheDataPath));
                    return promptBuilder;
                })
                .forEach(promptsArrBuilder::add);
    }
}
