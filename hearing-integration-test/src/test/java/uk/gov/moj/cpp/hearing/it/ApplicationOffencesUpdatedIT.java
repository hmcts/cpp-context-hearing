package uk.gov.moj.cpp.hearing.it;

import io.restassured.path.json.JsonPath;
import java.time.LocalDate;
import javax.json.JsonObject;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Spy;
import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.core.courts.CourtApplicationCase;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.LaaReference;
import uk.gov.justice.core.courts.Offence;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.integrationtest.utils.jms.JmsMessageConsumerClient;
import uk.gov.justice.services.integrationtest.utils.jms.JmsMessageConsumerClientProvider;
import uk.gov.justice.services.test.utils.framework.api.JsonObjectConvertersFactory;
import uk.gov.moj.cpp.hearing.command.initiate.ExtendHearingCommand;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.HearingDetailsResponse;
import uk.gov.moj.cpp.hearing.test.CommandHelpers;
import uk.gov.moj.cpp.hearing.test.HearingFactory;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataOf;
import static uk.gov.moj.cpp.hearing.it.Queries.getHearingPollForMatch;
import static uk.gov.moj.cpp.hearing.it.UseCases.initiateHearing;
import static uk.gov.moj.cpp.hearing.it.UseCases.sendPublicApplicationOffencesUpdatedMessage;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.minimumInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.test.matchers.ElementAtListMatcher.first;
import static uk.gov.moj.cpp.hearing.utils.QueueUtil.getPublicTopicInstance;
import static uk.gov.moj.cpp.hearing.utils.QueueUtil.retrieveMessageAsJsonPath;
import static uk.gov.moj.cpp.hearing.utils.QueueUtil.sendMessage;
import static uk.gov.moj.cpp.hearing.utils.RestUtils.DEFAULT_POLL_TIMEOUT_IN_SEC;

public class ApplicationOffencesUpdatedIT extends AbstractIT {
    private static final String PROGRESSION_EVENTS_HEARING_EXTENDED = "public.progression.events.hearing-extended";

    private JmsMessageConsumerClient consumerForLinkProsecutionCasesToHearing;

    @Spy
    private final ObjectToJsonObjectConverter objectToJsonObjectConverter = new JsonObjectConvertersFactory().objectToJsonObjectConverter();

    @BeforeEach
    public void setUp() {

        consumerForLinkProsecutionCasesToHearing = JmsMessageConsumerClientProvider.newPrivateJmsMessageConsumerClientProvider(CONTEXT_NAME).withEventNames("hearing.events.application-detail-changed").getMessageConsumerClient();

    }
    @Test
    public void updateApplicationDetails_WhenProgressionNotifiesUpdates() throws Exception {

        //Given Application already Exist
        final ExtendHearingCommand extendHearingCommand = addApplication();
        final CourtApplication existingCourtApplication = extendHearingCommand.getCourtApplication();
        final LaaReference laaReference = LaaReference.laaReference().withStatusId(randomUUID()).withApplicationReference("applicationReference")
                .withStatusCode("statusCode").withStatusDescription("description").withStatusDate(LocalDate.now()).build();
        //when progression announced application change

        sendPublicApplicationOffencesUpdatedMessage(objectToJsonObjectConverter.convert(laaReference), existingCourtApplication.getId().toString(), existingCourtApplication.getSubject().getId().toString(), existingCourtApplication.getCourtApplicationCases().get(0).getOffences().get(0).getId().toString());

        //then application should  updated
        Queries.getHearingPollForMatch(extendHearingCommand.getHearingId(), DEFAULT_POLL_TIMEOUT_IN_SEC, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(extendHearingCommand.getHearingId()))
                        .withValue(h -> h.getCourtApplications().size(), 2)
                        .with(Hearing::getCourtApplications, hasItem(isBean(CourtApplication.class)
                                .withValue(CourtApplication::getId, existingCourtApplication.getId())
                                .withValue(CourtApplication::getApplicationReference, existingCourtApplication.getApplicationReference())
                                .with(CourtApplication::getCourtApplicationCases, first(isBean(CourtApplicationCase.class)
                                        .with(CourtApplicationCase::getOffences, first(isBean(Offence.class)
                                                .with(Offence::getLaaApplnReference, isBean(LaaReference.class)
                                                        .withValue(LaaReference::getApplicationReference, laaReference.getApplicationReference())
                                                        .withValue(LaaReference::getStatusCode, laaReference.getStatusCode())
                                                        .withValue(LaaReference::getStatusDate, laaReference.getStatusDate())
                                                        .withValue(LaaReference::getStatusId, laaReference.getStatusId()))))
                                 ))
                )
        )));

    }

    private ExtendHearingCommand addApplication() throws Exception {

        final CommandHelpers.InitiateHearingCommandHelper hearingOne = h(initiateHearing(getRequestSpec(), minimumInitiateHearingTemplate()));

        final Hearing hearing = hearingOne.getHearing();
        final CourtApplication initialCourtApplication = hearing.getCourtApplications().get(0);

        getHearingPollForMatch(hearing.getId(), isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearing.getId()))
                        .with(Hearing::getCourtApplications, first(isBean(CourtApplication.class)
                                .withValue(CourtApplication::getId, initialCourtApplication.getId())
                                .withValue(CourtApplication::getApplicationReference, initialCourtApplication.getApplicationReference())
                        ))
                )
        );

        ExtendHearingCommand extendHearingCommand = new ExtendHearingCommand();
        extendHearingCommand.setHearingId(hearing.getId());
        final CourtApplication newCourtApplication = (new HearingFactory()).courtApplication().build();
        extendHearingCommand.setCourtApplication(newCourtApplication);

        JsonObject commandJson = Utilities.JsonUtil.objectToJsonObject(extendHearingCommand);

        sendMessage(getPublicTopicInstance().createProducer(),
                PROGRESSION_EVENTS_HEARING_EXTENDED,
                commandJson,
                metadataOf(randomUUID(), PROGRESSION_EVENTS_HEARING_EXTENDED)
                        .withUserId(randomUUID().toString())
                        .build()
        );

        getHearingPollForMatch(hearing.getId(), isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearing.getId()))
                        .withValue(h -> h.getCourtApplications().size(), 2)
                        .with(Hearing::getCourtApplications, hasItem(isBean(CourtApplication.class)
                                .withValue(CourtApplication::getId, extendHearingCommand.getCourtApplication().getId())
                                .withValue(CourtApplication::getApplicationReference, extendHearingCommand.getCourtApplication().getApplicationReference())
                        ))
                )
        );

        return extendHearingCommand;
    }
}