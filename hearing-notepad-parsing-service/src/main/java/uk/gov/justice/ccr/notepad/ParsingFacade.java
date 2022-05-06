package uk.gov.justice.ccr.notepad;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.justice.ccr.notepad.process.ChildResultDefinitionDetail;
import uk.gov.justice.ccr.notepad.process.Knowledge;
import uk.gov.justice.ccr.notepad.process.Processor;
import uk.gov.justice.ccr.notepad.result.cache.model.ResultDefinition;
import uk.gov.justice.ccr.notepad.view.Part;
import uk.gov.justice.services.common.configuration.Value;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.cache.CacheDomain;
import uk.gov.moj.cpp.hearing.cache.service.CacheService;

import javax.inject.Inject;
import javax.json.JsonObject;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class ParsingFacade {

    private static final Logger LOGGER = LoggerFactory.getLogger(ParsingFacade.class);

    @Inject
    Processor processor;

    @Inject
    private CacheService cacheService;

    @Inject
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @Inject
    private StringToJsonObjectConverter stringToJsonObjectConverter;

    @Inject
    JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Inject
    @Value(key = "redisCommonCacheEnabled", defaultValue = "false")
    private String redisCommonCacheEnabled;
    
    @Inject
    @Value(key = "redisContextHearingCacheEnabled", defaultValue = "false")
    private String redisContextHearingCacheEnabled;

    public Knowledge processParts(final List<Part> parts, final LocalDate orderedDate) {
        List<String> values = parts.stream().map(Part::getValueAsString).collect(toList());
        return processor.processParts(values, orderedDate);
    }

    public Knowledge processPrompt(final String resultDefinitionId, final LocalDate orderedDate) {
        return processor.processResultPrompt(resultDefinitionId, orderedDate);
    }


    public ChildResultDefinitionDetail retrieveChildResultDefinitionDetail(final String resultDefinitionId, final LocalDate orderedDate) {
        if (Boolean.parseBoolean(redisCommonCacheEnabled) && (Boolean.parseBoolean(redisContextHearingCacheEnabled))) {
            return retrieveChildResultDefinitionDetailUsingRedisCache(resultDefinitionId, orderedDate);
        }
        return processor.retrieveChildResultDefinitionDetail(resultDefinitionId, orderedDate);
    }

    public void lazyLoad(final JsonEnvelope envelope, final LocalDate orderedDate) {
        processor.lazyLoad(envelope, orderedDate);
    }

    public ResultDefinition retrieveResultDefinitionById(final String resultDefinitionId, final LocalDate orderedDate) {
        if (Boolean.parseBoolean(redisCommonCacheEnabled) && (Boolean.parseBoolean(redisContextHearingCacheEnabled))) {
            return retrieveResultDefinitionByIdUsingRedisCache(resultDefinitionId, orderedDate);
        }
        return processor.retrieveResultDefinitionById(resultDefinitionId, orderedDate);
    }

    private ChildResultDefinitionDetail retrieveChildResultDefinitionDetailUsingRedisCache(final String resultDefinitionId, final LocalDate orderedDate) {
        final String cacheKey = redisCacheKey(CacheDomain.CHILD_RESULT_DEFINITION_ID, resultDefinitionId, orderedDate);
        String cacheValue = cacheService.get(cacheKey);

        LOGGER.info("Redis CacheValue For ChildResultDefinition_OrderedDate Key:{}, Value:{} ", cacheKey, cacheValue);

        ChildResultDefinitionDetail childResultDefinitionDetail;

        if (cacheValue == null) {

            childResultDefinitionDetail = processor.retrieveChildResultDefinitionDetail(resultDefinitionId, orderedDate);
            LOGGER.info("childResultDefinitionDetail:{}", childResultDefinitionDetail);

            if (childResultDefinitionDetail != null) {
                JsonObject convert = objectToJsonObjectConverter.convert(childResultDefinitionDetail);

                cacheValue = convert.toString();
                LOGGER.info("Adding Redis CacheValue For ChildResultDefinition_OrderedDate Key:{}, Value:{}", cacheKey, cacheValue);

                cacheService.add(cacheKey, cacheValue);
            }
        } else {
            JsonObject convert = stringToJsonObjectConverter.convert(cacheValue);
            childResultDefinitionDetail = jsonObjectToObjectConverter.convert(convert, ChildResultDefinitionDetail.class);
            LOGGER.info("Converted childResultDefinitionDetail:{}", childResultDefinitionDetail);
        }

        return childResultDefinitionDetail;
    }

    private ResultDefinition retrieveResultDefinitionByIdUsingRedisCache(final String resultDefinitionId, final LocalDate orderedDate) {
        final String cacheKey = redisCacheKey(CacheDomain.RESULT_DEFINITION_ID, resultDefinitionId, orderedDate);
        String cacheValue = cacheService.get(cacheKey);

        LOGGER.info("Redis CacheValue For ResultDefinition_OrderedDate Key:{}, Value:{} ", cacheKey, cacheValue);

        ResultDefinition resultDefinition;

        if (cacheValue == null) {
            resultDefinition = processor.retrieveResultDefinitionById(resultDefinitionId, orderedDate);
            if (resultDefinition != null) {
                JsonObject convert = objectToJsonObjectConverter.convert(resultDefinition);
                cacheValue = convert.toString();
                LOGGER.info("Adding Redis CacheValue For ResultDefinition_OrderedDate Key:{}, Value:{}", cacheKey, cacheValue);
                cacheService.add(cacheKey, cacheValue);
            }
        } else {
            JsonObject convert = stringToJsonObjectConverter.convert(cacheValue);
            resultDefinition = jsonObjectToObjectConverter.convert(convert, ResultDefinition.class);
            LOGGER.info("Converted resultDefinition:{}", resultDefinition);
        }

        return resultDefinition;
    }

    protected String redisCacheKey(final CacheDomain cacheDomain, final String resultDefinitionId, final LocalDate orderedDate) {
        return cacheDomain.name() + '_' + resultDefinitionId + '_' + orderedDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }
}
