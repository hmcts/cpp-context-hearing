package uk.gov.moj.cpp.hearing.event.nows.mapper;

import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static uk.gov.justice.json.schemas.staging.Frequency.WEEKLY;
import static uk.gov.justice.json.schemas.staging.ReserveTermsType.INSTALMENTS_ONLY;
import static uk.gov.justice.json.schemas.staging.ReserveTermsType.LUMP_SUM;
import static uk.gov.justice.json.schemas.staging.ReserveTermsType.LUMP_SUM_PLUS_INSTALMENTS;
import static uk.gov.moj.cpp.hearing.event.nows.PromptTypesConstant.P_INSTALMENT_AMOUNT;
import static uk.gov.moj.cpp.hearing.event.nows.PromptTypesConstant.P_INSTALMENT_START_DATE;
import static uk.gov.moj.cpp.hearing.event.nows.PromptTypesConstant.P_LUMP_SUM_AMOUNT;
import static uk.gov.moj.cpp.hearing.event.nows.PromptTypesConstant.P_PAYMENT_FREQUENCY;
import static uk.gov.moj.cpp.hearing.event.nows.ResultDefinitionsConstant.RD_ABCD;
import static uk.gov.moj.cpp.hearing.event.nows.ResultDefinitionsConstant.RD_AEOC;
import static uk.gov.moj.cpp.hearing.event.nows.ResultDefinitionsConstant.RD_RINSTL;
import static uk.gov.moj.cpp.hearing.event.nows.ResultDefinitionsConstant.RD_RLSUM;
import static uk.gov.moj.cpp.hearing.event.nows.ResultDefinitionsConstant.RD_RLSUMI;

import uk.gov.justice.core.courts.FinancialOrderDetails;
import uk.gov.justice.core.courts.Now;
import uk.gov.justice.core.courts.Prompt;
import uk.gov.justice.core.courts.ResultPrompt;
import uk.gov.justice.core.courts.SharedResultLine;
import uk.gov.justice.json.schemas.staging.CollectionOrder;
import uk.gov.justice.json.schemas.staging.ReserveTermsType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.junit.Test;

public class StagingEnforcementCollectionOrderMapperTest {

    @Test
    public void testEmpty() {

        final List<SharedResultLine> sharedResultLines = new ArrayList<>();

        final Map<UUID, UUID> resultLineResultDefinitionIdMap = new HashMap<>();

        final Map<UUID, List<Prompt>> sharedResultLineWithPrompts = new HashMap<>();

        final Now now = Now.now().build();

        StagingEnforcementCollectionOrderMapper mapper = new StagingEnforcementCollectionOrderMapper(
                sharedResultLines,
                now,
                resultLineResultDefinitionIdMap,
                sharedResultLineWithPrompts);

        CollectionOrder collectionOrder = mapper.map();

        assertNotNull(collectionOrder);
    }

    @Test
    public void testIsApplicationForBenefitsDeduction() {

        final Now now = Now.now().build();

        Map<UUID, List<Prompt>> sharedResultLineWithPrompts = new HashMap<>();
        UUID sharedResultLineId = randomUUID();
        sharedResultLineWithPrompts.put(sharedResultLineId, asList());

        Map<UUID, UUID> resultLineResultDefinitionIdMap = new HashMap<>();
        resultLineResultDefinitionIdMap.put(sharedResultLineId, RD_ABCD);

        final List<SharedResultLine> sharedResultLines = new ArrayList<>();
        sharedResultLines.add(SharedResultLine.sharedResultLine().withId(sharedResultLineId)
                .withPrompts(asList(ResultPrompt.resultPrompt().withValue("WEEKLY").build())).build());

        StagingEnforcementCollectionOrderMapper mapper = new StagingEnforcementCollectionOrderMapper(
                sharedResultLines,
                now,
                resultLineResultDefinitionIdMap,
                sharedResultLineWithPrompts);

        CollectionOrder collectionOrder = mapper.map();

        assertEquals(true, collectionOrder.getIsApplicationForBenefitsDeduction());
    }

