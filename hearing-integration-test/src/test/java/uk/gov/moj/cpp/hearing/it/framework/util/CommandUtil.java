package uk.gov.moj.cpp.hearing.it.framework.util;

import static uk.gov.moj.cpp.hearing.it.UseCases.initiateHearingWithoutWaitingFotEvent;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;

import uk.gov.justice.core.courts.HearingDay;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;

import java.time.ZonedDateTime;

import com.jayway.restassured.specification.RequestSpecification;

public class CommandUtil {

    public static void fireCommand(final int numberOfCommands, final RequestSpecification requestSpec) {
        for (int i = 0; i < numberOfCommands; i++) {
            InitiateHearingCommand initiateHearingCommand = standardInitiateHearingTemplate();
            HearingDay hearingDay = initiateHearingCommand.getHearing().getHearingDays().get(0);
            hearingDay.setSittingDay(ZonedDateTime.now().plusDays(1));
            initiateHearingWithoutWaitingFotEvent(requestSpec, initiateHearingCommand);
        }
    }
}
