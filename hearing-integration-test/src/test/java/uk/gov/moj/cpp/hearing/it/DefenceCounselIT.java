package uk.gov.moj.cpp.hearing.it;

import static com.google.common.collect.Lists.newArrayList;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.core.Is.is;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_ZONED_DATE_TIME;
import static uk.gov.moj.cpp.hearing.it.Queries.pollForHearing;
import static uk.gov.moj.cpp.hearing.it.UseCases.addDefenceCounsel;
import static uk.gov.moj.cpp.hearing.it.UseCases.asDefault;
import static uk.gov.moj.cpp.hearing.it.UseCases.initiateHearing;
import static uk.gov.moj.cpp.hearing.it.UseCases.logEvent;
import static uk.gov.moj.cpp.hearing.it.UseCases.removeDefenceCounsel;
import static uk.gov.moj.cpp.hearing.it.UseCases.updateDefenceCounsel;
import static uk.gov.moj.cpp.hearing.it.Utilities.listenFor;
import static uk.gov.moj.cpp.hearing.steps.HearingEventStepDefinitions.RECORDED_LABEL_END_HEARING;
import static uk.gov.moj.cpp.hearing.steps.HearingEventStepDefinitions.findEventDefinitionWithActionLabel;
import static uk.gov.moj.cpp.hearing.steps.HearingStepDefinitions.givenAUserHasLoggedInAsADefenceCounsel;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.AddDefenceCounselCommandTemplates.addDefenceCounselCommandTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.initiateHearingTemplateForMagistrates;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.UpdateDefenceCounselCommandTemplates.updateDefenceCounselCommandTemplate;
import static uk.gov.moj.cpp.hearing.utils.WireMockStubUtils.stubUsersAndGroupsUserRoles;

import uk.gov.justice.core.courts.DefenceCounsel;
import uk.gov.justice.hearing.courts.AddDefenceCounsel;
import uk.gov.justice.hearing.courts.RemoveDefenceCounsel;
import uk.gov.justice.hearing.courts.UpdateDefenceCounsel;
import uk.gov.moj.cpp.hearing.domain.HearingEventDefinition;
import uk.gov.moj.cpp.hearing.it.Utilities.EventListener;
import uk.gov.moj.cpp.hearing.test.CommandHelpers.InitiateHearingCommandHelper;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

import javax.annotation.concurrent.NotThreadSafe;

import io.restassured.path.json.JsonPath;
import org.junit.jupiter.api.Test;

@NotThreadSafe
class DefenceCounselIT extends AbstractIT {

    private static final ZonedDateTime EVENT_TIME = PAST_ZONED_DATE_TIME.next().withZoneSameLocal(ZoneId.of("UTC"));