    @Test
    public void testIsAttachmentOfEarnings() {

        final Now now = Now.now().build();

        Map<UUID, List<Prompt>> sharedResultLineWithPrompts = new HashMap<>();
        UUID sharedResultLineId = randomUUID();
        sharedResultLineWithPrompts.put(sharedResultLineId, asList());

        Map<UUID, UUID> resultLineResultDefinitionIdMap = new HashMap<>();
        resultLineResultDefinitionIdMap.put(sharedResultLineId, RD_AEOC);

        final List<SharedResultLine> sharedResultLines = new ArrayList<>();
        sharedResultLines.add(SharedResultLine.sharedResultLine().build());

        StagingEnforcementCollectionOrderMapper mapper = new StagingEnforcementCollectionOrderMapper(
                sharedResultLines,
                now,
                resultLineResultDefinitionIdMap,
                sharedResultLineWithPrompts);

        CollectionOrder collectionOrder = mapper.map();

        assertEquals(true, collectionOrder.getIsAttachmentOfEarnings());
    }

    @Test
    public void testReserveTermsType() {

        final Now now = Now.now().build();

        Map<UUID, ReserveTermsType> reserveTermsTypeMap = new HashMap<>();
        reserveTermsTypeMap.put(RD_RLSUM, LUMP_SUM);
        reserveTermsTypeMap.put(RD_RLSUMI, LUMP_SUM_PLUS_INSTALMENTS);
        reserveTermsTypeMap.put(RD_RINSTL, INSTALMENTS_ONLY);

        int random = new Random().nextInt(3);

        List<UUID> resultDefinitions = new ArrayList<>(reserveTermsTypeMap.keySet());

        Map<UUID, List<Prompt>> sharedResultLineWithPrompts = new HashMap<>();
        UUID sharedResultLineId = randomUUID();
        sharedResultLineWithPrompts.put(sharedResultLineId, asList());

        Map<UUID, UUID> resultLineResultDefinitionIdMap = new HashMap<>();
        resultLineResultDefinitionIdMap.put(sharedResultLineId, resultDefinitions.get(random));

        final List<SharedResultLine> sharedResultLines = new ArrayList<>();
        sharedResultLines.add(SharedResultLine.sharedResultLine().build());

        StagingEnforcementCollectionOrderMapper mapper = new StagingEnforcementCollectionOrderMapper(
                sharedResultLines,
                now,
                resultLineResultDefinitionIdMap,
                sharedResultLineWithPrompts);

        CollectionOrder collectionOrder = mapper.map();

        assertEquals(reserveTermsTypeMap.get(resultDefinitions.get(random)), collectionOrder.getReserveTerms().getReserveTermsType());

    }

    @Test
    public void testLumpSumAmount() {

        final Now now = Now.now().withFinancialOrders(FinancialOrderDetails.financialOrderDetails().withTotalBalance("100").build()).build();

        Map<UUID, ReserveTermsType> reserveTermsTypeMap = new HashMap<>();
        reserveTermsTypeMap.put(RD_RLSUM, LUMP_SUM);

        List<UUID> resultDefinitions = new ArrayList<>(reserveTermsTypeMap.keySet());

        Map<UUID, List<Prompt>> sharedResultLineWithPrompts = new HashMap<>();
        UUID sharedResultLineId = randomUUID();

        sharedResultLineWithPrompts.put(sharedResultLineId, asList());

        Map<UUID, UUID> resultLineResultDefinitionIdMap = new HashMap<>();
        resultLineResultDefinitionIdMap.put(sharedResultLineId, resultDefinitions.get(0));

        final List<SharedResultLine> sharedResultLines = new ArrayList<>();
        sharedResultLines.add(
                SharedResultLine.sharedResultLine()
                        .withId(sharedResultLineId)
                        .build());

        StagingEnforcementCollectionOrderMapper mapper = new StagingEnforcementCollectionOrderMapper(
                sharedResultLines,
                now,
                resultLineResultDefinitionIdMap,
                sharedResultLineWithPrompts);

        CollectionOrder collectionOrder = mapper.map();

        assertEquals(new BigDecimal(100), collectionOrder.getReserveTerms().getLumpSum().getAmount());
    }

