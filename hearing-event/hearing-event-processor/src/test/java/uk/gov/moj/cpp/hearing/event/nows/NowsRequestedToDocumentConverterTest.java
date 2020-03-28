package uk.gov.moj.cpp.hearing.event.nows;

import static java.lang.String.format;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloper;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.test.matchers.ElementAtListMatcher.first;

import uk.gov.justice.core.courts.CreateNowsRequest;
import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.JurisdictionType;
import uk.gov.justice.core.courts.Now;
import uk.gov.justice.core.courts.Offence;
import uk.gov.justice.core.courts.ProsecutionCaseIdentifier;
import uk.gov.justice.core.courts.nowdocument.DefendantCaseOffence;
import uk.gov.justice.core.courts.nowdocument.FinancialOrderDetails;
import uk.gov.justice.core.courts.nowdocument.NowDocumentContent;
import uk.gov.justice.core.courts.nowdocument.NowDocumentRequest;
import uk.gov.justice.core.courts.nowdocument.Nowaddress;
import uk.gov.justice.core.courts.nowdocument.Nowdefendant;
import uk.gov.justice.core.courts.nowdocument.Prompt;
import uk.gov.justice.core.courts.nowdocument.ProsecutionCase;
import uk.gov.justice.core.courts.nowdocument.Result;
import uk.gov.justice.hearing.courts.referencedata.CourtCentreOrganisationUnit;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.notification.Subscriptions;
import uk.gov.moj.cpp.hearing.event.NowsRequestedToDocumentConverter;
import uk.gov.moj.cpp.hearing.event.service.CourtHouseReverseLookup;
import uk.gov.moj.cpp.hearing.test.TestTemplates;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

@RunWith(DataProviderRunner.class)
public class NowsRequestedToDocumentConverterTest {

    @Spy
    private final Enveloper enveloper = createEnveloper();

    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @Mock
    private SubscriptionClient subscriptionClient;

    @Mock
    private CourtHouseReverseLookup courtHouseReverseLookup;