    @SuppressWarnings("squid:S2699")
    @Test
    void testDefenceCounselOperations_addUpdateAndRemove() {

        final InitiateHearingCommandHelper hearingOne = h(initiateHearing(getRequestSpec(), standardInitiateHearingTemplate()));

        DefenceCounsel firstDefenceCounsel = createFirstDefenceCounsel(hearingOne);
        final DefenceCounsel secondDefenceCounsel = createSecondDefenceCounsel(hearingOne, firstDefenceCounsel);

        //Adding same defence counsel should be ignored
        final String currentLastNameValueForFprFirstPC = firstDefenceCounsel.getLastName();
        firstDefenceCounsel.setLastName("DummyLastName");

        try (EventListener publicDefenceCounselAdded = listenFor("public.hearing.defence-counsel-change-ignored")
                .withFilter(isJson(withJsonPath("$.hearingId", is(hearingOne.getHearingId().toString()))))) {

            addDefenceCounsel(getRequestSpec(), hearingOne.getHearingId(),
                    addDefenceCounselCommandTemplate(hearingOne.getHearingId(), firstDefenceCounsel)
            );

            publicDefenceCounselAdded.waitFor();
        }

        pollForHearing(hearingOne.getHearingId().toString(),
                withJsonPath("$.hearing.defenceCounsels.[0].id", is(firstDefenceCounsel.getId().toString())),
                withJsonPath("$.hearing.defenceCounsels.[0].status", is(firstDefenceCounsel.getStatus())),
                withJsonPath("$.hearing.defenceCounsels.[0].firstName", is(firstDefenceCounsel.getFirstName())),
                withJsonPath("$.hearing.defenceCounsels.[0].lastName", is(currentLastNameValueForFprFirstPC)),
                withJsonPath("$.hearing.defenceCounsels.[0].title", is(firstDefenceCounsel.getTitle())),
                withJsonPath("$.hearing.defenceCounsels.[0].middleName", is(firstDefenceCounsel.getMiddleName())),
                withJsonPath("$.hearing.defenceCounsels.[0].attendanceDays.[0]", is(firstDefenceCounsel.getAttendanceDays().get(0).toString())),
                withJsonPath("$.hearing.defenceCounsels.[0].defendants.[0]", is(firstDefenceCounsel.getDefendants().get(0).toString())),
                withJsonPath("$.hearing.defenceCounsels.[0].userId", is(firstDefenceCounsel.getUserId().toString()))
        );

        final UpdateDefenceCounsel firstDefenceCounselReAddCommand;
        try (EventListener publicDefenceCounselUpdated = listenFor("public.hearing.defence-counsel-updated")
                .withFilter(isJson(withJsonPath("$.hearingId", is(hearingOne.getHearingId().toString()))))) {

            //Updating first defence counsel
            firstDefenceCounsel.setFirstName("DummyFirstName");
            firstDefenceCounsel.setLastName("DummyLastName");
            firstDefenceCounsel.setStatus("DummyStatus");
            firstDefenceCounsel.setTitle("UpdateTitle");
            firstDefenceCounsel.setMiddleName("DummyMiddleName");
            firstDefenceCounsel.setAttendanceDays(newArrayList(LocalDate.now().plusDays(1)));

            firstDefenceCounselReAddCommand = updateDefenceCounsel(getRequestSpec(), hearingOne.getHearingId(),
                    updateDefenceCounselCommandTemplate(hearingOne.getHearingId(), firstDefenceCounsel)
            );

            publicDefenceCounselUpdated.waitFor();
        }

        DefenceCounsel firstDefenceCounselUpdated = firstDefenceCounselReAddCommand.getDefenceCounsel();
        pollForHearing(hearingOne.getHearingId().toString(),
                withJsonPath("$.hearing.defenceCounsels.[0].id", is(firstDefenceCounsel.getId().toString())),
                withJsonPath("$.hearing.defenceCounsels.[0].status", is(firstDefenceCounselUpdated.getStatus())),
                withJsonPath("$.hearing.defenceCounsels.[0].firstName", is(firstDefenceCounselUpdated.getFirstName())),
                withJsonPath("$.hearing.defenceCounsels.[0].lastName", is(firstDefenceCounselUpdated.getLastName())),
                withJsonPath("$.hearing.defenceCounsels.[0].title", is(firstDefenceCounselUpdated.getTitle())),
                withJsonPath("$.hearing.defenceCounsels.[0].middleName", is(firstDefenceCounselUpdated.getMiddleName())),
                withJsonPath("$.hearing.defenceCounsels.[0].attendanceDays.[0]", is(firstDefenceCounsel.getAttendanceDays().get(0).toString())),
                withJsonPath("$.hearing.defenceCounsels.[0].defendants.[0]", is(firstDefenceCounsel.getDefendants().get(0).toString())),
                withJsonPath("$.hearing.defenceCounsels.[0].userId", is(firstDefenceCounsel.getUserId().toString()))
        );

        try (EventListener publicDefenceCounselRemoved = listenFor("public.hearing.defence-counsel-removed")
                .withFilter(isJson(withJsonPath("$.hearingId", is(hearingOne.getHearingId().toString()))))) {

            //remove first defence counsel
            removeDefenceCounsel(getRequestSpec(), hearingOne.getHearingId(),
                    new RemoveDefenceCounsel(hearingOne.getHearingId(), firstDefenceCounsel.getId())
            );

            publicDefenceCounselRemoved.waitFor();
        }

        pollForHearing(hearingOne.getHearingId().toString(),
                withJsonPath("$.hearing.defenceCounsels", hasSize(1)),
                withJsonPath("$.hearing.defenceCounsels.[0].id", is(secondDefenceCounsel.getId().toString()))
        );

    }

