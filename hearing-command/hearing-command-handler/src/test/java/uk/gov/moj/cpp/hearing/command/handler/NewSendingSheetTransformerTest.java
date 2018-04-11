package uk.gov.moj.cpp.hearing.command.handler;

import org.junit.Assert;
import org.junit.Test;
import uk.gov.moj.cpp.external.domain.progression.sendingsheetcompleted.Defendant;
import uk.gov.moj.cpp.external.domain.progression.sendingsheetcompleted.Hearing;
import uk.gov.moj.cpp.external.domain.progression.sendingsheetcompleted.Offence;
import uk.gov.moj.cpp.external.domain.progression.sendingsheetcompleted.Plea;
import uk.gov.moj.cpp.external.domain.progression.sendingsheetcompleted.PleaValue;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.command.plea.HearingUpdatePleaCommand;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class NewSendingSheetTransformerTest {

    @Test
    public void testTransform() {
        Hearing hearing = (new Hearing.Builder())
                .withCaseId(UUID.randomUUID())
                .withDefendants(Arrays.asList(
                        new Defendant.Builder().withOffences(
                                Arrays.asList(
                                        new Offence.Builder().withId(UUID.randomUUID()).build()
                                )
                        )
                                .withId(UUID.randomUUID())
                                .build()

                ))
                .build();
        UUID hearingId = UUID.randomUUID();
        InitiateHearingCommand command = (new NewSendingSheetTransformer()).transform(hearing, hearingId);
        assertNotNull(command.getHearing());
        List<uk.gov.moj.cpp.hearing.command.initiate.Defendant> defendants = command.getHearing().getDefendants();
        assertNotNull(defendants.get(0).getOffences());
        assertEquals(1, defendants.get(0).getOffences().size());
        uk.gov.moj.cpp.hearing.command.initiate.Offence offence = defendants.get(0).getOffences().get(0);
        assertNotNull(offence.getCaseId());
        assertEquals(command.getCases().size(), 1);
        assertEquals(command.getHearing().getDefendants().get(0).getId(),
                hearing.getDefendants().get(0).getId());
        assertEquals(command.getHearing().getDefendants().get(0).getOffences().get(0).getId(),
                hearing.getDefendants().get(0).getOffences().get(0).getId());


    }

    @Test
    public void testTransformPleas() {
        Hearing hearing = (new Hearing.Builder())
                .withCaseId(UUID.randomUUID())
                .withDefendants(Arrays.asList(
                        new Defendant.Builder().withOffences(
                                Arrays.asList(
                                        new Offence.Builder().withId(UUID.randomUUID())
                                                .withPlea(
                                                        new Plea.Builder()
                                                                .withPleaDate(LocalDate.now())
                                                                .withPleaValue(PleaValue.GUILTY)
                                                                .build()
                                                ).build()
                                )
                        )
                                .withId(UUID.randomUUID())
                                .build()

                ))
                .build();
        UUID hearingId = UUID.randomUUID();
        HearingUpdatePleaCommand hearingUpdatePleaCommand = new NewSendingSheetTransformer().mapToUpdatePleaCommands(hearing, hearingId);
        uk.gov.moj.cpp.hearing.command.plea.Offence offence = hearingUpdatePleaCommand.getDefendants().get(0).getOffences().get(0);

        assertEquals(hearingUpdatePleaCommand.getHearingId(), hearingId);
        assertEquals(hearingUpdatePleaCommand.getCaseId(), hearing.getCaseId());
        assertEquals(hearingUpdatePleaCommand.getDefendants().get(0).getId(), hearing.getDefendants().get(0).getId());
        assertEquals(offence.getId(), hearing.getDefendants().get(0).getOffences().get(0).getId());
        assertEquals(offence.getPlea().getPleaDate(), hearing.getDefendants().get(0).getOffences().get(0).getPlea().getPleaDate());
        assertEquals(offence.getPlea().getValue(), hearing.getDefendants().get(0).getOffences().get(0).getPlea().getValue().toString());

    }

}
