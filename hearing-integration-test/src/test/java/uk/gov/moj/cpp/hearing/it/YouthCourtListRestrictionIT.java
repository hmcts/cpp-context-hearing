package uk.gov.moj.cpp.hearing.it;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static uk.gov.moj.cpp.hearing.it.UseCases.initiateHearing;
import static uk.gov.moj.cpp.hearing.it.UseCases.initiateHearingForApplication;
import static uk.gov.moj.cpp.hearing.it.UseCases.sendPublicApplicationChangedMessage;
import static uk.gov.moj.cpp.hearing.it.UseCases.updateDefendants;
import static uk.gov.moj.cpp.hearing.it.Utilities.listenFor;
import static uk.gov.moj.cpp.hearing.it.Utilities.makeCommand;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.CaseDefendantDetailsChangedCommandTemplates.caseDefendantDetailsChangedCommandTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingWithApplicationTemplate;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.with;

import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.core.courts.CourtApplicationParty;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.MasterDefendant;
import uk.gov.justice.core.courts.PersonDefendant;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.it.Utilities.EventListener;
import uk.gov.moj.cpp.hearing.steps.CourtListRestrictionSteps;
import uk.gov.moj.cpp.hearing.test.CommandHelpers.InitiateHearingCommandHelper;
import uk.gov.moj.cpp.hearing.test.HearingFactory;

import java.util.UUID;

import javax.json.Json;

import io.restassured.path.json.JsonPath;

import org.junit.jupiter.api.Test;

public class YouthCourtListRestrictionIT extends AbstractIT {

    @Test
    public void shouldRestrictCourtListWhenDefendantBecomesYouth() {
        final InitiateHearingCommandHelper hearingHelper = h(initiateHearing(getRequestSpec(), standardInitiateHearingTemplate()));
        final Hearing hearing = hearingHelper.getHearing();
        final UUID defendantId = hearingHelper.getFirstDefendantForFirstCase().getId();

        try (EventListener eventListener = listenFor("public.hearing.defendants-in-youthcourt-updated")
                .withFilter(isJson(withJsonPath("$.hearingId", is(hearing.getId().toString()))))) {
            makeCommand(getRequestSpec(), "hearing.youth-court-defendants")
                    .withArgs(hearing.getId())
                    .ofType("application/vnd.hearing.youth-court-defendants+json")
                    .withPayload(Json.createObjectBuilder()
                            .add("youthCourtDefendantIds", Json.createArrayBuilder().add(defendantId.toString()))
                            .build()
                            .toString())
                    .executeSuccessfully();
            eventListener.waitFor();
        }

        final CourtListRestrictionSteps steps = new CourtListRestrictionSteps();
        steps.hideDefendantFromXhibit(hearing, true);

        final JsonPath restrictedEvent = steps.hearingEventsCourtListRestrictedReceived(isJson(allOf(
                withJsonPath("$.hearingId", is(hearing.getId().toString())),
                withJsonPath("$.defendantIds", hasSize(1)),
                withJsonPath("$.restrictCourtList", is(true)))));
        assertThat(restrictedEvent.getBoolean("restrictCourtList"), is(true));
    }

    @Test
    public void shouldRestrictCourtListWhenDefendantUpdatedFromAdultToYouth() throws Exception {
        final InitiateHearingCommandHelper hearingHelper = h(initiateHearing(getRequestSpec(), standardInitiateHearingTemplate()));
        final Hearing hearing = hearingHelper.getHearing();

        final CourtListRestrictionSteps steps = new CourtListRestrictionSteps();

        steps.hideDefendantFromXhibit(hearing, false);
        final JsonPath notRestrictedEvent = steps.hearingEventsCourtListRestrictedReceived(isJson(allOf(
                withJsonPath("$.hearingId", is(hearing.getId().toString())),
                withJsonPath("$.defendantIds", hasSize(1)),
                withJsonPath("$.restrictCourtList", is(false)))));
        assertThat(notRestrictedEvent.getBoolean("restrictCourtList"), is(false));

        updateDefendants(with(caseDefendantDetailsChangedCommandTemplate(), template -> {
            template.getDefendants().get(0).setId(hearingHelper.getFirstDefendantForFirstCase().getId());
            template.getDefendants().get(0).setProsecutionCaseId(hearingHelper.getFirstDefendantForFirstCase().getProsecutionCaseId());
            template.getDefendants().get(0).setIsYouth(true);
        }));

        steps.hideDefendantFromXhibit(hearing, true);
        final JsonPath restrictedEvent = steps.hearingEventsCourtListRestrictedReceived(isJson(allOf(
                withJsonPath("$.hearingId", is(hearing.getId().toString())),
                withJsonPath("$.defendantIds", hasSize(1)),
                withJsonPath("$.restrictCourtList", is(true)))));
        assertThat(restrictedEvent.getBoolean("restrictCourtList"), is(true));
    }

