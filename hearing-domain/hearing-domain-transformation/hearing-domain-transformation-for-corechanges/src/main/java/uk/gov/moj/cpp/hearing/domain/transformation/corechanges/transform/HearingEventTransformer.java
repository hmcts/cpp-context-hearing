package uk.gov.moj.cpp.hearing.domain.transformation.corechanges.transform;

import uk.gov.justice.services.messaging.Metadata;

import javax.json.JsonObject;

public interface HearingEventTransformer {
    JsonObject transform(Metadata eventMetadata, JsonObject payload);
}
