package uk.gov.moj.cpp.hearing.command.handler;

import static java.util.Arrays.asList;
import static java.util.UUID.fromString;

import uk.gov.moj.cpp.hearing.domain.HearingEventDefinition;
import uk.gov.moj.cpp.hearing.domain.command.CreateHearingEventDefinitions;

import java.util.List;

public class HearingEventDefinitionsTestHelper {

    public static String HEARING_EVENT_DEFINITIONS_UUID  = "4daefec6-5f78-4109-82d9-1e60544a6c02";

    public static CreateHearingEventDefinitions createHearingEventDefinitions(String uuid) {
        List<HearingEventDefinition> hearingEventDefinitions = asList(
                new HearingEventDefinition("Identify defendant", "Defendant identified", 1),
                new HearingEventDefinition("Start Hearing", "Hearing started", 2));
        return new CreateHearingEventDefinitions(fromString(uuid), hearingEventDefinitions);
    }
}