    @Test
    public void testLumpSumInstalmentsFrequency() {

        final Map<UUID, ReserveTermsType> reserveTermsTypeMap = new HashMap<>();
        reserveTermsTypeMap.put(RD_RINSTL, INSTALMENTS_ONLY);

        final List<UUID> resultDefinitions = new ArrayList<>(reserveTermsTypeMap.keySet());

        final Map<UUID, List<Prompt>> sharedResultLineWithPrompts = new HashMap<>();
        final UUID sharedResultLineId = randomUUID();
        final UUID promptId1 = randomUUID();
        final UUID promptId2 = randomUUID();
        final UUID promptId3 = randomUUID();
        final UUID promptId4 = randomUUID();
        final ResultPrompt resultPrompt1 = ResultPrompt.resultPrompt()
                .withId(promptId1)
                .withPromptReference(P_PAYMENT_FREQUENCY)
                .withValue("WEEKLY")
                .build();
        final ResultPrompt resultPrompt3 = ResultPrompt.resultPrompt()
                .withId(promptId3)
                .withPromptReference(P_INSTALMENT_START_DATE)
                .withValue("2018-01-10")
                .build();
        final ResultPrompt resultPrompt4 = ResultPrompt.resultPrompt()
                .withId(promptId4)
                .withPromptReference(P_INSTALMENT_AMOUNT)
                .withValue("50")
                .build();

        final Now now = Now.now().build();

        sharedResultLineWithPrompts.put(sharedResultLineId, asList(Prompt.prompt().withId(promptId1).build(), Prompt.prompt().withId(promptId2).build(), Prompt.prompt().withId(promptId3).build(), Prompt.prompt().withId(promptId4).build()));

        Map<UUID, UUID> resultLineResultDefinitionIdMap = new HashMap<>();
        resultLineResultDefinitionIdMap.put(sharedResultLineId, resultDefinitions.get(0));

        final List<SharedResultLine> sharedResultLines = new ArrayList<>();
        sharedResultLines.add(
                SharedResultLine.sharedResultLine()
                        .withId(sharedResultLineId)
                        .withPrompts(asList(resultPrompt1, resultPrompt3, resultPrompt4)).build());

        StagingEnforcementCollectionOrderMapper mapper = new StagingEnforcementCollectionOrderMapper(
                sharedResultLines,
                now,
                resultLineResultDefinitionIdMap,
                sharedResultLineWithPrompts);

        CollectionOrder collectionOrder = mapper.map();

        assertEquals(WEEKLY, collectionOrder.getReserveTerms().getInstalments().getFrequency());
    }

    @Test
    public void testLumpSumInstalmentsStartDate() {

        final Now now = Now.now().build();

        Map<UUID, ReserveTermsType> reserveTermsTypeMap = new HashMap<>();
        reserveTermsTypeMap.put(RD_RINSTL, INSTALMENTS_ONLY);

        List<UUID> resultDefinitions = new ArrayList<>(reserveTermsTypeMap.keySet());

        Map<UUID, List<Prompt>> sharedResultLineWithPrompts = new HashMap<>();
        UUID sharedResultLineId = randomUUID();
        UUID promptId1 = randomUUID();
        ResultPrompt resultPrompt1 = ResultPrompt.resultPrompt()
                .withId(promptId1)
                .withPromptReference(P_INSTALMENT_START_DATE)
                .withValue("2018-01-01")
                .build();
        UUID promptId2 = randomUUID();
        ResultPrompt resultPrompt2 = ResultPrompt.resultPrompt()
                .withId(promptId2)
                .withPromptReference(P_INSTALMENT_AMOUNT)
                .withValue("10")
                .build();
        UUID promptId3 = randomUUID();
        final ResultPrompt resultPrompt3 = ResultPrompt.resultPrompt()
                .withId(promptId3)
                .withPromptReference(P_PAYMENT_FREQUENCY)
                .withValue("WEEKLY")
                .build();

        sharedResultLineWithPrompts.put(sharedResultLineId, asList(Prompt.prompt().withId(promptId1).build(), Prompt.prompt().withId(promptId2).build(), Prompt.prompt().withId(promptId3).build()));

        Map<UUID, UUID> resultLineResultDefinitionIdMap = new HashMap<>();
        resultLineResultDefinitionIdMap.put(sharedResultLineId, resultDefinitions.get(0));

        final List<SharedResultLine> sharedResultLines = new ArrayList<>();
        sharedResultLines.add(
                SharedResultLine.sharedResultLine()
                        .withId(sharedResultLineId)
                        .withPrompts(asList(resultPrompt1, resultPrompt2, resultPrompt3)).build());

        StagingEnforcementCollectionOrderMapper mapper = new StagingEnforcementCollectionOrderMapper(
                sharedResultLines,
                now,
                resultLineResultDefinitionIdMap,
                sharedResultLineWithPrompts);

        CollectionOrder collectionOrder = mapper.map();

        assertEquals(LocalDate.of(2018, 1, 1), collectionOrder.getReserveTerms().getInstalments().getStartDate());
    }

