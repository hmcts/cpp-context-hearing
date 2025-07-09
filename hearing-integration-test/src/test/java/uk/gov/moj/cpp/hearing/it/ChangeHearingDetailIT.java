package uk.gov.moj.cpp.hearing.it;

import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.is;
import static uk.gov.justice.core.courts.CourtCentre.courtCentre;
import static uk.gov.justice.core.courts.HearingDay.hearingDay;
import static uk.gov.justice.core.courts.HearingType.hearingType;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataOf;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.moj.cpp.hearing.command.hearing.details.HearingDetailsUpdateCommand.hearingDetailsUpdateCommand;
import static uk.gov.moj.cpp.hearing.it.Queries.getHearingPollForMatch;
import static uk.gov.moj.cpp.hearing.it.UseCases.initiateHearing;
import static uk.gov.moj.cpp.hearing.it.UseCases.updateHearing;
import static uk.gov.moj.cpp.hearing.it.Utilities.makeCommand;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplateWithoutJudiciaryUserId;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.test.matchers.ElementAtListMatcher.first;
import static uk.gov.moj.cpp.hearing.utils.QueueUtil.getPublicTopicInstance;
import static uk.gov.moj.cpp.hearing.utils.QueueUtil.sendMessage;
import static uk.gov.moj.cpp.hearing.utils.RestUtils.DEFAULT_POLL_TIMEOUT_IN_SEC;
import static uk.gov.moj.cpp.hearing.utils.WireMockStubUtils.stubUsersAndGroupsUserRoles;

import uk.gov.justice.core.courts.CourtCentre;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.HearingDay;
import uk.gov.justice.core.courts.HearingLanguage;
import uk.gov.justice.core.courts.HearingType;
import uk.gov.justice.core.courts.JudicialRole;
import uk.gov.justice.core.courts.JurisdictionType;
import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.moj.cpp.hearing.command.hearing.details.HearingDetailsUpdateCommand;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.HearingDetailsResponse;
import uk.gov.moj.cpp.hearing.test.CommandHelpers;
import uk.gov.moj.cpp.hearing.test.CommandHelpers.InitiateHearingCommandHelper;
import uk.gov.moj.cpp.hearing.test.CoreTestTemplates;

import java.io.IOException;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.UUID;

import org.junit.jupiter.api.Test;

@SuppressWarnings("unchecked")
public class ChangeHearingDetailIT extends AbstractIT {

