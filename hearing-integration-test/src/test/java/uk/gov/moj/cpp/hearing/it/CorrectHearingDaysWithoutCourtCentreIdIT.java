package uk.gov.moj.cpp.hearing.it;

import static java.time.ZonedDateTime.now;
import static java.util.Collections.emptyList;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.hamcrest.CoreMatchers.is;
import static uk.gov.justice.core.courts.HearingLanguage.ENGLISH;
import static uk.gov.justice.core.courts.JurisdictionType.CROWN;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.CoreTestTemplates.DefendantType.PERSON;
import static uk.gov.moj.cpp.hearing.test.CoreTestTemplates.defaultArguments;
import static uk.gov.moj.cpp.hearing.test.CoreTestTemplates.hearingDay;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.asList;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.utils.RestUtils.DEFAULT_POLL_TIMEOUT_IN_SEC;

import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.HearingDay;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.HearingDetailsResponse;
import uk.gov.moj.cpp.hearing.test.CommandHelpers;
import uk.gov.moj.cpp.hearing.test.CoreTestTemplates;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import javax.annotation.concurrent.NotThreadSafe;

import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.Test;

@NotThreadSafe
public class CorrectHearingDaysWithoutCourtCentreIdIT extends AbstractIT {

    private static final int WAIT_TIME = 3;

    @Test
    public void shouldCorrectHearingDaysWithoutCourtCentreId() throws Exception {

        final HearingDay hd1 = hearingDay(now(), 1).withCourtCentreId(null).withCourtRoomId(null).build();
        final HearingDay hd2 = hearingDay(now().plusDays(1), 2).withCourtCentreId(null).withCourtRoomId(null).build();

        List<HearingDay> hd = asList(hd1, hd2);

        final CommandHelpers.InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(getRequestSpec(),
                InitiateHearingCommand.initiateHearingCommand()
                        .setHearing(CoreTestTemplates.hearing(defaultArguments()
                                .setPutCustodialEstablishment(false)
                                .setDefendantType(PERSON)
                                .setHearingLanguage(ENGLISH)
                                .setJurisdictionType(CROWN)
                        ).withHearingDays(hd).build())));

        // Check no courtCentreID and courtRoomId is set
        Queries.getHearingPollForMatch(hearingOne.getHearingId(), DEFAULT_POLL_TIMEOUT_IN_SEC, WAIT_TIME, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearingOne.getHearingId()))

                        .with(he -> he.getHearingDays().stream().map(HearingDay::getCourtCentreId).filter(Objects::nonNull).collect(toList()), is(emptyList()))
                        .with(he -> he.getHearingDays().stream().map(HearingDay::getCourtRoomId).filter(Objects::nonNull).collect(toList()), is(emptyList()))
                ));

        final UUID courtCentreId = randomUUID();
        final UUID courtRoomId = randomUUID();

        hd.forEach(d -> {
            d.setCourtCentreId(courtCentreId);
            d.setCourtRoomId(courtRoomId);
        });

        UseCases.correctHearingDaysWithoutCourtCentre(getRequestSpec(), hearingOne.getHearingId(), hd);

        //courtCentreID and courtRoomId is set
        Queries.getHearingPollForMatch(hearingOne.getHearingId(), DEFAULT_POLL_TIMEOUT_IN_SEC, WAIT_TIME, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearingOne.getHearingId()))

                        .with(he -> he.getHearingDays().stream().map(HearingDay::getCourtCentreId).collect(toSet()), is(ImmutableSet.of(courtCentreId)))
                        .with(he -> he.getHearingDays().stream().map(HearingDay::getCourtRoomId).collect(toSet()), is(ImmutableSet.of(courtRoomId)))
                ));
    }

}