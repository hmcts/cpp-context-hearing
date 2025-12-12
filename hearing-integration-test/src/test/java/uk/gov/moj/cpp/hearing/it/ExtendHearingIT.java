package uk.gov.moj.cpp.hearing.it;

import static com.google.common.collect.ImmutableList.of;
import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataOf;
import static uk.gov.moj.cpp.hearing.it.Queries.getHearingPollForMatch;
import static uk.gov.moj.cpp.hearing.it.UseCases.initiateHearing;
import static uk.gov.moj.cpp.hearing.it.UseCases.initiateHearingWithoutBreachApplication;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.minimumInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.test.matchers.ElementAtListMatcher.first;
import static uk.gov.moj.cpp.hearing.utils.QueueUtil.getPublicTopicInstance;
import static uk.gov.moj.cpp.hearing.utils.QueueUtil.sendMessage;

import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.HearingDay;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.moj.cpp.hearing.command.initiate.ExtendHearingCommand;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.HearingDetailsResponse;
import uk.gov.moj.cpp.hearing.test.CommandHelpers.InitiateHearingCommandHelper;
import uk.gov.moj.cpp.hearing.test.HearingFactory;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import javax.json.Json;
import javax.json.JsonObject;

import org.junit.jupiter.api.Test;


@SuppressWarnings("squid:S2699")
public class ExtendHearingIT extends AbstractIT {

    private static final String PROGRESSION_EVENTS_HEARING_EXTENDED = "public.progression.events.hearing-extended";

    @Test
    public void insertCourtApplication() throws Exception {
        extend(true);
    }

    @Test
    public void amendCourtApplication() throws Exception {
        extend(false);
    }

    @Test
    public void addBreachCourtApplication() throws Exception {
        extendBreachApplication();
    }

