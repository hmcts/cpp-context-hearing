package uk.gov.moj.cpp.hearing.query.view.convertor;

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


    protected JsonObjectBuilder convert(final ReusableInformation<T> reusableInformation) {

        return createObjectBuilder()
                .add(PROMPT_REF, reusableInformation.getPromptRef())
                .add(MASTER_DEFENDANT_ID, reusableInformation.getMasterDefendantId().toString())
                .add(TYPE_LABEL, type.name())
                .add(CACHE_DATA_PATH, reusableInformation.getCacheDataPath())
                .add(CACHEABLE, reusableInformation.getCacheable());

    }
}