    @Test
    void addDefenceCounsel_failedCheckin_SPICases_whereCaseURNisPopulated() {

        final UUID userId = randomUUID();
        givenAUserHasLoggedInAsADefenceCounsel(userId);

        stubUsersAndGroupsUserRoles(userId);

        final InitiateHearingCommandHelper hearingOne = h(initiateHearing(getRequestSpec(), standardInitiateHearingTemplate()));

        final HearingEventDefinition hearingEventDefinition = findEventDefinitionWithActionLabel(RECORDED_LABEL_END_HEARING);

        logEvent(randomUUID(), getRequestSpec(), asDefault(), hearingOne.it(),
                hearingEventDefinition.getId(), false, randomUUID(), EVENT_TIME, RECORDED_LABEL_END_HEARING, null);

        //Add Defence Counsel
        JsonPath jsonPath;
        try (EventListener publicDefenceCounselAdded = listenFor("public.hearing.defence-counsel-change-ignored")
                .withFilter(isJson(withJsonPath("$.hearingId", is(hearingOne.getHearingId().toString()))))) {

            addDefenceCounsel(getRequestSpec(), hearingOne.getHearingId(),
                    addDefenceCounselCommandTemplate(hearingOne)
            );

            jsonPath = publicDefenceCounselAdded.waitFor();
        }

        String caseURN = hearingOne.getHearing().getProsecutionCases().get(0).getProsecutionCaseIdentifier().getCaseURN();
        assertThat(jsonPath.getString("caseURN"), is(caseURN));
        assertThat(jsonPath.getString("prosecutionAuthorityReference"), isEmptyOrNullString());
    }

    @Test
    void addDefenceCounsel_failedCheckin_SJPCases_wherePARisPopulated() {

        final UUID userId = randomUUID();

        givenAUserHasLoggedInAsADefenceCounsel(userId);
        stubUsersAndGroupsUserRoles(userId);

        final InitiateHearingCommandHelper hearingOne = h(initiateHearing(getRequestSpec(), initiateHearingTemplateForMagistrates()));

        final HearingEventDefinition hearingEventDefinition = findEventDefinitionWithActionLabel(RECORDED_LABEL_END_HEARING);

        logEvent(randomUUID(), requestSpec, asDefault(), hearingOne.it(),
                hearingEventDefinition.getId(), false, randomUUID(), EVENT_TIME, RECORDED_LABEL_END_HEARING, null);

        //Add Defence Counsel
        JsonPath jsonPath;
        try (EventListener publicDefenceCounselAdded = listenFor("public.hearing.defence-counsel-change-ignored")
                .withFilter(isJson(withJsonPath("$.hearingId", is(hearingOne.getHearingId().toString()))))) {

            addDefenceCounsel(getRequestSpec(), hearingOne.getHearingId(),
                    addDefenceCounselCommandTemplate(hearingOne)
            );

            jsonPath = publicDefenceCounselAdded.waitFor();
        }

        String caseURN = hearingOne.getHearing().getProsecutionCases().get(0).getProsecutionCaseIdentifier().getCaseURN();
        String prosecutionAuthorityReference = hearingOne.getHearing().getProsecutionCases().get(0).getProsecutionCaseIdentifier().getProsecutionAuthorityReference();
        assertThat(jsonPath.getString("caseURN"), is(prosecutionAuthorityReference));
        assertThat(caseURN, isEmptyOrNullString());
    }

