package uk.gov.moj.cpp.hearing.it;

import static java.util.UUID.randomUUID;
import static org.hamcrest.Matchers.is;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.test.matchers.ElementAtListMatcher.first;
import static uk.gov.moj.cpp.hearing.utils.RestUtils.DEFAULT_POLL_TIMEOUT_IN_SEC;

import uk.gov.justice.core.courts.CourtCentre;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.HearingDay;
import uk.gov.justice.core.courts.HearingLanguage;
import uk.gov.justice.core.courts.HearingType;
import uk.gov.justice.core.courts.JudicialRole;
import uk.gov.justice.core.courts.JurisdictionType;
import uk.gov.moj.cpp.hearing.command.hearingDetails.HearingDetailsUpdateCommand;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.HearingDetailsResponse;
import uk.gov.moj.cpp.hearing.test.CommandHelpers;
import uk.gov.moj.cpp.hearing.test.CoreTestTemplates;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;

import org.junit.Test;

@SuppressWarnings("unchecked")
public class ChangeHearingDetailIT extends AbstractIT {

    @Test
    public void shouldUpdateHearing() throws Exception {

        final CommandHelpers.InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(getRequestSpec(), standardInitiateHearingTemplate()));

        HearingDetailsUpdateCommand hearingDetailsUpdateCommand = UseCases.updateHearing(HearingDetailsUpdateCommand.hearingDetailsUpdateCommand()
                .setHearing(uk.gov.moj.cpp.hearing.command.hearingDetails.Hearing.hearing()
                        .setId(hearingOne.getHearingId())
                        .setCourtCentre(CourtCentre.courtCentre()
                                .withId(randomUUID())
                                .withName("Name 1")
                                .withRoomId(randomUUID())
                                .withRoomName("Room Name 1")
                                .withWelshName(STRING.next())
                                .withWelshRoomName(STRING.next())
                                .build())
                        .setType(HearingType.hearingType()
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
                        .setHearingDays(Arrays.asList(HearingDay.hearingDay()
                                .withListedDurationMinutes(10)
                                .withListingSequence(20)
                                .withSittingDay(ZonedDateTime.now())
                                .build()))
                ));

        Queries.getHearingPollForMatch(hearingOne.getHearingId(), DEFAULT_POLL_TIMEOUT_IN_SEC, isBean(HearingDetailsResponse.class)
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
                                .withValue(jr->jr.getJudicialRoleType().getJudiciaryType(), hearingDetailsUpdateCommand.getHearing().getJudiciary().get(0).getJudicialRoleType().getJudiciaryType())
                                .with(JudicialRole::getIsDeputy, is(hearingDetailsUpdateCommand.getHearing().getJudiciary().get(0).getIsDeputy()))
                                .with(JudicialRole::getIsBenchChairman, is(hearingDetailsUpdateCommand.getHearing().getJudiciary().get(0).getIsBenchChairman()))
                        ))
                        .with(Hearing::getReportingRestrictionReason, is(hearingDetailsUpdateCommand.getHearing().getReportingRestrictionReason()))
                        .with(Hearing::getHearingLanguage, is(hearingDetailsUpdateCommand.getHearing().getHearingLanguage()))
                        .with(Hearing::getJurisdictionType, is(hearingDetailsUpdateCommand.getHearing().getJurisdictionType()))
                )
        );
    }

}
