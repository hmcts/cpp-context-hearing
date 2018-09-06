package uk.gov.moj.cpp.hearing.it;


import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.time.format.DateTimeFormatter.ISO_INSTANT;
import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.Matchers.is;
import static uk.gov.justice.services.test.utils.core.http.RequestParamsBuilder.requestParams;
import static uk.gov.justice.services.test.utils.core.http.RestPoller.poll;
import static uk.gov.justice.services.test.utils.core.matchers.ResponsePayloadMatcher.payload;
import static uk.gov.justice.services.test.utils.core.matchers.ResponseStatusMatcher.status;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataOf;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_ZONED_DATE_TIME;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.CaseDefendantDetailsChangedCommandTemplates.caseDefendantDetailsChangedCommandTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.test.matchers.ElementAtListMatcher.first;
import static uk.gov.moj.cpp.hearing.utils.QueueUtil.publicEvents;
import static uk.gov.moj.cpp.hearing.utils.QueueUtil.sendMessage;

import uk.gov.justice.json.schemas.core.Address;
import uk.gov.justice.json.schemas.core.CourtCentre;
import uk.gov.justice.json.schemas.core.Defendant;
import uk.gov.justice.json.schemas.core.HearingDay;
import uk.gov.justice.json.schemas.core.HearingType;
import uk.gov.justice.json.schemas.core.JudicialRole;
import uk.gov.justice.json.schemas.core.Person;
import uk.gov.justice.json.schemas.core.PersonDefendant;
import uk.gov.justice.json.schemas.core.ProsecutionCase;
import uk.gov.justice.services.common.converter.ZonedDateTimes;
import uk.gov.justice.services.test.utils.core.random.RandomGenerator;
import uk.gov.justice.json.schemas.core.Hearing;
import uk.gov.moj.cpp.hearing.command.hearingDetails.HearingDetailsUpdateCommand;
import uk.gov.moj.cpp.hearing.command.hearingDetails.Judge;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.HearingDetailsResponse;
import uk.gov.moj.cpp.hearing.test.CommandHelpers;
import uk.gov.moj.cpp.hearing.test.CommandHelpers.InitiateHearingCommandHelper;

import java.time.ZoneId;
import java.util.Collections;
import java.util.UUID;

import javax.json.Json;
import javax.json.JsonObject;
import javax.print.DocFlavor;

import org.junit.Test;

@SuppressWarnings("unchecked")
public class ChangeHearingDetailIT extends AbstractIT {

    @Test
    public void shouldUpdateHearing() throws Exception {

        final CommandHelpers.InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(requestSpec, standardInitiateHearingTemplate()));


        HearingDetailsUpdateCommand hearingDetailsUpdateCommand = UseCases.updateHearing(HearingDetailsUpdateCommand.hearingDetailsUpdateCommand()
                .setHearing(uk.gov.moj.cpp.hearing.command.hearingDetails.Hearing.hearing()
                        .setId(hearingOne.getHearingId())
                        .setCourtRoomId(randomUUID())
                        .setCourtRoomName(STRING.next())
                        .setType(STRING.next())
                        .setHearingDays(Collections.singletonList(PAST_ZONED_DATE_TIME.next()))
                        .setJudge(Judge.judge()
                                .setFirstName(STRING.next())
                                .setLastName(STRING.next())
                                .setId(randomUUID())
                                .setTitle(STRING.next())
                        )
                ));

        Queries.getHearingPollForMatch(hearingOne.getHearingId(), 30, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearingOne.getHearingId()))
                        .with(Hearing::getCourtCentre, isBean(CourtCentre.class)
                                .with(CourtCentre::getRoomId, is(hearingDetailsUpdateCommand.getHearing().getCourtRoomId()))
                                .with(CourtCentre::getRoomName, is(hearingDetailsUpdateCommand.getHearing().getCourtRoomName()))

                        )
                        .with(Hearing::getType, isBean(HearingType.class)
                                .with(HearingType::getDescription, is(hearingDetailsUpdateCommand.getHearing().getType()))
                        )
                        .with(Hearing::getHearingDays, first(isBean(HearingDay.class)
                                //TODO - look into why I need to hack the time zone here.
                                .with(HearingDay::getSittingDay, is(hearingDetailsUpdateCommand.getHearing().getHearingDays().get(0).withZoneSameLocal(ZoneId.of("UTC"))))
                        ))
                        .with(Hearing::getJudiciary, first(isBean(JudicialRole.class)
                                .with(JudicialRole::getFirstName, is(hearingDetailsUpdateCommand.getHearing().getJudge().getFirstName()))
                                .with(JudicialRole::getLastName, is(hearingDetailsUpdateCommand.getHearing().getJudge().getLastName()))
                                .with(JudicialRole::getTitle, is(hearingDetailsUpdateCommand.getHearing().getJudge().getTitle()))
                                .with(JudicialRole::getJudicialId, is(hearingDetailsUpdateCommand.getHearing().getJudge().getId()))
                        ))
                )
        );
    }

}
