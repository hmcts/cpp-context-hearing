package uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.transform;

import javax.json.JsonObject;

public interface EventInstance {

    JsonObject transform(final JsonObject jsonObject);
}
