package uk.gov.moj.cpp.hearing.query.view.convertor;

import static java.util.Optional.ofNullable;
import static javax.json.Json.createObjectBuilder;

import uk.gov.moj.cpp.hearing.common.ReusableInformation;
import uk.gov.moj.cpp.hearing.common.ReusableInformationConverterType;

import javax.json.JsonObjectBuilder;

public abstract class ReusableInformationBaseConverter<T> {

    protected static final String TYPE_LABEL = "type";
    protected static final String VALUE_LABEL = "value";
    protected static final String CACHE_DATA_PATH = "cacheDataPath";
    protected static final String CACHEABLE = "cacheable";
    protected ReusableInformationConverterType type;

    private static final String PROMPT_REF = "promptRef";
    private static final String MASTER_DEFENDANT_ID = "masterDefendantId";
    private static final String CASE_ID = "caseId";
    private static final String APPLICATION_ID = "applicationId";

    protected JsonObjectBuilder convert(final ReusableInformation<T> reusableInformation) {
        final JsonObjectBuilder builder = createObjectBuilder()
                .add(PROMPT_REF, reusableInformation.getPromptRef())
                .add(TYPE_LABEL, type.name())
                .add(CACHE_DATA_PATH, reusableInformation.getCacheDataPath())
                .add(CACHEABLE, reusableInformation.getCacheable());
        ofNullable(reusableInformation.getMasterDefendantId()).ifPresent(id -> builder.add(MASTER_DEFENDANT_ID, id.toString()));
        ofNullable(reusableInformation.getCaseId()).ifPresent(id -> builder.add(CASE_ID, id.toString()));
        ofNullable(reusableInformation.getApplicationId()).ifPresent(id -> builder.add(APPLICATION_ID, id.toString()));

        return builder;
    }
}