    private DefenceCounsel createFirstDefenceCounsel(final InitiateHearingCommandHelper hearingOne) {
        final AddDefenceCounsel firstDefenceCounselCommand;
        try (EventListener publicDefenceCounselAdded = listenFor("public.hearing.defence-counsel-added")
                .withFilter(isJson(withJsonPath("$.hearingId", is(hearingOne.getHearingId().toString()))))) {

            firstDefenceCounselCommand = addDefenceCounsel(getRequestSpec(), hearingOne.getHearingId(),
                    addDefenceCounselCommandTemplate(hearingOne)
            );

            publicDefenceCounselAdded.waitFor();
        }

        DefenceCounsel firstDefenceCounsel = firstDefenceCounselCommand.getDefenceCounsel();
        pollForHearing(hearingOne.getHearingId().toString(),
                withJsonPath("$.hearing.defenceCounsels.[0].status", is(firstDefenceCounsel.getStatus())),
                withJsonPath("$.hearing.defenceCounsels.[0].firstName", is(firstDefenceCounsel.getFirstName())),
                withJsonPath("$.hearing.defenceCounsels.[0].lastName", is(firstDefenceCounsel.getLastName())),
                withJsonPath("$.hearing.defenceCounsels.[0].title", is(firstDefenceCounsel.getTitle())),
                withJsonPath("$.hearing.defenceCounsels.[0].middleName", is(firstDefenceCounsel.getMiddleName())),
                withJsonPath("$.hearing.defenceCounsels.[0].attendanceDays.[0]", is(firstDefenceCounsel.getAttendanceDays().get(0).toString())),
                withJsonPath("$.hearing.defenceCounsels.[0].defendants.[0]", is(firstDefenceCounsel.getDefendants().get(0).toString()))
        );
        return firstDefenceCounsel;
    }

    private DefenceCounsel createSecondDefenceCounsel(final InitiateHearingCommandHelper hearingOne, final DefenceCounsel firstDefenceCounsel) {
        final AddDefenceCounsel secondDefenceCounselCommand = addDefenceCounsel(getRequestSpec(), hearingOne.getHearingId(),
                addDefenceCounselCommandTemplate(hearingOne)
        );
        DefenceCounsel secondDefenceCounsel = secondDefenceCounselCommand.getDefenceCounsel();
        pollForHearing(hearingOne.getHearingId().toString(),
                withJsonPath("$.hearing.defenceCounsels.[0].status", is(firstDefenceCounsel.getStatus())),
                withJsonPath("$.hearing.defenceCounsels.[0].firstName", is(firstDefenceCounsel.getFirstName())),
                withJsonPath("$.hearing.defenceCounsels.[0].lastName", is(firstDefenceCounsel.getLastName())),
                withJsonPath("$.hearing.defenceCounsels.[0].title", is(firstDefenceCounsel.getTitle())),
                withJsonPath("$.hearing.defenceCounsels.[0].middleName", is(firstDefenceCounsel.getMiddleName())),
                withJsonPath("$.hearing.defenceCounsels.[0].attendanceDays.[0]", is(firstDefenceCounsel.getAttendanceDays().get(0).toString())),
                withJsonPath("$.hearing.defenceCounsels.[0].defendants.[0]", is(firstDefenceCounsel.getDefendants().get(0).toString())),

                withJsonPath("$.hearing.defenceCounsels.[1].status", is(secondDefenceCounsel.getStatus())),
                withJsonPath("$.hearing.defenceCounsels.[1].firstName", is(secondDefenceCounsel.getFirstName())),
                withJsonPath("$.hearing.defenceCounsels.[1].lastName", is(secondDefenceCounsel.getLastName())),
                withJsonPath("$.hearing.defenceCounsels.[1].title", is(secondDefenceCounsel.getTitle())),
                withJsonPath("$.hearing.defenceCounsels.[1].middleName", is(secondDefenceCounsel.getMiddleName())),
                withJsonPath("$.hearing.defenceCounsels.[1].attendanceDays.[0]", is(secondDefenceCounsel.getAttendanceDays().get(0).toString())),
                withJsonPath("$.hearing.defenceCounsels.[1].defendants.[0]", is(secondDefenceCounsel.getDefendants().get(0).toString()))
        );
        return secondDefenceCounsel;
    }
}