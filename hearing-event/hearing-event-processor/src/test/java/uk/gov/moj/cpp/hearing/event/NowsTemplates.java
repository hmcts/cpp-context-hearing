package uk.gov.moj.cpp.hearing.event;

import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.codehaus.groovy.runtime.InvokerHelper.asList;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.BOOLEAN;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.INTEGER;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_LOCAL_DATE;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_ZONED_DATE_TIME;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.integer;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.VariantDirectoryTemplates.standardVariantTemplate;

import uk.gov.justice.core.courts.DelegatedPowers;
import uk.gov.justice.core.courts.Now;
import uk.gov.justice.core.courts.NowVariant;
import uk.gov.justice.core.courts.NowVariantKey;
import uk.gov.justice.core.courts.NowVariantResult;
import uk.gov.justice.core.courts.Target;
import uk.gov.moj.cpp.hearing.command.result.CompletedResultLineStatus;
import uk.gov.moj.cpp.hearing.domain.event.VerdictUpsert;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsShared;
import uk.gov.moj.cpp.hearing.test.CommandHelpers.InitiateHearingCommandHelper;
import uk.gov.moj.cpp.hearing.test.CoreTestTemplates;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.google.common.collect.ImmutableMap;

public class NowsTemplates {

    private NowsTemplates() {
    }

    public static List<Now> basicNowsTemplate() {
        final UUID defendantId = randomUUID();
        final UUID nowsTypeId = randomUUID();
        return singletonList(
                Now.now()
                        .withDefendantId(defendantId)
                        .withId(randomUUID())
                        .withRequestedMaterials(singletonList(NowVariant.nowVariant()
                                        .withMaterialId(UUID.randomUUID())
                                        .withKey(
                                                NowVariantKey.nowVariantKey()
                                                        .withUsergroups(singletonList("Listing Officer"))
                                                        .withNowsTypeId(nowsTypeId)
                                                        .withHearingId(randomUUID())
                                                        .withDefendantId(defendantId)
                                                        .build()
                                        )
                                        .withNowResults(singletonList(NowVariantResult.nowVariantResult()
                                                        .withSharedResultId(randomUUID())
                                                        .withSequence(123)
                                                        .withPromptRefs(singletonList(randomUUID()))
                                                        .build()
                                                )
                                        )
                                        .build()
                                )
                        )
                        .withNowsTypeId(nowsTypeId)
                        .build()
        );
    }

    public static ResultsShared resultsSharedTemplate() {

        InitiateHearingCommandHelper hearingOne = h(standardInitiateHearingTemplate());

        UUID completedResultLineId = randomUUID();

        final List<Target> targets = new ArrayList<>(asList(
                CoreTestTemplates.target(hearingOne.getHearingId(), hearingOne.getFirstDefendantForFirstCase().getId(), hearingOne.getFirstOffenceIdForFirstDefendant(), completedResultLineId).build()
        ));

        final VerdictUpsert verdictUpsert = VerdictUpsert.verdictUpsert()
                .setHearingId(hearingOne.getFirstOffenceIdForFirstDefendant())
                .setVerdict(uk.gov.justice.core.courts.Verdict.verdict()
                        .withVerdictDate(PAST_LOCAL_DATE.next())
                        .withOffenceId(hearingOne.getFirstOffenceIdForFirstDefendant())
                        .withOriginatingHearingId(randomUUID())
                        .withJurors(
                                uk.gov.justice.core.courts.Jurors.jurors()
                                        .withNumberOfJurors(integer(9, 12).next())
                                        .withNumberOfSplitJurors(integer(0, 3).next())
                                        .withUnanimous(BOOLEAN.next())
                                        .build())
                        .withVerdictType(
                                uk.gov.justice.core.courts.VerdictType.verdictType()
                                        .withId(randomUUID())
                                        .withCategoryType(STRING.next())
                                        .withCategory(STRING.next())
                                        .withDescription(STRING.next())
                                        .withSequence(INTEGER.next())
                                        .build())
                        .withLesserOrAlternativeOffence(uk.gov.justice.core.courts.LesserOrAlternativeOffence.lesserOrAlternativeOffence()
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
                .withTargets(targets)
                .withSharedTime(PAST_ZONED_DATE_TIME.next().withZoneSameInstant(ZoneId.of("UTC")))
                .withHearing(hearingOne.getHearing())
                .withCourtClerk(DelegatedPowers.delegatedPowers()
                        .withUserId(randomUUID())
                        .withFirstName(STRING.next())
                        .withLastName(STRING.next())
                        .build())
                .withCompletedResultLinesStatus(ImmutableMap.of(completedResultLineId, CompletedResultLineStatus.builder()
                        .withCourtClerk(DelegatedPowers.delegatedPowers()
                                .withUserId(randomUUID())
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

    public static ResultsShared resultsSharedTemplateForDocumentIsDeleted() {

        InitiateHearingCommandHelper hearingOne = h(standardInitiateHearingTemplate());

        UUID completedResultLineId = randomUUID();

        final List<Target> targets = new ArrayList<>(asList(
                CoreTestTemplates.targetForDocumentIsDeleted(hearingOne.getHearingId(), hearingOne.getFirstDefendantForFirstCase().getId(), hearingOne.getFirstOffenceIdForFirstDefendant(), completedResultLineId).build()
        ));

        return ResultsShared.builder()
                .withHearingId(hearingOne.getHearingId())
                .withTargets(targets)
                .withSharedTime(PAST_ZONED_DATE_TIME.next().withZoneSameInstant(ZoneId.of("UTC")))
                .withHearing(hearingOne.getHearing())
                .withCourtClerk(DelegatedPowers.delegatedPowers()
                        .withUserId(randomUUID())
                        .withFirstName(STRING.next())
                        .withLastName(STRING.next())
                        .build())
                .withCompletedResultLinesStatus(ImmutableMap.of(completedResultLineId, CompletedResultLineStatus.builder()
                        .withCourtClerk(DelegatedPowers.delegatedPowers()
                                .withUserId(randomUUID())
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

    public static ResultsShared resultsSharedTemplateForSendingResultSharedForOffence(final UUID resultDefinitionId) {
        InitiateHearingCommandHelper hearingOne = h(standardInitiateHearingTemplate());


        UUID completedResultLineId = randomUUID();

        final List<Target> targets = asList(
                CoreTestTemplates.targetForOffenceResultShared(hearingOne.getHearingId(), hearingOne.getFirstDefendantForFirstCase().getId(), hearingOne.getFirstOffenceIdForFirstDefendant(), completedResultLineId, resultDefinitionId).build()
        );

        return ResultsShared.builder()
                .withHearingId(hearingOne.getHearingId())
                .withTargets(targets)
                .withSharedTime(PAST_ZONED_DATE_TIME.next().withZoneSameInstant(ZoneId.of("UTC")))
                .withHearing(hearingOne.getHearing())
                .withCourtClerk(DelegatedPowers.delegatedPowers()
                        .withUserId(randomUUID())
                        .withFirstName(STRING.next())
                        .withLastName(STRING.next())
                        .build())
                .withCompletedResultLinesStatus(ImmutableMap.of(completedResultLineId, CompletedResultLineStatus.builder()
                        .withCourtClerk(DelegatedPowers.delegatedPowers()
                                .withUserId(randomUUID())
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