    @Test
    public void insertProsecutionCases() throws Exception {

        final InitiateHearingCommandHelper hearingOne = h(initiateHearing(getRequestSpec(), minimumInitiateHearingTemplate()));

        final Hearing hearing = hearingOne.getHearing();
        getHearingPollForMatch(hearing.getId(), isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearing.getId()))
                )
        );

        ExtendHearingCommand extendHearingCommand = new ExtendHearingCommand();
        extendHearingCommand.setHearingId(hearing.getId());
        final UUID caseId = randomUUID();
        extendHearingCommand.setProsecutionCases(cloneCase(hearing, caseId));

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
                        .withValue(h -> h.getProsecutionCases().size(), 2)
                        .with(Hearing::getProsecutionCases, hasItem(isBean(ProsecutionCase.class)
                                .withValue(ProsecutionCase::getId, caseId)
                        ))
                )
        );

    }

    @Test
    public void insertProsecutionCasesForAdhocHearing() throws Exception {

        final InitiateHearingCommandHelper hearingOne = h(initiateHearing(getRequestSpec(), minimumInitiateHearingTemplate()));

        final Hearing hearing = hearingOne.getHearing();
        getHearingPollForMatch(hearing.getId(), isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearing.getId()))
                        .withValue(h -> h.getProsecutionCases().size(), 1)
                )
        );

        ExtendHearingCommand extendHearingCommand = new ExtendHearingCommand();
        extendHearingCommand.setHearingId(hearing.getId());
        final UUID caseId = randomUUID();
        extendHearingCommand.setProsecutionCases(cloneCase(hearing, caseId));

        JsonObject commandJson = Utilities.JsonUtil.objectToJsonObject(extendHearingCommand);

        sendMessage(getPublicTopicInstance().createProducer(),
                "public.progression.related-hearing-updated-for-adhoc-hearing",
                commandJson,
                metadataOf(randomUUID(), "public.progression.related-hearing-updated-for-adhoc-hearing")
                        .withUserId(randomUUID().toString())
                        .build()
        );

        getHearingPollForMatch(hearing.getId(), isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearing.getId()))
                        .withValue(h -> h.getProsecutionCases().size(), 2)
                        .with(Hearing::getProsecutionCases, hasItem(isBean(ProsecutionCase.class)
                                .withValue(ProsecutionCase::getId, caseId)
                        ))
                )
        );
        // Adding dummy assertion
        assertThat(hearing.getId(), is(not(nullValue())));

    }


    private List<ProsecutionCase> cloneCase(final Hearing hearing, final UUID caseId) {
        final List<ProsecutionCase> prosecutionCases = hearing.getProsecutionCases();

        prosecutionCases.get(0).setId(caseId);
        prosecutionCases.get(0).getDefendants().get(0).setId(randomUUID());
        prosecutionCases.get(0).getDefendants().get(0).getOffences().get(0).setId(randomUUID());
        prosecutionCases.get(0).getDefendants().get(0).getOffences().get(0).getReportingRestrictions().get(0).setId(randomUUID());
        prosecutionCases.get(0).getCaseMarkers().get(0).setId(randomUUID());
        return prosecutionCases;
    }

    private void extend(boolean insert) throws Exception {

        final InitiateHearingCommandHelper hearingOne = h(initiateHearing(getRequestSpec(), minimumInitiateHearingTemplate()));

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
        final CourtApplication newCourtApplication = (new HearingFactory()).courtApplication().build();
        extendHearingCommand.setHearingId(hearing.getId());
        if (!insert) {
            extendHearingCommand.setHearingDays(of(HearingDay.hearingDay().withSittingDay(ZonedDateTime.now()).withListedDurationMinutes(20).build()));
            newCourtApplication.setId(initialCourtApplication.getId());
        }
        extendHearingCommand.setCourtApplication(newCourtApplication);

        JsonObject commandJson = Utilities.JsonUtil.objectToJsonObject(extendHearingCommand);

        sendMessage(getPublicTopicInstance().createProducer(),
                PROGRESSION_EVENTS_HEARING_EXTENDED,
                commandJson,
                metadataOf(randomUUID(), PROGRESSION_EVENTS_HEARING_EXTENDED)
                        .withUserId(randomUUID().toString())
                        .build()
        );

        int expectedApplicationCount = hearing.getCourtApplications().size() + (insert ? 1 : 0);
        int listedDurationMin = insert ? hearing.getHearingDays().get(0).getListedDurationMinutes() : 20;

        getHearingPollForMatch(hearing.getId(), isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearing.getId()))
                        .withValue(h -> h.getCourtApplications().size(), expectedApplicationCount)
                        .with(Hearing::getCourtApplications, hasItem(isBean(CourtApplication.class)
                                .withValue(CourtApplication::getId, extendHearingCommand.getCourtApplication().getId())
                                .withValue(CourtApplication::getApplicationReference, extendHearingCommand.getCourtApplication().getApplicationReference())
                        ))
                        .with(Hearing::getHearingDays, hasItem(isBean(HearingDay.class)
                                .withValue(HearingDay::getListedDurationMinutes, listedDurationMin)))
                )
        );
    }

    private void extendBreachApplication() throws Exception {

        final InitiateHearingCommandHelper hearingOne = h(initiateHearingWithoutBreachApplication(getRequestSpec(), minimumInitiateHearingTemplate()));
        final Hearing hearing = hearingOne.getHearing();

        UUID courtApplicationId = randomUUID();

        final JsonObject publicEventBreachApplicationsToBeAdded = Json.createObjectBuilder()
                .add("hearingId", hearing.getId().toString())
                .add("breachedApplications", Json.createArrayBuilder().add(courtApplicationId.toString()))
                .build();

        sendMessage(getPublicTopicInstance().createProducer(),
                "public.progression.breach-applications-to-be-added-to-hearing",
                publicEventBreachApplicationsToBeAdded,
                metadataOf(randomUUID(), "public.progression.breach-applications-to-be-added-to-hearing")
                        .withUserId(randomUUID().toString())
                        .build()
        );

        ExtendHearingCommand extendHearingCommand = new ExtendHearingCommand();
        final CourtApplication newCourtApplication = (new HearingFactory()).courtApplication().build();
        extendHearingCommand.setHearingId(hearing.getId());

        extendHearingCommand.setHearingDays(of(HearingDay.hearingDay().withSittingDay(ZonedDateTime.now()).withListedDurationMinutes(20).build()));
        newCourtApplication.setId(courtApplicationId);

        extendHearingCommand.setCourtApplication(newCourtApplication);

        JsonObject commandJson = Utilities.JsonUtil.objectToJsonObject(extendHearingCommand);

        sendMessage(getPublicTopicInstance().createProducer(),
                PROGRESSION_EVENTS_HEARING_EXTENDED,
                commandJson,
                metadataOf(randomUUID(), PROGRESSION_EVENTS_HEARING_EXTENDED)
                        .withUserId(randomUUID().toString())
                        .build()
        );

        int listedDurationMin = 20;

        getHearingPollForMatch(hearing.getId(), isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearing.getId()))
                        .with(Hearing::getCourtApplications, hasItem(isBean(CourtApplication.class)
                                .withValue(CourtApplication::getId, extendHearingCommand.getCourtApplication().getId())
                                .withValue(CourtApplication::getApplicationReference, extendHearingCommand.getCourtApplication().getApplicationReference())
                        ))
                        .with(Hearing::getHearingDays, hasItem(isBean(HearingDay.class)
                                .withValue(HearingDay::getListedDurationMinutes, listedDurationMin)))
                )
        );


    }

}