    @Test
    public void testLumpSumPlusInstalments() {

        final Now now = Now.now().build();

        Map<UUID, ReserveTermsType> reserveTermsTypeMap = new HashMap<>();
        reserveTermsTypeMap.put(RD_RLSUMI, LUMP_SUM_PLUS_INSTALMENTS);

        List<UUID> resultDefinitions = new ArrayList<>(reserveTermsTypeMap.keySet());

        Map<UUID, List<Prompt>> sharedResultLineWithPrompts = new HashMap<>();
        UUID sharedResultLineId = randomUUID();
        UUID promptId1 = randomUUID();
        ResultPrompt resultPrompt1 = ResultPrompt.resultPrompt()
                .withId(promptId1)
                .withPromptReference(P_LUMP_SUM_AMOUNT)
                .withValue("100")
                .build();
        UUID promptId2 = randomUUID();
        ResultPrompt resultPrompt2 = ResultPrompt.resultPrompt()
                .withId(promptId2)
                .withPromptReference(P_INSTALMENT_START_DATE)
                .withValue("2018-01-01")
                .build();
        UUID promptId3 = randomUUID();
        ResultPrompt resultPrompt3 = ResultPrompt.resultPrompt()
                .withId(promptId3)
                .withPromptReference(P_INSTALMENT_AMOUNT)
                .withValue("10")
                .build();
        UUID promptId4 = randomUUID();
        final ResultPrompt resultPrompt4 = ResultPrompt.resultPrompt()
                .withId(promptId4)
                .withPromptReference(P_PAYMENT_FREQUENCY)
                .withValue("WEEKLY")
                .build();

        sharedResultLineWithPrompts.put(sharedResultLineId, asList(Prompt.prompt().withId(promptId1).build(), Prompt.prompt().withId(promptId2).build(), Prompt.prompt().withId(promptId3).build(), Prompt.prompt().withId(promptId4).build()));

        Map<UUID, UUID> resultLineResultDefinitionIdMap = new HashMap<>();
        resultLineResultDefinitionIdMap.put(sharedResultLineId, resultDefinitions.get(0));

        final List<SharedResultLine> sharedResultLines = new ArrayList<>();
        sharedResultLines.add(
                SharedResultLine.sharedResultLine()
                        .withId(sharedResultLineId)
                        .withPrompts(asList(resultPrompt1, resultPrompt2, resultPrompt3, resultPrompt4)).build());

        StagingEnforcementCollectionOrderMapper mapper = new StagingEnforcementCollectionOrderMapper(
                sharedResultLines,
                now,
                resultLineResultDefinitionIdMap,
                sharedResultLineWithPrompts);

        CollectionOrder collectionOrder = mapper.map();

        assertEquals(LocalDate.of(2018, 1, 1), collectionOrder.getReserveTerms().getInstalments().getStartDate());
        assertEquals(LUMP_SUM_PLUS_INSTALMENTS, collectionOrder.getReserveTerms().getReserveTermsType());
        assertEquals(new BigDecimal("100").toString(), collectionOrder.getReserveTerms().getLumpSum().getAmount().toString());
        assertEquals("10", collectionOrder.getReserveTerms().getInstalments().getAmount().toString());
        assertEquals(WEEKLY, collectionOrder.getReserveTerms().getInstalments().getFrequency());
    }
}