    @Test
    public void shouldRestrictCourtListForHearingWithCaseAndApplicationWhenBothUpdatedToYouth() throws Exception {
        final HearingFactory factory = new HearingFactory();
        final CourtApplicationParty adultApplicant = buildAdultApplicationParty(factory);
        final CourtApplication courtApplication = factory.courtApplication(adultApplicant).build();

        final InitiateHearingCommandHelper hearingHelper = h(initiateHearing(getRequestSpec(),
                standardInitiateHearingWithApplicationTemplate(singletonList(courtApplication))));
        final Hearing hearing = hearingHelper.getHearing();

        final CourtListRestrictionSteps steps = new CourtListRestrictionSteps();

        steps.hideDefendantFromXhibit(hearing, false);
        final JsonPath defendantNotRestricted = steps.hearingEventsCourtListRestrictedReceived(isJson(allOf(
                withJsonPath("$.hearingId", is(hearing.getId().toString())),
                withJsonPath("$.defendantIds", hasSize(1)),
                withJsonPath("$.restrictCourtList", is(false)))));
        assertThat(defendantNotRestricted.getBoolean("restrictCourtList"), is(false));

        steps.hideApplicationApplicantFromXhibit(hearing, false);
        final JsonPath applicantNotRestricted = steps.hearingEventsCourtListRestrictedReceived(isJson(allOf(
                withJsonPath("$.hearingId", is(hearing.getId().toString())),
                withJsonPath("$.courtApplicationApplicantIds", hasSize(1)),
                withJsonPath("$.restrictCourtList", is(false)))));
        assertThat(applicantNotRestricted.getBoolean("restrictCourtList"), is(false));

        updateDefendants(with(caseDefendantDetailsChangedCommandTemplate(), template -> {
            template.getDefendants().get(0).setId(hearingHelper.getFirstDefendantForFirstCase().getId());
            template.getDefendants().get(0).setProsecutionCaseId(hearingHelper.getFirstDefendantForFirstCase().getProsecutionCaseId());
            template.getDefendants().get(0).setIsYouth(true);
        }));

        courtApplication.getApplicant().getMasterDefendant().setIsYouth(true);
        sendPublicApplicationChangedMessage(courtApplication);

        steps.hideDefendantFromXhibit(hearing, true);
        final JsonPath defendantRestricted = steps.hearingEventsCourtListRestrictedReceived(isJson(allOf(
                withJsonPath("$.hearingId", is(hearing.getId().toString())),
                withJsonPath("$.defendantIds", hasSize(1)),
                withJsonPath("$.restrictCourtList", is(true)))));
        assertThat(defendantRestricted.getBoolean("restrictCourtList"), is(true));

        steps.hideApplicationApplicantFromXhibit(hearing, true);
        final JsonPath applicantRestricted = steps.hearingEventsCourtListRestrictedReceived(isJson(allOf(
                withJsonPath("$.hearingId", is(hearing.getId().toString())),
                withJsonPath("$.courtApplicationApplicantIds", hasSize(1)),
                withJsonPath("$.restrictCourtList", is(true)))));
        assertThat(applicantRestricted.getBoolean("restrictCourtList"), is(true));
    }

    @Test
    public void shouldRestrictCourtListForApplicationOnlyHearingWhenSubjectUpdatedToYouth() throws Exception {
        final HearingFactory factory = new HearingFactory();
        final CourtApplicationParty adultApplicant = buildAdultApplicationParty(factory);
        final CourtApplication courtApplication = factory.courtApplication(adultApplicant).build();

        final InitiateHearingCommand initiateHearingCommand = standardInitiateHearingWithApplicationTemplate(singletonList(courtApplication));
        h(initiateHearingForApplication(getRequestSpec(), initiateHearingCommand));
        final Hearing hearing = initiateHearingCommand.getHearing();

        final CourtListRestrictionSteps steps = new CourtListRestrictionSteps();

        steps.hideApplicationApplicantFromXhibit(hearing, false);
        final JsonPath notRestricted = steps.hearingEventsCourtListRestrictedReceived(isJson(allOf(
                withJsonPath("$.hearingId", is(hearing.getId().toString())),
                withJsonPath("$.courtApplicationApplicantIds", hasSize(1)),
                withJsonPath("$.restrictCourtList", is(false)))));
        assertThat(notRestricted.getBoolean("restrictCourtList"), is(false));

        courtApplication.getApplicant().getMasterDefendant().setIsYouth(true);
        sendPublicApplicationChangedMessage(courtApplication);

        steps.hideApplicationApplicantFromXhibit(hearing, true);
        final JsonPath restricted = steps.hearingEventsCourtListRestrictedReceived(isJson(allOf(
                withJsonPath("$.hearingId", is(hearing.getId().toString())),
                withJsonPath("$.courtApplicationApplicantIds", hasSize(1)),
                withJsonPath("$.restrictCourtList", is(true)))));
        assertThat(restricted.getBoolean("restrictCourtList"), is(true));
    }

    private CourtApplicationParty buildAdultApplicationParty(final HearingFactory factory) {
        final MasterDefendant adultMasterDefendant = MasterDefendant.masterDefendant()
                .withMasterDefendantId(randomUUID())
                .withPersonDefendant(PersonDefendant.personDefendant()
                        .withPersonDetails(factory.person().build())
                        .build())
                .withIsYouth(false)
                .build();

        return CourtApplicationParty.courtApplicationParty()
                .withMasterDefendant(adultMasterDefendant)
                .withId(randomUUID())
                .withSummonsRequired(false)
                .withNotificationRequired(false)
                .withPersonDetails(factory.person().build())
                .build();
    }
}
