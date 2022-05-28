package uk.gov.moj.cpp.hearing.it;

import static java.util.UUID.randomUUID;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.Matchers.is;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataOf;
import static uk.gov.moj.cpp.hearing.it.Utilities.listenFor;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.minimumInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.test.matchers.MapStringToTypeMatcher.convertStringTo;
import static uk.gov.moj.cpp.hearing.utils.QueueUtil.getPublicTopicInstance;
import static uk.gov.moj.cpp.hearing.utils.QueueUtil.sendMessage;

import uk.gov.justice.core.courts.Hearing;
import uk.gov.moj.cpp.hearing.domain.event.HearingMarkedAsDuplicate;
import uk.gov.moj.cpp.hearing.domain.event.HearingDeleted;
import uk.gov.moj.cpp.hearing.test.CommandHelpers;

import javax.json.JsonObject;

import org.junit.Test;

public class StagingHmiIT extends AbstractIT {

    @Test
    public void shouldRemoveHearingWhenHmiRemovesCourtRoom() {
        final Hearing hearing = initiateHearing();

        final Utilities.EventListener privateEventListener = listenFor("hearing.events.hearing-deleted", "hearing.event")
                .withFilter(convertStringTo(HearingDeleted.class, isBean(HearingDeleted.class)
                        .with(HearingDeleted::getHearingId, is(hearing.getId()))));

        publishPublicEvent("public.staginghmi.hearing-updated-from-hmi",
                createObjectBuilder()
                        .add("hearingId", hearing.getId().toString())
                        .add("courtCentreId", randomUUID().toString())
                        .build());
        privateEventListener.waitFor();
    }

    @Test
    public void shouldRemoveHearingWhenHmiRemovesHearing() {
        final Hearing hearing = initiateHearing();

        final Utilities.EventListener privateEventListener = listenFor("hearing.events.marked-as-duplicate", "hearing.event")
                .withFilter(convertStringTo(HearingMarkedAsDuplicate.class, isBean(HearingMarkedAsDuplicate.class)
                        .with(HearingMarkedAsDuplicate::getHearingId, is(hearing.getId()))));

        publishPublicEvent("public.staginghmi.hearing-deleted-from-hmi",
                createObjectBuilder()
                        .add("hearingId", hearing.getId().toString())
                        .build());
        privateEventListener.waitFor();
    }

    private Hearing initiateHearing() {
        final CommandHelpers.InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(getRequestSpec(), minimumInitiateHearingTemplate()));
        return hearingOne.getHearing();
    }

    private void publishPublicEvent(final String publicEventName,
                                    final JsonObject publicEventPayload) {
        sendMessage(getPublicTopicInstance().createProducer(),
                publicEventName,
                publicEventPayload,
                metadataOf(randomUUID(), publicEventName)
                        .withUserId(randomUUID().toString())
                        .build()
        );
    }
}
