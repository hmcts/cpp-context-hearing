package uk.gov.moj.cpp.hearing.query.api;

import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.inject.Inject;

@ServiceComponent(Component.QUERY_API)
public class HearingQueryApi {

    @Inject
    private Requester requester;

    @Handles("hearing.get.hearings-by-startdate.v2")
    public JsonEnvelope findHearingsByStartDateV2(final JsonEnvelope query) {
        return this.requester.request(query);
    }

    @Handles("hearing.get.hearing.v2")
    public JsonEnvelope findHearingV2(final JsonEnvelope query) {
        return this.requester.request(query);
    }

    @Handles("hearing.get-hearing-event-definitions")
    public JsonEnvelope getHearingEventDefinitions(final JsonEnvelope query) {
        return this.requester.request(query);
    }

    @Handles("hearing.get-hearing-event-definitions.v2")
    public JsonEnvelope getHearingEventDefinitionsVersionTwo(final JsonEnvelope query) {
        return this.requester.request(query);
    }

    @Handles("hearing.get-hearing-event-definition")
    public JsonEnvelope getHearingEventDefinition(final JsonEnvelope query) {
        return this.requester.request(query);
    }

    @Handles("hearing.get-hearing-event-log")
    public JsonEnvelope getHearingEventLog(final JsonEnvelope query) {
        return this.requester.request(query);
    }

    @Handles("hearing.get-draft-result")
    public JsonEnvelope getDraftResult(final JsonEnvelope query) {
        return this.requester.request(query);
    }

}
