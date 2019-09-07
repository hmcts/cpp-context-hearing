package uk.gov.moj.cpp.hearing.domain.transformation.mot.transform;

import uk.gov.justice.core.courts.ProsecutionCase;

import javax.json.JsonObject;
import java.util.Map;

public interface EventInstance {

    JsonObject transform(final JsonObject jsonObject, Map<String, ProsecutionCase> hearingMap);

}
