package uk.gov.moj.cpp.hearing.event.nows;

import org.junit.Test;
import uk.gov.moj.cpp.hearing.command.result.CompletedResultLine;
import uk.gov.moj.cpp.hearing.command.result.CompletedResultLineStatus;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.NowDefinition;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.ResultDefinitions;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_ZONED_DATE_TIME;
import static uk.gov.moj.cpp.hearing.command.nowsdomain.variants.ResultLineReference.resultLineReference;
import static uk.gov.moj.cpp.hearing.command.nowsdomain.variants.Variant.variant;
import static uk.gov.moj.cpp.hearing.command.nowsdomain.variants.VariantKey.variantKey;
import static uk.gov.moj.cpp.hearing.command.nowsdomain.variants.VariantValue.variantValue;

public class GenerateVariantDecisionMakerTest {

    @Test
    public void decide_toNotGenerateTheVariantBecauseNoResultLinesHaveChangedSinceLast() {
        UUID defendantId = randomUUID();
        UUID resultLineId = randomUUID();

        NowDefinition nowDefinition = NowDefinition.now()
                .setId(randomUUID())
                .setResultDefinitions(singletonList(ResultDefinitions.resultDefinitions().setId(randomUUID())));


        ZonedDateTime lastSharedTime = PAST_ZONED_DATE_TIME.next();
        List<String> usergroups = asList("Listing Officer", "Court Clerk");

        GenerateVariantDecisionMaker generateVariantDecisionMaker = new GenerateVariantDecisionMakerFactory()
                .setCompletedResultLines(singletonList(
                        CompletedResultLine.builder()
                                .withId(resultLineId)
                                .withDefendantId(defendantId)
                                .withResultDefinitionId(nowDefinition.getResultDefinitions().get(0).getId())
                                .build()
                        )
                )
                .setCompletedResultLineStatuses(
                        Stream.of(
                                CompletedResultLineStatus.builder()
                                        .withId(resultLineId)
                                        .withLastSharedDateTime(lastSharedTime)
                                        .build()
                        )
                                .collect(Collectors.toMap(CompletedResultLineStatus::getId, Function.identity()))
                )
                .setVariantDirectory(singletonList(variant()
                                .setKey(variantKey()
                                        .setDefendantId(defendantId)
                                        .setNowsTypeId(nowDefinition.getId())
                                        .setUsergroups(usergroups)
                                )
                                .setValue(variantValue()
                                        .setResultLines(singletonList(resultLineReference()
                                                .setLastSharedTime(lastSharedTime)
                                                .setResultLineId(resultLineId)
                                        ))
                                )
                        )
                )
                .buildFor(defendantId, nowDefinition);

        GenerateVariantDecisionMaker.Decision decision = generateVariantDecisionMaker.decide(usergroups);

        assertThat(decision.isShouldGenerate(), is(false));
        assertThat(decision.isAmended(), is(false));
    }

    @Test
    public void decide_toGenerateTheVariantHasNotBeenGeneratedBefore() {
        UUID defendantId = randomUUID();
        UUID resultLineId = randomUUID();

        NowDefinition nowDefinition = NowDefinition.now()
                .setId(randomUUID())
                .setResultDefinitions(singletonList(ResultDefinitions.resultDefinitions().setId(randomUUID())));


        ZonedDateTime lastSharedTime = PAST_ZONED_DATE_TIME.next();
        List<String> usergroups = asList("Listing Officer", "Court Clerk");

        GenerateVariantDecisionMaker generateVariantDecisionMaker = new GenerateVariantDecisionMakerFactory()
                .setCompletedResultLines(singletonList(
                        CompletedResultLine.builder()
                                .withId(resultLineId)
                                .withDefendantId(defendantId)
                                .withResultDefinitionId(nowDefinition.getResultDefinitions().get(0).getId())
                                .build()
                        )
                )
                .setCompletedResultLineStatuses(
                        Stream.of(
                                CompletedResultLineStatus.builder()
                                        .withId(resultLineId)
                                        .withLastSharedDateTime(lastSharedTime)
                                        .build()
                        )
                                .collect(Collectors.toMap(CompletedResultLineStatus::getId, Function.identity()))
                )
                .setVariantDirectory(emptyList())
                .buildFor(defendantId, nowDefinition);

        GenerateVariantDecisionMaker.Decision decision = generateVariantDecisionMaker.decide(usergroups);

        assertThat(decision.isShouldGenerate(), is(true));
        assertThat(decision.isAmended(), is(false));
    }

