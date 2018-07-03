package uk.gov.moj.cpp.hearing.event;

import com.google.common.collect.ImmutableMap;
import uk.gov.justice.services.test.utils.core.random.RandomGenerator;
import uk.gov.moj.cpp.hearing.command.result.CompletedResultLine;
import uk.gov.moj.cpp.hearing.command.result.CompletedResultLineStatus;
import uk.gov.moj.cpp.hearing.command.result.CourtClerk;
import uk.gov.moj.cpp.hearing.command.result.Level;
import uk.gov.moj.cpp.hearing.command.result.ResultPrompt;
import uk.gov.moj.cpp.hearing.domain.Plea;
import uk.gov.moj.cpp.hearing.domain.event.DefenceCounselUpsert;
import uk.gov.moj.cpp.hearing.domain.event.ProsecutionCounselUpsert;
import uk.gov.moj.cpp.hearing.domain.event.VerdictUpsert;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsShared;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.Material;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.NowResult;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.Nows;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.PromptRef;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.UserGroups;
import uk.gov.moj.cpp.hearing.test.CommandHelpers.InitiateHearingCommandHelper;

import java.util.List;
import java.util.UUID;

import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.BOOLEAN;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_LOCAL_DATE;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_ZONED_DATE_TIME;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;

public class NowsTemplates {

    private NowsTemplates() {
    }

    public static List<Nows> basicNowsTemplate() {
        return singletonList(
                Nows.nows()
                        .setDefendantId(randomUUID())
                        .setId(randomUUID())
                        .setNowsTemplateName(STRING.next())
                        .setMaterials(singletonList(Material.material()
                                        .setId(randomUUID())
                                        .setNowResult(singletonList(NowResult.nowResult()
                                                        .setSharedResultId(randomUUID())
                                                        .setSequence(123)
                                                        .setPrompts(singletonList(PromptRef.promptRef()
                                                                .setId(randomUUID())
                                                                .setLabel("label1"))
                                                        )
                                                )
                                        )
                                        .setUserGroups(singletonList(UserGroups.userGroups().setGroup("Listing Officer")))
                                )
                        )
                        .setNowsTypeId(randomUUID())

        );
    }

    public static ResultsShared resultsSharedTemplate() {

        InitiateHearingCommandHelper hearingOne = h(standardInitiateHearingTemplate());
        UUID completedResultLineId = randomUUID();
        return ResultsShared.builder()
                .withHearingId(hearingOne.getHearingId())
                .withSharedTime(PAST_ZONED_DATE_TIME.next())
                .withHearing(hearingOne.it().getHearing())
                .withCases(hearingOne.it().getCases())
                .withDefenceCounsels(ImmutableMap.of(randomUUID(), DefenceCounselUpsert.builder()
                        .withHearingId(hearingOne.getHearingId())
                        .withPersonId(randomUUID())
                        .withAttendeeId(randomUUID())
                        .withDefendantIds(singletonList(randomUUID()))
                        .withFirstName(STRING.next())
                        .withLastName(STRING.next())
                        .withStatus(STRING.next())
                        .withTitle(STRING.next())
                        .build()
                ))
                .withProsecutionCounsels(ImmutableMap.of(randomUUID(), ProsecutionCounselUpsert.builder()
                        .withAttendeeId(randomUUID())
                        .withHearingId(hearingOne.getHearingId())
                        .withPersonId(randomUUID())
                        .withFirstName(STRING.next())
                        .withLastName(STRING.next())
                        .withStatus(STRING.next())
                        .withTitle(STRING.next())
                        .build()
                ))
                .withPleas(ImmutableMap.of(randomUUID(), Plea.plea()
                        .setOffenceId(hearingOne.getFirstOffenceIdForFirstDefendant())
                        .setOriginHearingId(hearingOne.getHearingId())
                        .setPleaDate(PAST_LOCAL_DATE.next())
                        .setValue(STRING.next())
                ))
                .withVerdicts(ImmutableMap.of(randomUUID(), VerdictUpsert.builder()
                        .withVerdictId(randomUUID())
                        .withCategory(STRING.next())
                        .withCode(STRING.next())
                        .withDescription(STRING.next())
                        .withNumberOfJurors(RandomGenerator.integer(9, 12).next())
                        .withNumberOfSplitJurors(RandomGenerator.integer(0, 3).next())
                        .withVerdictDate(PAST_LOCAL_DATE.next())
                        .withVerdictValueId(randomUUID())
                        .withOffenceId(hearingOne.getFirstOffenceIdForFirstDefendant())
                        .withUnanimous(BOOLEAN.next())
                        .withHearingId(hearingOne.getHearingId())
                        .build()))
                .withCourtClerk(CourtClerk.builder()
                        .withId(randomUUID())
                        .withFirstName(STRING.next())
                        .withLastName(STRING.next())
                        .build())
                .withCompletedResultLines(singletonList(
                        CompletedResultLine.builder()
                                .withId(completedResultLineId)
                                .withCaseId(hearingOne.getFirstCaseId())
                                .withDefendantId(hearingOne.getFirstDefendantId())
                                .withOffenceId(hearingOne.getFirstOffenceIdForFirstDefendant())
                                .withLevel(Level.CASE)
                                .withResultDefinitionId(randomUUID())
                                .withResultLabel(STRING.next())
                                .withResultPrompts(singletonList(ResultPrompt.builder()
                                        .withId(randomUUID())
                                        .withLabel(STRING.next())
                                        .withValue(STRING.next())
                                        .build()))
                                .build()
                ))
                .withCompletedResultLinesStatus(ImmutableMap.of(completedResultLineId, CompletedResultLineStatus.builder()
                        .withCourtClerk(CourtClerk.builder()
                                .withId(randomUUID())
                                .withFirstName(STRING.next())
                                .withLastName(STRING.next())
                                .build())
                        .withId(completedResultLineId)
                        .withLastSharedDateTime(PAST_ZONED_DATE_TIME.next())
                        .build()
                ))
                .build();
    }
}
