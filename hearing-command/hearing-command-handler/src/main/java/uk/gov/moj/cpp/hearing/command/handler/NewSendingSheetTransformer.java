package uk.gov.moj.cpp.hearing.command.handler;

import uk.gov.moj.cpp.external.domain.progression.sendingsheetcompleted.PleaValue;
import uk.gov.moj.cpp.hearing.command.initiate.Address;
import uk.gov.moj.cpp.hearing.command.initiate.Case;
import uk.gov.moj.cpp.hearing.command.initiate.Defendant;
import uk.gov.moj.cpp.hearing.command.initiate.Hearing;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.command.initiate.Interpreter;
import uk.gov.moj.cpp.hearing.command.initiate.Judge;
import uk.gov.moj.cpp.hearing.command.initiate.Offence;
import uk.gov.moj.cpp.hearing.command.plea.HearingUpdatePleaCommand;

import java.time.ZonedDateTime;
import java.util.UUID;

import static java.util.UUID.randomUUID;

public class NewSendingSheetTransformer {


    public HearingUpdatePleaCommand mapToUpdatePleaCommands(uk.gov.moj.cpp.external.domain.progression.sendingsheetcompleted.Hearing hearing,
                                                            UUID hearingId) {

        HearingUpdatePleaCommand.Builder hearingUpdatePleaCommand = HearingUpdatePleaCommand.builder()
                .withCaseId(hearing.getCaseId())
                .withHearingId(hearingId);

        for (uk.gov.moj.cpp.external.domain.progression.sendingsheetcompleted.Defendant defendant : hearing.getDefendants()) {

            uk.gov.moj.cpp.hearing.command.plea.Defendant.Builder defendantBuilder = uk.gov.moj.cpp.hearing.command.plea.Defendant.builder()
                    .withId(defendant.getId())
                    .withPersonId(defendant.getPersonId());

            hearingUpdatePleaCommand.addDefendant(defendantBuilder);


            for (uk.gov.moj.cpp.external.domain.progression.sendingsheetcompleted.Offence offence : defendant.getOffences()) {

                if (offence.getPlea() == null || offence.getPlea().getValue() == PleaValue.NOT_GUILTY) {
                    continue;
                }

                defendantBuilder.addOffence(uk.gov.moj.cpp.hearing.command.plea.Offence.builder()
                        .withId(offence.getId())
                        .withPlea(uk.gov.moj.cpp.hearing.command.plea.Plea.builder()
                                .withId(offence.getId())
                                .withPleaDate(offence.getPlea().getPleaDate())
                                .withValue(offence.getPlea().getValue().toString())
                        )
                );

            }
        }

        return hearingUpdatePleaCommand.build();
    }


    public InitiateHearingCommand transform(final uk.gov.moj.cpp.external.domain.progression.sendingsheetcompleted.Hearing sHearing, UUID hearingId) {

        Hearing.Builder hearingBuilder = Hearing.builder()
                //TODO get this form plea date ?
                //TODO are we adding in hearings with no guilty plea ?
                .withStartDateTime(ZonedDateTime.now())
                //TODO is this ok ?
                .withId(hearingId)
                //TODO make this non mandatory
                .withCourtRoomId(randomUUID())
                //TODO make this non mandatory
                .withCourtRoomName("unkown court room name")
                .withCourtCentreId(sHearing.getCourtCentreId() == null ? null : UUID.fromString(sHearing.getCourtCentreId()))
                .withCourtCentreName(sHearing.getCourtCentreName())
                //TODO resolve assumption here ?!
                .withEstimateMinutes(20)
                //TODO resolve assumption here ?!
                .withNotBefore(false)
                .withType(sHearing.getType())
                //TODO make this non mandatory
                .withJudge(Judge.builder()
                        .withId(randomUUID())
                        .withLastName("unknownJudgeLastName")
                        .withFirstName("unknownJudgeFirstName")
                        .withTitle("Honourable"));

        sHearing.getDefendants().forEach(
                d -> {
                    Defendant.Builder dBuilder = Defendant.builder()
                            .withDateOfBirth(d.getDateOfBirth())
                            .withDefenceOrganisation(d.getDefenceOrganisation())
                            .withFirstName(d.getFirstName())
                            .withGender(d.getGender())
                            //TODO remove mandatory constraint
                            .withPersonId(d.getPersonId())
                            .withId(d.getId())
                            .withLastName(d.getLastName())
                            .withNationality(d.getNationality());

                    if (d.getInterpreter() != null) {
                        dBuilder.withInterpreter(Interpreter.builder()
                                .withLanguage(d.getInterpreter().getLanguage())
                                .withNeeded(d.getInterpreter().getNeeded()));
                    }

                    if (d.getAddress() != null) {
                        dBuilder.withAddress(Address.builder()
                                .withPostCode(d.getAddress().getPostcode())
                                .withAddress1(d.getAddress().getAddress1())
                                .withAddress2(d.getAddress().getAddress2())
                                .withAddress3(d.getAddress().getAddress3())
                                .withAddress4(d.getAddress().getAddress4())
                        );
                    }
                    hearingBuilder.addDefendant(dBuilder);
                    d.getOffences().forEach(
                            o -> {
                                dBuilder.addOffence(Offence.builder()
                                                .withId(o.getId())
                                                .withOffenceCode(o.getOffenceCode())
                                                .withCaseId(sHearing.getCaseId())
                                                .withStartDate(sHearing.getSendingCommittalDate())
                                                .withWording(o.getWording())
                                                .withConvictionDate(o.getConvictionDate())
                                        // TODO count N/A
                                        //.withCount(o.getCount)

                                );
                            }
                    );
                }

        );

        return InitiateHearingCommand.builder()
                .withHearing(hearingBuilder)
                .addCase(Case.builder().withCaseId(sHearing.getCaseId()).withUrn(sHearing.getCaseUrn()))
                .build();
    }


}