    @Test
    public void decide_toGenerateBecauseWeHaveAChangedResultLine() {
        UUID defendantId = randomUUID();
        UUID resultLineId = randomUUID();

        NowDefinition nowDefinition = NowDefinition.now()
                .setId(randomUUID())
                .setResultDefinitions(singletonList(ResultDefinitions.resultDefinitions().setId(randomUUID())));

        List<String> usergroups = asList("Listing Officer", "Court Clerk");

        GenerateVariantDecisionMaker generateVariantDecisionMaker = new GenerateVariantDecisionMakerFactory()
                .setCompletedResultLines(singletonList(
                        CompletedResultLine.builder()
                                .withId(resultLineId)
                                .withDefendantId(defendantId)
                                .withResultDefinitionId(nowDefinition.getResultDefinitions().get(0).getId())
                                .build()
                        )
                )
                .setCompletedResultLineStatuses(
                        Stream.of(
                                CompletedResultLineStatus.builder()
                                        .withId(resultLineId)
                                        .withLastSharedDateTime(PAST_ZONED_DATE_TIME.next())
                                        .build()
                        )
                                .collect(Collectors.toMap(CompletedResultLineStatus::getId, Function.identity()))
                )
                .setVariantDirectory(singletonList(variant()
                                .setKey(variantKey()
                                        .setDefendantId(defendantId)
                                        .setNowsTypeId(nowDefinition.getId())
                                        .setUsergroups(usergroups)
                                )
                                .setValue(variantValue()
                                        .setResultLines(singletonList(resultLineReference()
                                                .setLastSharedTime(PAST_ZONED_DATE_TIME.next())
                                                .setResultLineId(resultLineId)
                                        ))
                                )
                        )
                )
                .buildFor(defendantId, nowDefinition);

        GenerateVariantDecisionMaker.Decision decision = generateVariantDecisionMaker.decide(usergroups);

        assertThat(decision.isShouldGenerate(), is(true));
        assertThat(decision.isAmended(), is(true));
    }

    @Test
    public void decide_toGenerateBecauseWeHaveANewResultLine() {
        UUID defendantId = randomUUID();
        UUID resultLineId = randomUUID();

        NowDefinition nowDefinition = NowDefinition.now()
                .setId(randomUUID())
                .setResultDefinitions(singletonList(ResultDefinitions.resultDefinitions().setId(randomUUID())));

        List<String> usergroups = asList("Listing Officer", "Court Clerk");

        GenerateVariantDecisionMaker generateVariantDecisionMaker = new GenerateVariantDecisionMakerFactory()
                .setCompletedResultLines(singletonList(
                        CompletedResultLine.builder()
                                .withId(resultLineId)
                                .withDefendantId(defendantId)
                                .withResultDefinitionId(nowDefinition.getResultDefinitions().get(0).getId())
                                .build()
                        )
                )
                .setCompletedResultLineStatuses(emptyMap())
                .setVariantDirectory(emptyList())
                .buildFor(defendantId, nowDefinition);

        GenerateVariantDecisionMaker.Decision decision = generateVariantDecisionMaker.decide(usergroups);

        assertThat(decision.isShouldGenerate(), is(true));
        assertThat(decision.isAmended(), is(false));
    }

    @Test
    public void decide_toGenerateBecauseWeHaveANewResultLineForAVariantPreviouslyGenerated() {
        UUID defendantId = randomUUID();
        UUID resultLineId = randomUUID();

        NowDefinition nowDefinition = NowDefinition.now()
                .setId(randomUUID())
                .setResultDefinitions(singletonList(ResultDefinitions.resultDefinitions().setId(randomUUID())));

        List<String> usergroups = asList("Listing Officer", "Court Clerk");

        GenerateVariantDecisionMaker generateVariantDecisionMaker = new GenerateVariantDecisionMakerFactory()
                .setCompletedResultLines(singletonList(
                        CompletedResultLine.builder()
                                .withId(resultLineId)
                                .withDefendantId(defendantId)
                                .withResultDefinitionId(nowDefinition.getResultDefinitions().get(0).getId())
                                .build()
                        )
                )
                .setCompletedResultLineStatuses(emptyMap())
                .setVariantDirectory(singletonList(variant()
                                .setKey(variantKey()
                                        .setDefendantId(defendantId)
                                        .setNowsTypeId(nowDefinition.getId())
                                        .setUsergroups(usergroups)
                                )
                                .setValue(variantValue()
                                        .setResultLines(singletonList(resultLineReference()
                                                .setLastSharedTime(PAST_ZONED_DATE_TIME.next())
                                                .setResultLineId(randomUUID())
                                        ))
                                )
                        )
                )
                .buildFor(defendantId, nowDefinition);

        GenerateVariantDecisionMaker.Decision decision = generateVariantDecisionMaker.decide(usergroups);

        assertThat(decision.isShouldGenerate(), is(true));
        assertThat(decision.isAmended(), is(true));
    }
}