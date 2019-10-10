package uk.gov.moj.cpp.hearing.domain.transformation.mot.transform;

import javax.json.JsonObject;
import java.util.Map;

public interface EventInstance {

    JsonObject transform(final JsonObject jsonObject);

    default JsonObject transform(final JsonObject jsonObject, final Map<String, String> valueMap){
        return transform(jsonObject);
    }

}
