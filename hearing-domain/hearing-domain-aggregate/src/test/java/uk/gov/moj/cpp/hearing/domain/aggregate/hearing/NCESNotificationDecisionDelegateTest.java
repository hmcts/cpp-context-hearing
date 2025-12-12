package uk.gov.moj.cpp.hearing.domain.aggregate.hearing;


import static java.util.Arrays.asList;
import static java.util.Optional.empty;
import static java.util.UUID.fromString;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

import uk.gov.moj.cpp.hearing.nces.ApplicationDetailsForDefendant;
import uk.gov.moj.cpp.hearing.nces.DefendantUpdateWithApplicationDetails;
import uk.gov.moj.cpp.hearing.nces.DefendantUpdateWithFinancialOrderDetails;
import uk.gov.moj.cpp.hearing.nces.DocumentContent;
import uk.gov.moj.cpp.hearing.nces.FinancialOrderForDefendant;
import uk.gov.moj.cpp.hearing.nces.NCESNotificationRequested;
import uk.gov.moj.cpp.hearing.nces.RemoveGrantedApplicationDetailsForDefendant;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class NCESNotificationDecisionDelegateTest {


    private static final UUID RD_FIDICI = fromString("de946ddc-ad77-44b1-8480-8bbc251cdcfb");
    private static final UUID RD_FIDICTI = fromString("5c023b16-e79c-4eb5-9673-e23accbeb35b");
    private static final UUID RD_FIDIPI = fromString("0e390ae0-8f3c-4735-8c0d-c16e8962537a");
    private static final UUID APPEAL_AGAINST_CONVICTION_ID = fromString("57810183-a5c2-3195-8748-c6b97eda1ebd");
    private static final UUID APPEAL_AGAINST_SENTENCE_ID = fromString("beb08419-0a9a-3119-b3ec-038d56c8a718");
    private static final UUID APPLICATION_TO_REOPEN_CASE_ID = fromString("44c238d9-3bc2-3cf3-a2eb-a7d1437b8383");
    private static final UUID APPEARANCE_TO_MAKE_STATUTORY_DECLARATION_SJP_CASE_ID = fromString("7375727f-30fc-3f55-99f3-36adc4f0e70e");
    private static final UUID APPEARANCE_TO_MAKE_STATUTORY_DECLARATION_ID = fromString("f3a6e917-7cc8-3c66-83dd-d958abd6a6e4");


    private static final UUID GRANTED_APPLICATION_OUTCOME_ID = fromString("c322f934-6b70-3fdd-b196-8628d5ee68db");
    private static final UUID REFUSED_APPLICATION_OUTCOME_ID = fromString("f48b2061-84b7-3429-8345-2ea4c3e88a3a");
    private static final UUID WITHDRAWN_APPLICATION_OUTCOME_ID = fromString("f62dedad-685b-370f-899b-61e94084dab2");

    private static final String WITHDRAWN_SCENARIO_TYPE = "withdrawn";
    private static final String REFUSED_SCENARIO_TYPE = "refused";
    private static final String GRANTED_SCENARIO_TYPE = "granted";

    public static final List<UUID> DEEMED_SERVED_RESULTS = Collections.unmodifiableList(asList(RD_FIDICI, RD_FIDICTI, RD_FIDIPI));


    @Mock
    private DefendantAggregateMomento momento;

    @InjectMocks
    private NCESNotificationDecisionDelegate ncesNotificationDecisionDelegateUnderTest;

    private UUID hearingId;
    private UUID caseId;
    private UUID defendantId;
    private String scenarioName;


    @BeforeEach
    public void setUp(){
        MockitoAnnotations.initMocks(this);
        hearingId = UUID.randomUUID();
        caseId = UUID.randomUUID();
        defendantId = UUID.randomUUID();
    }


    public static Stream<Arguments> amendmentType() {
        return Stream.of(
                Arguments.of(WITHDRAWN_SCENARIO_TYPE, APPEARANCE_TO_MAKE_STATUTORY_DECLARATION_ID, WITHDRAWN_APPLICATION_OUTCOME_ID, "Stat dec withdrawn", "stat dec / withdrawn"),
                Arguments.of(WITHDRAWN_SCENARIO_TYPE, APPEARANCE_TO_MAKE_STATUTORY_DECLARATION_SJP_CASE_ID, WITHDRAWN_APPLICATION_OUTCOME_ID, "Stat dec withdrawn",  "stat dec sjp case id / withdrawn"),
                Arguments.of(WITHDRAWN_SCENARIO_TYPE, APPEAL_AGAINST_CONVICTION_ID, WITHDRAWN_APPLICATION_OUTCOME_ID, "Appeal withdrawn", "appeal against conviction / withdrawn"),
                Arguments.of(WITHDRAWN_SCENARIO_TYPE, APPEAL_AGAINST_SENTENCE_ID, WITHDRAWN_APPLICATION_OUTCOME_ID, "Appeal withdrawn", "appeal against sentence / withdrawn"),
                Arguments.of(WITHDRAWN_SCENARIO_TYPE, APPLICATION_TO_REOPEN_CASE_ID, WITHDRAWN_APPLICATION_OUTCOME_ID, "Reopen withdrawn", "application to reopen / withdrawn"),

                Arguments.of(REFUSED_SCENARIO_TYPE, APPEARANCE_TO_MAKE_STATUTORY_DECLARATION_ID, REFUSED_APPLICATION_OUTCOME_ID, "Stat dec refused", "stat dec / refused"),
                Arguments.of(REFUSED_SCENARIO_TYPE, APPEARANCE_TO_MAKE_STATUTORY_DECLARATION_SJP_CASE_ID, REFUSED_APPLICATION_OUTCOME_ID, "Stat dec refused" , "stat dec sjp case id / refused"),
                Arguments.of(REFUSED_SCENARIO_TYPE, APPEAL_AGAINST_CONVICTION_ID, REFUSED_APPLICATION_OUTCOME_ID, "Appeal refused", "appeal against conviction / refused"),
                Arguments.of(REFUSED_SCENARIO_TYPE, APPEAL_AGAINST_SENTENCE_ID, REFUSED_APPLICATION_OUTCOME_ID, "Appeal refused", "appeal against sentence / refused"),
                Arguments.of(REFUSED_SCENARIO_TYPE, APPLICATION_TO_REOPEN_CASE_ID, REFUSED_APPLICATION_OUTCOME_ID, "Reopen refused", "application to reopen / refused"),

                Arguments.of(GRANTED_SCENARIO_TYPE, APPEARANCE_TO_MAKE_STATUTORY_DECLARATION_ID, GRANTED_APPLICATION_OUTCOME_ID, "Stat dec granted",  "stat dec / granted"),
                Arguments.of(GRANTED_SCENARIO_TYPE, APPEARANCE_TO_MAKE_STATUTORY_DECLARATION_SJP_CASE_ID, GRANTED_APPLICATION_OUTCOME_ID, "Stat dec granted", "stat dec sjp case id / granted"),
                Arguments.of(GRANTED_SCENARIO_TYPE, APPEAL_AGAINST_CONVICTION_ID, GRANTED_APPLICATION_OUTCOME_ID, "Appeal granted", "appeal against conviction / granted"),
                Arguments.of(GRANTED_SCENARIO_TYPE, APPEAL_AGAINST_SENTENCE_ID, GRANTED_APPLICATION_OUTCOME_ID, "Appeal granted", "appeal against conviction / granted"),
                Arguments.of(GRANTED_SCENARIO_TYPE, APPLICATION_TO_REOPEN_CASE_ID, GRANTED_APPLICATION_OUTCOME_ID, "Reopen granted", "application to reopen / granted")
        );
    }


    @ParameterizedTest
    @MethodSource("amendmentType")
    public void should_not_notify_NCES_with_defendant_and_application_details_when_it_was_not_shared_before(final String scenarioType,final  UUID applicationTypeId,final  UUID applicationOutcomeTypeId,
                                                                                                            final String amendmentType, final String scenarioName) {

        ApplicationDetailsForDefendant expectedApplicationDetailsForDefendant = createAppDetailsForDefendant(applicationTypeId, applicationOutcomeTypeId);

        FinancialOrderForDefendant noExistingFinancialOrderForDefendant = null;
        Mockito.when(momento.getFinancialOrderForDefendant()).thenReturn(noExistingFinancialOrderForDefendant);

        Stream<Object> stream = ncesNotificationDecisionDelegateUnderTest.updateDefendantWithApplicationDetails(expectedApplicationDetailsForDefendant);

        List<Object> events = stream.collect(Collectors.toList());
        Assert.assertThat(events.size(), is(1));

        ApplicationDetailsForDefendant actualApplicationDetailsForDefendant = getApplicationDetailsForDefendantFromTheFirstEvent(events.get(0));
        Assert.assertThat(actualApplicationDetailsForDefendant.getApplicationTypeId(), is(expectedApplicationDetailsForDefendant.getApplicationTypeId()));
        Assert.assertThat(actualApplicationDetailsForDefendant.getApplicationOutcomeTypeId(), is(expectedApplicationDetailsForDefendant.getApplicationOutcomeTypeId()));
    }

    @ParameterizedTest
    @MethodSource("amendmentType")
    public void should_notify_NCES_with_defendant_and_application_details_when_refused_or_withdrawn(final String scenarioType,final  UUID applicationTypeId,final  UUID applicationOutcomeTypeId,
                                                                                                    final String amendmentType, final String scenarioName) {

        Assume.assumeTrue(WITHDRAWN_SCENARIO_TYPE.equals(scenarioType) || REFUSED_SCENARIO_TYPE.equals(scenarioType));
        ApplicationDetailsForDefendant expectedApplicationDetailsForDefendant = createAppDetailsForDefendant(applicationTypeId, applicationOutcomeTypeId);

        Optional<String> noOldGobAccountNumber = empty();

        FinancialOrderForDefendant existingFinancialOrderForDefendantWithoutOldGobAccountNumber = createFinancialOrderForDefendant(noOldGobAccountNumber, empty());
        Mockito.when(momento.getFinancialOrderForDefendant()).thenReturn(existingFinancialOrderForDefendantWithoutOldGobAccountNumber);

        Stream<Object> stream = ncesNotificationDecisionDelegateUnderTest.updateDefendantWithApplicationDetails(expectedApplicationDetailsForDefendant);

        List<Object> events = stream.collect(Collectors.toList());
        Assert.assertThat(events.size(), is(2));

        ApplicationDetailsForDefendant actualApplicationDetailsForDefendantFromTheFirstEvent = getApplicationDetailsForDefendantFromTheFirstEvent(events.get(0));
        Assert.assertThat(actualApplicationDetailsForDefendantFromTheFirstEvent.getApplicationTypeId(), is(expectedApplicationDetailsForDefendant.getApplicationTypeId()));
        Assert.assertThat(actualApplicationDetailsForDefendantFromTheFirstEvent.getApplicationOutcomeTypeId(), is(expectedApplicationDetailsForDefendant.getApplicationOutcomeTypeId()));

        FinancialOrderForDefendant actualFinancialOrderForDefendantFromTheSecondEvent = getFinancialOrderForDefendantFromTheSecondEvent(events.get(1));
        checkTheEvent(actualFinancialOrderForDefendantFromTheSecondEvent, existingFinancialOrderForDefendantWithoutOldGobAccountNumber);
        Assert.assertThat(actualFinancialOrderForDefendantFromTheSecondEvent.getDocumentContent().getAmendmentType(), is(amendmentType));
    }

    @ParameterizedTest
    @MethodSource("amendmentType")
    public void should_NOT_notify_NCES_when_we_share_and_there_is_NO_deemed_served_results(final String scenarioType,final  UUID applicationTypeId,final  UUID applicationOutcomeTypeId,
                                                                                           final String amendmentType, final String scenarioName) {
        Optional<List<UUID>> noDeemedServedResults = empty();
        FinancialOrderForDefendant expectedSharedFinancialOrderForDefendant = createFinancialOrderForDefendant(empty(), noDeemedServedResults);

        FinancialOrderForDefendant noExistingFinancialOrderForDefendant = null;
        Mockito.when(momento.getFinancialOrderForDefendant()).thenReturn(noExistingFinancialOrderForDefendant);

        Stream<Object> stream = ncesNotificationDecisionDelegateUnderTest.updateDefendantWithFinancialOrder(expectedSharedFinancialOrderForDefendant);


        List<Object> events = stream.collect(Collectors.toList());
        Assert.assertThat(events.size(), is(1));

        FinancialOrderForDefendant actualFinancialOrderForDefendantFromTheFirstEvent = getFinancialOrderForDefendantFromTheFirstEvent(events.get(0));
        checkTheEvent(expectedSharedFinancialOrderForDefendant, actualFinancialOrderForDefendantFromTheFirstEvent);

    }


    @ParameterizedTest
    @MethodSource("amendmentType")
    public void should_notify_NCES_when_we_share_with_deemed_served_results(final String scenarioType,final  UUID applicationTypeId,final  UUID applicationOutcomeTypeId,
                                                                            final String amendmentType, final String scenarioName) {
        FinancialOrderForDefendant expectedSharedFinancialOrderForDefendant = createFinancialOrderForDefendant(empty(), Optional.of(DEEMED_SERVED_RESULTS));

        FinancialOrderForDefendant noExistingFinancialOrderForDefendant = null;
        Mockito.when(momento.getFinancialOrderForDefendant()).thenReturn(noExistingFinancialOrderForDefendant);

        Stream<Object> stream = ncesNotificationDecisionDelegateUnderTest.updateDefendantWithFinancialOrder(expectedSharedFinancialOrderForDefendant);

        List<Object> events = stream.collect(Collectors.toList());
        Assert.assertThat(events.size(), is(2));

        FinancialOrderForDefendant actualFinancialOrderForDefendantFromTheFirstEvent = getFinancialOrderForDefendantFromTheFirstEvent(events.get(0));
        checkTheEvent(expectedSharedFinancialOrderForDefendant, actualFinancialOrderForDefendantFromTheFirstEvent);

        FinancialOrderForDefendant actualFinancialOrderForDefendantFromTheSecondEvent = getFinancialOrderForDefendantFromTheSecondEvent(events.get(1));
        checkTheEvent(expectedSharedFinancialOrderForDefendant, actualFinancialOrderForDefendantFromTheSecondEvent);
        Assert.assertThat(actualFinancialOrderForDefendantFromTheSecondEvent.getDocumentContent().getAmendmentType(), is("Write off one day deemed served"));
    }

    @ParameterizedTest
    @MethodSource("amendmentType")
    public void should_notify_NCES_when_we_reshare_with_an_existing_application(final String scenarioType,final  UUID applicationTypeId,final  UUID applicationOutcomeTypeId,
                                                                                final String amendmentType, final String scenarioName) {

        Assume.assumeTrue(GRANTED_SCENARIO_TYPE.equals(scenarioType));

        FinancialOrderForDefendant expectedReshareFinancialOrderForDefendant = createFinancialOrderForDefendant(empty(), Optional.of(DEEMED_SERVED_RESULTS));

        FinancialOrderForDefendant existingFinancialOrderForDefendant = createFinancialOrderForDefendant(empty(), empty());
        Mockito.when(momento.getFinancialOrderForDefendant()).thenReturn(existingFinancialOrderForDefendant);
        Mockito.when(momento.getApplicationDetailsForDefendant()).thenReturn(createAppDetailsForDefendant(applicationTypeId, applicationOutcomeTypeId));

        Stream<Object> stream = ncesNotificationDecisionDelegateUnderTest.updateDefendantWithFinancialOrder(expectedReshareFinancialOrderForDefendant);

        List<Object> events = stream.collect(Collectors.toList());
        Assert.assertThat(events.size(), is(3));

        FinancialOrderForDefendant actualFinancialOrderForDefendantFromTheFirstEvent = getFinancialOrderForDefendantFromTheFirstEvent(events.get(0));
        checkTheEvent(expectedReshareFinancialOrderForDefendant, actualFinancialOrderForDefendantFromTheFirstEvent);

        FinancialOrderForDefendant actualFinancialOrderForDefendantFromTheSecondEvent = getFinancialOrderForDefendantFromTheSecondEvent(events.get(1));
        checkTheEvent(expectedReshareFinancialOrderForDefendant, actualFinancialOrderForDefendantFromTheFirstEvent);
        Assert.assertThat(actualFinancialOrderForDefendantFromTheSecondEvent.getDocumentContent().getAmendmentType(), is(amendmentType));

        Assert.assertThat(events.get(2), instanceOf(RemoveGrantedApplicationDetailsForDefendant.class));

    }

    @ParameterizedTest
    @MethodSource("amendmentType")
    public void should_notify_NCES_when_we_reshare_having_NO_deemed_served_results_without_an_existing_application(final String scenarioType,final  UUID applicationTypeId,final  UUID applicationOutcomeTypeId,
                                                                                                                   final String amendmentType, final String scenarioName) {

        Assume.assumeTrue(GRANTED_SCENARIO_TYPE.equals(scenarioType));

        Optional<List<UUID>> noDeemedServedResults = empty();
        FinancialOrderForDefendant expectedFinancialOrderForDefendant = createFinancialOrderForDefendant(empty(), noDeemedServedResults);

        FinancialOrderForDefendant existingFinancialOrderForDefendant = createFinancialOrderForDefendant(empty(), noDeemedServedResults);
        ApplicationDetailsForDefendant noExistingApplicationDetailsForDefendant = null;

        Mockito.when(momento.getFinancialOrderForDefendant()).thenReturn(existingFinancialOrderForDefendant);
        Mockito.when(momento.getApplicationDetailsForDefendant()).thenReturn(noExistingApplicationDetailsForDefendant);

        Stream<Object> stream = ncesNotificationDecisionDelegateUnderTest.updateDefendantWithFinancialOrder(expectedFinancialOrderForDefendant);

        List<Object> events = stream.collect(Collectors.toList());
        Assert.assertThat(events.size(), is(2));

        FinancialOrderForDefendant actualApplicationDetailsForDefendantFromTheFirstEvent = getFinancialOrderForDefendantFromTheFirstEvent(events.get(0));
        checkTheEvent(expectedFinancialOrderForDefendant, actualApplicationDetailsForDefendantFromTheFirstEvent);

        FinancialOrderForDefendant actualFinancialOrderForDefendantFromTheSecondEvent = getFinancialOrderForDefendantFromTheSecondEvent(events.get(1));
        checkTheEvent(expectedFinancialOrderForDefendant, actualFinancialOrderForDefendantFromTheSecondEvent);
        Assert.assertThat(actualFinancialOrderForDefendantFromTheSecondEvent.getDocumentContent().getAmendmentType(), is("Amend result"));
    }



    @ParameterizedTest
    @MethodSource("amendmentType")
    public void should_notify_NCES_when_we_reshare_having_deemed_served_results_without_an_existing_application(final String scenarioType,final  UUID applicationTypeId,final  UUID applicationOutcomeTypeId,
                                                                                                                final String amendmentType, final String scenarioName) {

        Assume.assumeTrue(GRANTED_SCENARIO_TYPE.equals(scenarioType));

        FinancialOrderForDefendant expectedResharedFinancialOrderForDefendant = createFinancialOrderForDefendant(empty(), Optional.of(DEEMED_SERVED_RESULTS));
        FinancialOrderForDefendant existingFinancialOrderForDefendantForMomento = createFinancialOrderForDefendant(empty(), empty());
        ApplicationDetailsForDefendant noExistingApplicationDetailsForDefendant = null;

        Mockito.when(momento.getFinancialOrderForDefendant()).thenReturn(existingFinancialOrderForDefendantForMomento);
        Mockito.when(momento.getApplicationDetailsForDefendant()).thenReturn(noExistingApplicationDetailsForDefendant);

        Stream<Object> stream = ncesNotificationDecisionDelegateUnderTest.updateDefendantWithFinancialOrder(expectedResharedFinancialOrderForDefendant);

        List<Object> events = stream.collect(Collectors.toList());
        Assert.assertThat(events.size(), is(2));

        FinancialOrderForDefendant actualApplicationDetailsForDefendantFromTheFirstEvent = getFinancialOrderForDefendantFromTheFirstEvent(events.get(0));
        checkTheEvent(expectedResharedFinancialOrderForDefendant, actualApplicationDetailsForDefendantFromTheFirstEvent);


        FinancialOrderForDefendant actualFinancialOrderForDefendantFromTheSecondEvent = getFinancialOrderForDefendantFromTheSecondEvent(events.get(1));
        checkTheEvent(expectedResharedFinancialOrderForDefendant, actualFinancialOrderForDefendantFromTheSecondEvent);
        Assert.assertThat(actualFinancialOrderForDefendantFromTheSecondEvent.getDocumentContent().getAmendmentType(), is("Amend result"));

    }

    private void checkTheEvent(FinancialOrderForDefendant expectedFinancialOrderForDefendant, FinancialOrderForDefendant actualApplicationDetailsForDefendantFromTheFirstEvent) {
        Assert.assertThat(actualApplicationDetailsForDefendantFromTheFirstEvent.getHearingId(), is(expectedFinancialOrderForDefendant.getHearingId()));
        Assert.assertThat(actualApplicationDetailsForDefendantFromTheFirstEvent.getDefendantId(), is(expectedFinancialOrderForDefendant.getDefendantId()));
        Assert.assertThat(actualApplicationDetailsForDefendantFromTheFirstEvent.getCaseId(), is(expectedFinancialOrderForDefendant.getCaseId()));
    }


    private FinancialOrderForDefendant createFinancialOrderForDefendant(Optional<String> oldGobAccountNumber, Optional<List<UUID>> resultDefinitionIds) {
        return createNewFinancialOrderForDefendant(caseId, defendantId, hearingId, oldGobAccountNumber, resultDefinitionIds);
    }

    private FinancialOrderForDefendant createNewFinancialOrderForDefendant(UUID caseId, UUID defendantId, UUID hearingId,
                                                                        Optional<String> oldGobAccountNumber, Optional<List<UUID>> resultDefinitionIds) {
        FinancialOrderForDefendant.Builder builder = FinancialOrderForDefendant.newBuilder()
                                                        .withCaseId(caseId)
                                                        .withDefendantId(defendantId)
                                                        .withHearingId(hearingId);
        resultDefinitionIds.ifPresent(ids -> builder.withResultDefinitionIds(ids));
        builder.withDocumentContent(createDocumentContent(oldGobAccountNumber));

        return builder.build();
    }

    private DocumentContent createDocumentContent(Optional<String> oldGobAccountNumber) {
        DocumentContent.Builder builder = DocumentContent.documentContent();
        oldGobAccountNumber.ifPresent(id -> builder.withOldGobAccountNumber(id));
        return builder.build();
    }

    private FinancialOrderForDefendant getFinancialOrderForDefendantFromTheFirstEvent(Object firstEvent) {
        return ((DefendantUpdateWithFinancialOrderDetails)firstEvent).getFinancialOrderForDefendant();
    }

    private ApplicationDetailsForDefendant getApplicationDetailsForDefendantFromTheFirstEvent(Object secondEvent) {
        return ((DefendantUpdateWithApplicationDetails)secondEvent).getApplicationDetailsForDefendant();
    }

    private FinancialOrderForDefendant getFinancialOrderForDefendantFromTheSecondEvent(Object secondEvent) {
        return ((NCESNotificationRequested)secondEvent).getFinancialOrderForDefendant();
    }

    private ApplicationDetailsForDefendant createAppDetailsForDefendant(UUID appTypeId, UUID appOutcomeTypeId) {
        return ApplicationDetailsForDefendant.newBuilder()
                .withApplicationTypeId(appTypeId)
                .withApplicationOutcomeTypeId(appOutcomeTypeId)
                .build();
    }

}