    @Test
    public void shouldUpdateHearingByCallingEndpointDirectly() throws Exception {
        final InitiateHearingCommandHelper hearingOne = h(initiateHearing(getRequestSpec(), standardInitiateHearingTemplate()));
        final UUID newCourtCentreId = randomUUID();
        final UUID newJudicialId = randomUUID();
        final UUID newUserId = randomUUID();

        sendChangeHearingDetailCommand(hearingOne, newCourtCentreId, newJudicialId, newUserId);

        getHearingPollForMatch(hearingOne.getHearingId(), isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearingOne.getHearingId()))
                        .with(Hearing::getCourtCentre, isBean(CourtCentre.class)
                                .with(CourtCentre::getId, is(newCourtCentreId))
                        )
                        .with(Hearing::getJudiciary, first(isBean(JudicialRole.class)
                                .with(JudicialRole::getJudicialId, is(newJudicialId))
                                .with(JudicialRole::getUserId, is(newUserId))
                                .with(JudicialRole::getUserId, not(hearingOne.getHearing().getJudiciary().get(0).getUserId()))
                                .with(JudicialRole::getJudicialId, not(hearingOne.getHearing().getJudiciary().get(0).getJudicialId()))
                        ))
                ));
    }
    @Test
    public void shouldUpdateHearingJudiaciaryWithoutAssociatedUser() throws Exception {
        final CommandHelpers.InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(getRequestSpec(), standardInitiateHearingTemplateWithoutJudiciaryUserId()));


        UUID cpUserId = UUID.randomUUID();
        final String eventPayloadString = getStringFromResource("public.referencedata.event.user-associated-with-judiciary.json")
                .replaceAll("JUDICIARY_ID", hearingOne.getHearing().getJudiciary().stream().findFirst().get().getJudicialId().toString())
                .replaceAll("CP_USER_ID", cpUserId.toString())
                .replaceAll("EMAIL_ID", "abc@dummy123.com");

        sendMessage(getPublicTopicInstance().createProducer(),
                "public.referencedata.event.user-associated-with-judiciary",
                new StringToJsonObjectConverter().convert(eventPayloadString),
                metadataOf(randomUUID(), "public.referencedata.event.user-associated-with-judiciary")
                        .withUserId(randomUUID().toString())
                        .build()
        );

        Queries.getHearingPollForMatch(hearingOne.getHearingId(), DEFAULT_POLL_TIMEOUT_IN_SEC, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearingOne.getHearingId()))
                        .with(Hearing::getCourtCentre, isBean(CourtCentre.class)
                        )
                        .with(Hearing::getJudiciary, first(isBean(JudicialRole.class)
                                .with(JudicialRole::getUserId, is(cpUserId))
                        ))
                ));
    }

    @Test
    public void shouldUpdateHearing_WhenChangeInitiatedByProgression() throws Exception {

        stubUsersAndGroupsUserRoles(getLoggedInUser());

        final InitiateHearingCommandHelper hearingOne = h(initiateHearing(getRequestSpec(), standardInitiateHearingTemplate()));

        HearingDetailsUpdateCommand hearingDetailsUpdateCommand = updateHearing(hearingDetailsUpdateCommand()
                .setHearing(uk.gov.moj.cpp.hearing.command.hearing.details.Hearing.hearing()
                        .setId(hearingOne.getHearingId())
                        .setCourtCentre(courtCentre()
                                .withId(randomUUID())
                                .withName("Name 1")
                                .withRoomId(randomUUID())
                                .withRoomName("Room Name 1")
                                .withWelshName(STRING.next())
                                .withWelshRoomName(STRING.next())
                                .build())
                        .setType(hearingType()
                                .withId(randomUUID())
                                .withDescription("Sentencing")
                                .build())
                        .setHearingLanguage(HearingLanguage.ENGLISH)
                        .setJurisdictionType(JurisdictionType.CROWN)
                        .setReportingRestrictionReason("Nothing")
                        .setJudiciary(Arrays.asList(JudicialRole.judicialRole()
                                .withJudicialId(randomUUID())
                                .withTitle("Mr")
                                .withFirstName("John")
                                .withMiddleName("Elton")
                                .withLastName("Smith")
                                .withJudicialRoleType(CoreTestTemplates.circuitJudge())
                                .withIsBenchChairman(false)
                                .withIsDeputy(false)
                                .build()))
                        .setHearingDays(Arrays.asList(hearingDay()
                                .withListedDurationMinutes(10)
                                .withListingSequence(20)
                                .withSittingDay(new UtcClock().now())
                                .build()))
                ));

        getHearingPollForMatch(hearingOne.getHearingId(), isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearingOne.getHearingId()))
                        .with(Hearing::getCourtCentre, isBean(CourtCentre.class)
                                .with(CourtCentre::getId, is(hearingDetailsUpdateCommand.getHearing().getCourtCentre().getId()))
                                .with(CourtCentre::getName, is(hearingDetailsUpdateCommand.getHearing().getCourtCentre().getName()))
                                .with(CourtCentre::getRoomId, is(hearingDetailsUpdateCommand.getHearing().getCourtCentre().getRoomId()))
                                .with(CourtCentre::getRoomName, is(hearingDetailsUpdateCommand.getHearing().getCourtCentre().getRoomName()))
                                .with(CourtCentre::getWelshName, is(hearingDetailsUpdateCommand.getHearing().getCourtCentre().getWelshName()))
                                .with(CourtCentre::getWelshRoomName, is(hearingDetailsUpdateCommand.getHearing().getCourtCentre().getWelshRoomName()))
                        )
                        .with(Hearing::getType, isBean(HearingType.class)
                                .with(HearingType::getId, is(hearingDetailsUpdateCommand.getHearing().getType().getId()))
                                .with(HearingType::getDescription, is(hearingDetailsUpdateCommand.getHearing().getType().getDescription()))
                        )
                        .with(Hearing::getHearingDays, first(isBean(HearingDay.class)
                                .with(HearingDay::getListedDurationMinutes, is(hearingDetailsUpdateCommand.getHearing().getHearingDays().get(0).getListedDurationMinutes()))
                                .with(HearingDay::getListingSequence, is(hearingDetailsUpdateCommand.getHearing().getHearingDays().get(0).getListingSequence()))
                                .with(HearingDay::getSittingDay, is(hearingDetailsUpdateCommand.getHearing().getHearingDays().get(0).getSittingDay().withZoneSameLocal(ZoneId.of("UTC"))))
                        ))
                        .with(Hearing::getJudiciary, first(isBean(JudicialRole.class)
                                .with(JudicialRole::getJudicialId, is(hearingDetailsUpdateCommand.getHearing().getJudiciary().get(0).getJudicialId()))
                                .with(JudicialRole::getTitle, is(hearingDetailsUpdateCommand.getHearing().getJudiciary().get(0).getTitle()))
                                .with(JudicialRole::getFirstName, is(hearingDetailsUpdateCommand.getHearing().getJudiciary().get(0).getFirstName()))
                                .with(JudicialRole::getMiddleName, is(hearingDetailsUpdateCommand.getHearing().getJudiciary().get(0).getMiddleName()))
                                .with(JudicialRole::getLastName, is(hearingDetailsUpdateCommand.getHearing().getJudiciary().get(0).getLastName()))
                                .withValue(jr -> jr.getJudicialRoleType().getJudiciaryType(), hearingDetailsUpdateCommand.getHearing().getJudiciary().get(0).getJudicialRoleType().getJudiciaryType())
                                .with(JudicialRole::getIsDeputy, is(hearingDetailsUpdateCommand.getHearing().getJudiciary().get(0).getIsDeputy()))
                                .with(JudicialRole::getIsBenchChairman, is(hearingDetailsUpdateCommand.getHearing().getJudiciary().get(0).getIsBenchChairman()))
                        ))
                        .with(Hearing::getReportingRestrictionReason, is(hearingDetailsUpdateCommand.getHearing().getReportingRestrictionReason()))
                        .with(Hearing::getHearingLanguage, is(hearingDetailsUpdateCommand.getHearing().getHearingLanguage()))
                        .with(Hearing::getJurisdictionType, is(hearingDetailsUpdateCommand.getHearing().getJurisdictionType()))
                )
        );
    }

    private void sendChangeHearingDetailCommand(final InitiateHearingCommandHelper hearing, final UUID courtCentreId, final UUID judicialId, final UUID userId) throws IOException {
        final String eventPayloadString = getStringFromResource("hearing.change-hearing-detail.json")
                .replaceAll("HEARING_ID", hearing.getHearing().getId().toString())
                .replaceAll("COURTCENTRE_ID", courtCentreId.toString())
                .replaceAll("JUDICIAL_ID", judicialId.toString())
                .replaceAll("USER_ID", userId.toString());

        makeCommand(getRequestSpec(), "hearing.change-hearing-detail")
                .ofType("application/vnd.hearing.change-hearing-detail+json")
                .withArgs(hearing.getHearing().getId())
                .withPayload(eventPayloadString)
                .withCppUserId(USER_ID_VALUE_AS_ADMIN)
                .executeSuccessfully();


    }

}
