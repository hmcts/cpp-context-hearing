package uk.gov.moj.cpp.hearing.event;

import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.BOOLEAN;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.INTEGER;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_LOCAL_DATE;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_ZONED_DATE_TIME;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.integer;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.VariantDirectoryTemplates.standardVariantTemplate;

import com.google.common.collect.ImmutableMap;
import uk.gov.justice.json.schemas.core.CourtClerk;
import uk.gov.justice.json.schemas.core.Plea;
import uk.gov.justice.json.schemas.core.PleaValue;
import uk.gov.justice.services.test.utils.core.random.RandomGenerator;
import uk.gov.moj.cpp.hearing.command.result.CompletedResultLineStatus;
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
import uk.gov.moj.cpp.hearing.test.CoreTestTemplates;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

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

        hearingOne.it().getHearing().setTargets(new ArrayList<>(Collections.singletonList(
                CoreTestTemplates.target(hearingOne.getHearingId(), hearingOne.getFirstDefendantForFirstCase().getId(), hearingOne.getFirstOffenceIdForFirstDefendant(), completedResultLineId).build()
        )));

        final VerdictUpsert verdictUpsert = VerdictUpsert.verdictUpsert()
                .setHearingId(hearingOne.getFirstOffenceIdForFirstDefendant())
                .setVerdict(uk.gov.justice.json.schemas.core.Verdict.verdict()
                        .withVerdictDate(PAST_LOCAL_DATE.next())
                        .withOffenceId(hearingOne.getFirstOffenceIdForFirstDefendant())
                        .withOriginatingHearingId(randomUUID())
                        .withJurors(
                                uk.gov.justice.json.schemas.core.Jurors.jurors()
                                        .withNumberOfJurors(integer(9, 12).next())
                                        .withNumberOfSplitJurors(integer(0, 3).next())
                                        .withUnanimous(BOOLEAN.next())
                                        .build())
                        .withVerdictType(
                                uk.gov.justice.json.schemas.core.VerdictType.verdictType()
                                        .withVerdictTypeId(randomUUID())
                                        .withCategoryType(STRING.next())
                                        .withCategory(STRING.next())
                                        .withDescription(STRING.next())
                                        .withSequence(INTEGER.next())
                                        .build())
                        .withLesserOrAlternativeOffence(uk.gov.justice.json.schemas.core.LesserOrAlternativeOffence.lesserOrAlternativeOffence()
                                .withOffenceLegislationWelsh(STRING.next())
                                .withOffenceLegislation(STRING.next())
                                .withOffenceTitleWelsh(STRING.next())
                                .withOffenceTitle(STRING.next())
                                .withOffenceCode(STRING.next())
                                .withOffenceDefinitionId(randomUUID())
                                .build())
                        .build());

        return ResultsShared.builder()
                .withHearingId(hearingOne.getHearingId())
                .withSharedTime(PAST_ZONED_DATE_TIME.next().withZoneSameInstant(ZoneId.of("UTC")))
                .withHearing(hearingOne.getHearing())
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
                        .withOffenceId(hearingOne.getFirstOffenceIdForFirstDefendant())
                        .withOriginatingHearingId(hearingOne.getHearingId())
                        .withPleaDate(PAST_LOCAL_DATE.next())
                        .withPleaValue(RandomGenerator.values(PleaValue.values()).next())
                        .build()
                ))
                .withVerdicts(ImmutableMap.of(randomUUID(), verdictUpsert))
                .withCourtClerk(CourtClerk.courtClerk()
                        .withId(randomUUID())
                        .withFirstName(STRING.next())
                        .withLastName(STRING.next())
                        .build())
                .withCompletedResultLinesStatus(ImmutableMap.of(completedResultLineId, CompletedResultLineStatus.builder()
                        .withCourtClerk(uk.gov.justice.json.schemas.core.CourtClerk.courtClerk()
                                .withId(randomUUID())
                                .withFirstName(STRING.next())
                                .withLastName(STRING.next())
                                .build())
                        .withId(completedResultLineId)
                        .withLastSharedDateTime(PAST_ZONED_DATE_TIME.next().withZoneSameInstant(ZoneId.of("UTC")))
                        .build()
                ))
                .withVariantDirectory(singletonList(
                        standardVariantTemplate(randomUUID(), hearingOne.getHearingId(), hearingOne.getFirstDefendantForFirstCase().getId())
                ))
                .build();
    }
}