    @InjectMocks
    private NowsRequestedToDocumentConverter nowsRequestedToDocumentConverter;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        setField(this.jsonObjectToObjectConverter, "objectMapper", new ObjectMapperProducer().objectMapper());
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
        CourtCentreOrganisationUnit courtCentreOrganisationUnit = new CourtCentreOrganisationUnit(null, null, null, null,
                null, null, null, null,
                "1234", false, null, null, null,
                null, null, null, null, null,
                null, null, null, null, null, null);
        when(courtHouseReverseLookup.getCourtCentreById(any(), any())).thenReturn(Optional.of(courtCentreOrganisationUnit));
    }

    @Test
    public void testCrownCourtConversion() throws IOException {
        testConversion(JurisdictionType.CROWN, false);
    }

    @Test
    public void testMagsCourtConversion() throws IOException {
        testConversion(JurisdictionType.MAGISTRATES, true);
    }

    @Test
    public void testNowsRequestTemplateWithConditionalText() {
        final CreateNowsRequest input = TestTemplates.generateNowsRequestTemplateWithConditionalText(UUID.randomUUID(), JurisdictionType.MAGISTRATES, true);
        final Hearing hearing = input.getHearing();
        final uk.gov.justice.core.courts.ProsecutionCase prosecutionCase = hearing.getProsecutionCases().get(0);
        final Defendant defendant = prosecutionCase.getDefendants().get(0);
        final Offence offence = defendant.getOffences().get(0);
        final Now now0 = input.getNows().get(0);

        final JsonEnvelope envelope = envelopeFrom(
                metadataWithRandomUUID("hearing.events.nows-requested"),
                objectToJsonObjectConverter.convert(input)
        );

        Subscriptions subscriptions = mock(Subscriptions.class);
        when(subscriptionClient.getAll(Mockito.any(JsonEnvelope.class), Mockito.any(UUID.class), Mockito.any(LocalDate.class))).thenReturn(subscriptions);

        final List<NowDocumentRequest> nowDocumentResult = nowsRequestedToDocumentConverter.convert(envelope, input);

        final NowDocumentContent nowDocumentContent = nowDocumentResult.get(0).getNowContent();
        final String defendantName = now0.getRequestedMaterials().get(0).getNowVariantDefendant().getName();
        final LocalDate dob = now0.getRequestedMaterials().get(0).getNowVariantDefendant().getDateOfBirth();
        final uk.gov.justice.core.courts.Address address = now0.getRequestedMaterials().get(0).getNowVariantDefendant().getAddress();
        final ProsecutionCaseIdentifier prosecutionCaseIdentifier = prosecutionCase.getProsecutionCaseIdentifier();
        final String expectedCaseRef = prosecutionCaseIdentifier.getProsecutionAuthorityReference();
        final LocalDate expectedConvictionDate = offence.getConvictionDate();
        uk.gov.justice.core.courts.FinancialOrderDetails financialOrderDetailsIn = now0.getFinancialOrders();

        assertThat(nowDocumentContent, isBean(NowDocumentContent.class)
                .with(NowDocumentContent::getOrderName, is(input.getNowTypes().get(0).getDescription()))
                .with(NowDocumentContent::getCourtCentreName, is(input.getHearing().getCourtCentre().getName()))
                .with(NowDocumentContent::getCourtClerkName, is(format("%s %s", input.getCourtClerk().getFirstName(), input.getCourtClerk().getLastName())))
                .with(NowDocumentContent::getOrderDate, is(input.getSharedResultLines().get(0).getOrderedDate().toString()))
                .with(NowDocumentContent::getDefendant, isBean(Nowdefendant.class)
                        .with(Nowdefendant::getName, is(defendantName))
                        .with(Nowdefendant::getDateOfBirth, is(dob.toString()))
                        .with(Nowdefendant::getAddress, isBean(Nowaddress.class)
                                .with(Nowaddress::getLine1, is(address.getAddress1()))
                                .with(Nowaddress::getLine2, nullValue())
                                .with(Nowaddress::getLine3, nullValue())
                                .with(Nowaddress::getLine4, nullValue())
                                .with(Nowaddress::getPostCode, nullValue())
                        )
                )
                .with(NowDocumentContent::getCaseUrns, hasItem(expectedCaseRef))
                .with(NowDocumentContent::getFinancialOrderDetails, isBean(FinancialOrderDetails.class)
                        .withValue(FinancialOrderDetails::getAccountPaymentReference, financialOrderDetailsIn.getAccountReference())
                )
                .with(NowDocumentContent::getCases, first(isBean(ProsecutionCase.class)
                        .with(ProsecutionCase::getUrn, is(expectedCaseRef))
                        .with(ProsecutionCase::getDefendantCaseResults, first(isBean(Result.class)
                                .with(Result::getLabel, is(input.getSharedResultLines().get(0).getLabel()))
                                .with(Result::getPrompts, first(isBean(Prompt.class)
                                        .with(Prompt::getLabel, is(input.getSharedResultLines().get(0).getPrompts().get(0).getLabel()))
                                        .with(Prompt::getValue, is(input.getSharedResultLines().get(0).getPrompts().get(0).getValue()))
                                ))
                        ))
                        .with(ProsecutionCase::getDefendantCaseResults, first(isBean(Result.class)
                                .with(Result::getLabel, is(input.getSharedResultLines().get(1).getLabel()))
                                .with(Result::getPrompts, first(isBean(Prompt.class)
                                        .with(Prompt::getLabel, is(input.getSharedResultLines().get(1).getPrompts().get(0).getLabel()))
                                        .with(Prompt::getValue, is(input.getSharedResultLines().get(1).getPrompts().get(0).getValue()))
                                ))
                        ))
                        .with(ProsecutionCase::getDefendantCaseOffences, first(isBean(DefendantCaseOffence.class)
                                .withValue(DefendantCaseOffence::getTitle, offence.getOffenceTitle())
                                .withValue(DefendantCaseOffence::getWelshTitle, offence.getOffenceTitleWelsh())
                                .with(DefendantCaseOffence::getStartDate, is(offence.getStartDate().toString()))
                                .with(DefendantCaseOffence::getConvictionDate, is(expectedConvictionDate.toString()))
                                .with(DefendantCaseOffence::getResults, first(isBean(Result.class)
                                        .with(Result::getLabel, is(input.getSharedResultLines().get(2).getLabel()))
                                        .with(Result::getPrompts, first(isBean(Prompt.class)
                                                .with(Prompt::getLabel, is(input.getSharedResultLines().get(2).getPrompts().get(0).getLabel()))
                                                .with(Prompt::getValue, is(input.getSharedResultLines().get(2).getPrompts().get(0).getValue()))
                                        ))
                                ))
                        ))
                ))
                .with(NowDocumentContent::getNowText, is(input.getNowTypes().get(0).getStaticText()))
        );
        assertThat(nowDocumentContent.getNowResultDefinitionsText().getAdditionalProperties().get("ABCD"),
                is(input.getNows().get(0).getRequestedMaterials().get(0).getNowResults().get(2).getNowVariantResultText().getAdditionalProperties().get("ABCD")));
    }

    private void testConversion(final JurisdictionType jurisdictionType, final boolean expectedRemotePrintingRequired) throws IOException {

        final CreateNowsRequest input = TestTemplates.generateNowsRequestTemplate(UUID.randomUUID(), jurisdictionType, true, expectedRemotePrintingRequired);
        final Hearing hearing = input.getHearing();
        final uk.gov.justice.core.courts.ProsecutionCase prosecutionCase = hearing.getProsecutionCases().get(0);
        final Defendant defendant = prosecutionCase.getDefendants().get(0);
        final Offence offence = defendant.getOffences().get(0);

        final JsonEnvelope envelope = envelopeFrom(
                metadataWithRandomUUID("hearing.events.nows-requested"),
                objectToJsonObjectConverter.convert(input)
        );

        Subscriptions subscriptions = mock(Subscriptions.class);
        when(subscriptionClient.getAll(Mockito.any(JsonEnvelope.class), Mockito.any(UUID.class), Mockito.any(LocalDate.class))).thenReturn(subscriptions);

        final List<NowDocumentRequest> nowDocumentRequest = nowsRequestedToDocumentConverter.convert(envelope, input);

        final NowDocumentContent nowDocumentContent = nowDocumentRequest.get(0).getNowContent();
        final String defendantName = input.getNows().get(0).getRequestedMaterials().get(0).getNowVariantDefendant().getName();
        final uk.gov.justice.core.courts.Address address = input.getNows().get(0).getRequestedMaterials().get(0).getNowVariantDefendant().getAddress();
        final ProsecutionCaseIdentifier prosecutionCaseIdentifier = prosecutionCase.getProsecutionCaseIdentifier();
        final String expectedCaseRef = jurisdictionType == JurisdictionType.MAGISTRATES ? prosecutionCaseIdentifier.getProsecutionAuthorityReference() : prosecutionCaseIdentifier.getCaseURN();
        final LocalDate expectedConvictionDate = offence.getConvictionDate();

        assertThat(nowDocumentContent, isBean(NowDocumentContent.class)
                .with(NowDocumentContent::getOrderName, is(input.getNowTypes().get(0).getDescription()))
                .with(NowDocumentContent::getCourtCentreName, is(input.getHearing().getCourtCentre().getName()))
                .with(NowDocumentContent::getCourtClerkName, is(format("%s %s", input.getCourtClerk().getFirstName(), input.getCourtClerk().getLastName())))
                .with(NowDocumentContent::getOrderDate, is(input.getSharedResultLines().get(0).getOrderedDate().toString()))
                .with(NowDocumentContent::getDefendant, isBean(Nowdefendant.class)
                        .with(Nowdefendant::getName, is(defendantName))
                        .with(Nowdefendant::getAddress, isBean(Nowaddress.class)
                                .with(Nowaddress::getLine1, is(address.getAddress1()))
                                .with(Nowaddress::getLine2, nullValue())
                                .with(Nowaddress::getLine3, nullValue())
                                .with(Nowaddress::getLine4, nullValue())
                                .with(Nowaddress::getPostCode, nullValue())
                        )
                )
                .with(NowDocumentContent::getCaseUrns, hasItem(expectedCaseRef))
                .with(NowDocumentContent::getCases, first(isBean(ProsecutionCase.class)
                        .with(ProsecutionCase::getUrn, is(expectedCaseRef))
                        .with(ProsecutionCase::getDefendantCaseResults, first(isBean(Result.class)
                                .with(Result::getLabel, is(input.getSharedResultLines().get(0).getLabel()))
                                .with(Result::getPrompts, first(isBean(Prompt.class)
                                        .with(Prompt::getLabel, is(input.getSharedResultLines().get(0).getPrompts().get(0).getLabel()))
                                        .with(Prompt::getValue, is(input.getSharedResultLines().get(0).getPrompts().get(0).getValue()))
                                ))
                        ))
                        .with(ProsecutionCase::getDefendantCaseResults, first(isBean(Result.class)
                                .with(Result::getLabel, is(input.getSharedResultLines().get(1).getLabel()))
                                .with(Result::getPrompts, first(isBean(Prompt.class)
                                        .with(Prompt::getLabel, is(input.getSharedResultLines().get(1).getPrompts().get(0).getLabel()))
                                        .with(Prompt::getValue, is(input.getSharedResultLines().get(1).getPrompts().get(0).getValue()))
                                ))
                        ))
                        .with(ProsecutionCase::getDefendantCaseOffences, first(isBean(DefendantCaseOffence.class)
                                .with(DefendantCaseOffence::getTitle, is(offence.getOffenceTitle()))
                                .with(DefendantCaseOffence::getWelshTitle, is(offence.getOffenceTitleWelsh()))
                                .with(DefendantCaseOffence::getStartDate, is(offence.getStartDate().toString()))
                                .with(DefendantCaseOffence::getConvictionDate, is(expectedConvictionDate.toString()))
                                .with(DefendantCaseOffence::getResults, first(isBean(Result.class)
                                        .with(Result::getLabel, is(input.getSharedResultLines().get(2).getLabel()))
                                        .with(Result::getPrompts, first(isBean(Prompt.class)
                                                .with(Prompt::getLabel, is(input.getSharedResultLines().get(2).getPrompts().get(0).getLabel()))
                                                .with(Prompt::getValue, is(input.getSharedResultLines().get(2).getPrompts().get(0).getValue()))
                                        ))
                                ))

                        ))
                ))
                .with(NowDocumentContent::getNowText, is(input.getNowTypes().get(0).getStaticText()))
        );

    }

    @DataProvider
    public static Object[][] welshWordings() {
        return new Object[][]{
                {null, "English Wording", "English Wording"},
                {"N/A", "English Wording", "English Wording"},
                {"Welsh Wording", "English Wording", "Welsh Wording"}
        };
    }


    @UseDataProvider("welshWordings")
    @Test
    public void shouldSetWelshWordingWithEnglishWordingWhenItIsEmpty(String welshWording, String englishWording, String expectedWording) {
        final CreateNowsRequest input = TestTemplates.generateNowsRequestTemplateWithConditionalText(UUID.randomUUID(), JurisdictionType.MAGISTRATES, true);
        final Hearing hearing = input.getHearing();

        hearing.getProsecutionCases().stream()
                .flatMap(a -> a.getDefendants().stream())
                .flatMap(a -> a.getOffences().stream())
                .forEach((a) -> {
                    a.setWordingWelsh(welshWording);
                    a.setWording(englishWording);
                });

        final JsonEnvelope envelope = envelopeFrom(
                metadataWithRandomUUID("hearing.events.nows-requested"),
                objectToJsonObjectConverter.convert(input)
        );

        Subscriptions subscriptions = mock(Subscriptions.class);
        when(subscriptionClient.getAll(Mockito.any(JsonEnvelope.class), Mockito.any(UUID.class), Mockito.any(LocalDate.class))).thenReturn(subscriptions);

        final List<NowDocumentRequest> nowDocumentResult = nowsRequestedToDocumentConverter.convert(envelope, input);
        final NowDocumentContent nowDocumentContent = nowDocumentResult.get(0).getNowContent();

        final uk.gov.justice.core.courts.ProsecutionCase prosecutionCase = hearing.getProsecutionCases().get(0);
        assertThat(nowDocumentContent, isBean(NowDocumentContent.class)
                .with(NowDocumentContent::getCases, first(isBean(ProsecutionCase.class)
                        .with(ProsecutionCase::getDefendantCaseOffences, first(isBean(DefendantCaseOffence.class)
                                .withValue(DefendantCaseOffence::getWelshWording, expectedWording)
                        )))));
    }
}
