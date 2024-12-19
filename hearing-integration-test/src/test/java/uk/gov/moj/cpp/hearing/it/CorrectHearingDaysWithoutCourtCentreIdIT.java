package uk.gov.moj.cpp.hearing.it;

import static java.time.ZonedDateTime.now;
import static java.util.Collections.emptyList;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.hamcrest.CoreMatchers.is;
import static uk.gov.justice.core.courts.HearingLanguage.ENGLISH;
import static uk.gov.justice.core.courts.JurisdictionType.CROWN;
import static uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand.initiateHearingCommand;
import static uk.gov.moj.cpp.hearing.it.Queries.getHearingPollForMatch;
import static uk.gov.moj.cpp.hearing.it.UseCases.correctHearingDaysWithoutCourtCentre;
import static uk.gov.moj.cpp.hearing.it.UseCases.initiateHearing;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.CoreTestTemplates.DefendantType.PERSON;
import static uk.gov.moj.cpp.hearing.test.CoreTestTemplates.defaultArguments;
import static uk.gov.moj.cpp.hearing.test.CoreTestTemplates.hearingDay;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.asList;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;

import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.HearingDay;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.HearingDetailsResponse;
import uk.gov.moj.cpp.hearing.test.CommandHelpers.InitiateHearingCommandHelper;
import uk.gov.moj.cpp.hearing.test.CoreTestTemplates;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import javax.annotation.concurrent.NotThreadSafe;

import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.Test;

@NotThreadSafe
public class CorrectHearingDaysWithoutCourtCentreIdIT extends AbstractIT {

    @Test
    public void shouldCorrectHearingDaysWithoutCourtCentreId() {

        final HearingDay hd1 = hearingDay(now(), 1).withCourtCentreId(null).withCourtRoomId(null).build();
        final HearingDay hd2 = hearingDay(now().plusDays(1), 2).withCourtCentreId(null).withCourtRoomId(null).build();

        List<HearingDay> hd = asList(hd1, hd2);

        final InitiateHearingCommandHelper hearingOne = h(initiateHearing(getRequestSpec(),
                initiateHearingCommand()
                        .setHearing(CoreTestTemplates.hearing(defaultArguments()
                                .setPutCustodialEstablishment(false)
                                .setDefendantType(PERSON)
                                .setHearingLanguage(ENGLISH)
                                .setJurisdictionType(CROWN)
                        ).withHearingDays(hd).build())));

        // Check no courtCentreID and courtRoomId is set
        final UUID hearingId = hearingOne.getHearingId();
        getHearingPollForMatch(hearingId, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearingId))
                        .with(he -> he.getHearingDays().stream().map(HearingDay::getCourtCentreId).filter(Objects::nonNull).collect(toList()), is(emptyList()))
                        .with(he -> he.getHearingDays().stream().map(HearingDay::getCourtRoomId).filter(Objects::nonNull).collect(toList()), is(emptyList()))
                ));

        final UUID courtCentreId = randomUUID();
        final UUID courtRoomId = randomUUID();

        hd.forEach(d -> {
            d.setCourtCentreId(courtCentreId);
            d.setCourtRoomId(courtRoomId);
        });

        correctHearingDaysWithoutCourtCentre(getRequestSpec(), hearingId, hd);

        //courtCentreID and courtRoomId is set
        getHearingPollForMatch(hearingId, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearingId))
                        .with(he -> he.getHearingDays().stream().map(HearingDay::getCourtCentreId).collect(toSet()), is(ImmutableSet.of(courtCentreId)))
                        .with(he -> he.getHearingDays().stream().map(HearingDay::getCourtRoomId).collect(toSet()), is(ImmutableSet.of(courtRoomId)))
                ));
    }

}