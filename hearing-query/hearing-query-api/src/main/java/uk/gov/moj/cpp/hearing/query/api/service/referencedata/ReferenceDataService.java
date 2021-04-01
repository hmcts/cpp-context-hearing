package uk.gov.moj.cpp.hearing.query.api.service.referencedata;

import static com.google.common.cache.CacheBuilder.newBuilder;
import static java.time.LocalDate.now;
import static java.util.Collections.emptyList;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createObjectBuilder;
import static uk.gov.justice.services.common.converter.LocalDates.from;
import static uk.gov.justice.services.core.annotation.Component.QUERY_API;
import static uk.gov.justice.services.core.enveloper.Enveloper.envelop;
import static uk.gov.justice.services.messaging.Envelope.metadataBuilder;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.JsonObjects.getString;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.moj.cpp.external.domain.referencedata.XhibitEventMappingsList;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.CrackedIneffectiveVacatedTrialTypes;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinition;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"squid:S3398"})
public class ReferenceDataService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReferenceDataService.class);
    private static final String GET_ALL_CRACKED_INEFFECTIVE_TRIAL_TYPES = "referencedata.query.cracked-ineffective-vacated-trial-types";
    private static final String XHIBIT_EVENT_MAPPINGS = "referencedata.query.cp-xhibit-hearing-event-mappings";
    private static final String REFERENCEDATA_QUERY_COURT_CENTRES = "referencedata.query.courtrooms";
    private static final String REFERENCEDATA_GET_ALL_RESULT_DEFINITIONS = "referencedata.get-all-result-definitions";
    private static final String RESULT_DEFINITIONS = "resultDefinitions";
    private static final String COUNTRY_NATIONALITIES = "countryNationality";
    private static final String REFERENCEDATA_GET_ALL_COUNTRY_NATIONALITIES = "referencedata.query.country-nationality";
    private static final String ISO_CODE = "isoCode";
    private static final String COUNTRY_NAME = "countryName";
    private static final int EXPIRATION_TIME = 8;
    private static final int MAXIMUM_SIZE = 60;

    @Inject
    @ServiceComponent(QUERY_API)
    private Requester requester;

    @Inject
    private UtcClock utcClock;

    @Inject
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    Map<String, String> countryCodesMap = new ConcurrentHashMap<>();
    private final LoadingCache<LocalDate, List<Prompt>> promptsCacheLoader = newBuilder()
            .expireAfterAccess(EXPIRATION_TIME, TimeUnit.HOURS)
            .maximumSize(MAXIMUM_SIZE)
            .build(new CacheLoader<LocalDate, List<Prompt>>() {
                @Override
                public List<Prompt> load(final LocalDate localDate) {
                    return getResultPrompts(localDate);
                }
            });

    public CrackedIneffectiveVacatedTrialTypes listAllCrackedIneffectiveVacatedTrialTypes() {

        final JsonEnvelope requestEnvelope = envelopeFrom(
                metadataBuilder().
                        withId(randomUUID()).
                        withName(GET_ALL_CRACKED_INEFFECTIVE_TRIAL_TYPES),
                createObjectBuilder());

        final Envelope<JsonObject> jsonResultEnvelope = requester.requestAsAdmin(requestEnvelope, JsonObject.class);

        return jsonObjectToObjectConverter.convert(jsonResultEnvelope.payload(), CrackedIneffectiveVacatedTrialTypes.class);
    }

    public XhibitEventMappingsList listAllEventMappings() {
        final Metadata metadata = JsonEnvelope.metadataBuilder()
                .createdAt(utcClock.now())
                .withName(XHIBIT_EVENT_MAPPINGS)
                .withId(randomUUID())
                .build();

        final JsonEnvelope jsonEnvelope = envelopeFrom(metadata, createObjectBuilder().build());

        return requester.requestAsAdmin(jsonEnvelope, XhibitEventMappingsList.class).payload();
    }

    public JsonObject getAllCourtRooms(final JsonEnvelope eventEnvelope) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Attempting to get all court rooms...");
        }

        final JsonObject payload = createObjectBuilder()
                .build();

        final Envelope<JsonObject> requestEnvelope = envelop(payload)
                .withName(REFERENCEDATA_QUERY_COURT_CENTRES)
                .withMetadataFrom(eventEnvelope);

        return requester.requestAsAdmin(requestEnvelope, JsonObject.class).payload();
    }

    public List<Prompt> getCacheableResultPrompts(final Optional<String> orderedDate) {
        try {
            final LocalDate cacheKey = orderedDate.isPresent() ? from(orderedDate.get()) : now();
            return promptsCacheLoader.get(cacheKey);
        } catch (ExecutionException e) {
            LOGGER.error("getAllResultDefinitionsByDate reference data service not available", e);
            return emptyList();
        }
    }

    private List<Prompt> getResultPrompts(final LocalDate orderedDate) {
        final JsonObjectBuilder getResultDefinitions = createObjectBuilder()
                .add("cacheable", true)
                .add("on", orderedDate.toString());
        final JsonEnvelope requestEnvelope = envelopeFrom(metadataBuilder().
                withId(randomUUID()).
                withName(REFERENCEDATA_GET_ALL_RESULT_DEFINITIONS), getResultDefinitions.build());
        final Envelope<JsonObject> response = requester.requestAsAdmin(requestEnvelope, JsonObject.class);
        final JsonArray resultDefinitionsPayload = response.payload().getJsonArray(RESULT_DEFINITIONS);
        final Map<Object, Boolean> promptReferenceMap = new HashMap<>();

        return resultDefinitionsPayload.isEmpty() ? emptyList() : resultDefinitionsPayload.getValuesAs(JsonObject.class).stream()
                .map(resultDef -> jsonObjectToObjectConverter.convert(resultDef, ResultDefinition.class))
                .flatMap(resultDefinition -> resultDefinition.getPrompts().stream())
                .filter(isCacheablePromptAndReferenceNotAddedAlready(promptReferenceMap))
                .collect(Collectors.toList());
    }

    private Predicate<Prompt> isCacheablePromptAndReferenceNotAddedAlready(final Map<Object, Boolean> promptReferenceMap) {
        return prompt -> prompt.getCacheable() != null && prompt.getCacheable() > 0 && (promptReferenceMap.putIfAbsent(prompt.getReference(), Boolean.TRUE) == null);
    }

    public Map<String, String> getCountryCodesMap() {
        if (MapUtils.isNotEmpty(countryCodesMap)) {
            return countryCodesMap;
        }

        final JsonEnvelope requestEnvelope = envelopeFrom(metadataBuilder()
                .withId(randomUUID())
                .withName(REFERENCEDATA_GET_ALL_COUNTRY_NATIONALITIES), createObjectBuilder().build());

        final Envelope<JsonObject> response = requester.requestAsAdmin(requestEnvelope, JsonObject.class);
        final JsonArray countryNationalitiesPayload = response.payload().getJsonArray(COUNTRY_NATIONALITIES);
        if (countryNationalitiesPayload.isEmpty()) {
            return countryCodesMap;
        }

        countryNationalitiesPayload.getValuesAs(JsonObject.class).stream()
                .filter(countryNationality -> getString(countryNationality, ISO_CODE).isPresent() && getString(countryNationality, COUNTRY_NAME).isPresent())
                .forEach(countryNationality -> countryCodesMap.putIfAbsent(countryNationality.getString(ISO_CODE), countryNationality.getString(COUNTRY_NAME)));

        return countryCodesMap;
    }

}
