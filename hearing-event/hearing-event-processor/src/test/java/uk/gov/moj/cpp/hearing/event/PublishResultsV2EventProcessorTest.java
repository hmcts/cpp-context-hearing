package uk.gov.moj.cpp.hearing.event;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.moj.cpp.hearing.event.NowsTemplates.resultsSharedTemplate;

import uk.gov.justice.core.courts.LjaDetails;
import uk.gov.justice.hearing.courts.referencedata.Address;
import uk.gov.justice.hearing.courts.referencedata.OrganisationalUnit;
import uk.gov.justice.hearing.courts.referencedata.Prosecutor;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.test.utils.framework.api.JsonObjectConvertersFactory;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsShared;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsSharedV2;
import uk.gov.moj.cpp.hearing.event.delegates.PublishResultsDelegate;
import uk.gov.moj.cpp.hearing.event.delegates.UpdateDefendantWithApplicationDetailsDelegate;
import uk.gov.moj.cpp.hearing.event.delegates.UpdateResultLineStatusDelegate;
import uk.gov.moj.cpp.hearing.event.relist.ResultsSharedFilter;
import uk.gov.moj.cpp.hearing.event.service.ReferenceDataService;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PublishResultsV2EventProcessorTest {

    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter = new JsonObjectConvertersFactory().jsonObjectToObjectConverter();

    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter = new JsonObjectConvertersFactory().objectToJsonObjectConverter();


    @Mock
    private Sender sender;

    @Mock
    private UpdateResultLineStatusDelegate updateResultLineStatusDelegate;

    @Mock
    private PublishResultsDelegate publishResultsDelegate;

    @Mock
    private UpdateDefendantWithApplicationDetailsDelegate updateDefendantWithApplicationDetailsDelegate;

    @Mock
    private ResultsSharedFilter resultsSharedFilter;

    @Mock
    private ReferenceDataService referenceDataService;

//    @Mock
//    private NewTargetToLegacyTargetConverter newTargetToLegacyTargetConverter;

    @InjectMocks
    private PublishResultsV2EventProcessor publishResultsEventProcessor;

    @Captor
    private ArgumentCaptor<Sender> senderArgumentCaptor;

    @Captor
    private ArgumentCaptor<JsonEnvelope> eventArgumentCaptor;

    @Captor
    private ArgumentCaptor<ResultsShared> publishResultDelegateCaptor;


    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldShareResultCorrectly() {

        final ResultsShared resultsShared = resultsSharedTemplate();
        final ResultsSharedV2 resultsSharedV2 = ResultsSharedV2.builder()
                .withSharedTime(resultsShared.getSharedTime())
                .withCompletedResultLinesStatus(resultsShared.getCompletedResultLinesStatus())
                .withCourtClerk(resultsShared.getCourtClerk())
                .withDefendantDetailsChanged(resultsShared.getDefendantDetailsChanged())
                .withHearing(resultsShared.getHearing())
                .withHearingId(resultsShared.getHearingId())
                .withSavedTargets(resultsShared.getSavedTargets())
                .withTargets(resultsShared.getTargets())
                .withVariantDirectory(resultsShared.getVariantDirectory())
                .build();
        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("hearing.results-shared"),
                objectToJsonObjectConverter.convert(resultsShared));

        when(referenceDataService.getProsecutorById(eq(event), eq(resultsShared.getHearing().getProsecutionCases().get(0).getProsecutionCaseIdentifier().getProsecutionAuthorityId())))
                .thenReturn(prosecutorTemplate());
        when(referenceDataService.getOrganisationUnitById(eq(event), eq(resultsShared.getHearing().getCourtCentre().getId())))
                .thenReturn(OrganisationalUnit.organisationalUnit()
                        .withOucode("123ABCD")
                        .withIsWelsh(true)
                        .withOucodeL3WelshName("Welsh Court Centre")
                        .withWelshAddress1("Welsh 1")
                        .withWelshAddress2("Welsh 2")
                        .withWelshAddress3("Welsh 3")
                        .withWelshAddress4("Welsh 4")
                        .withWelshAddress5("Welsh 5")
                        .withPostcode("LL55 2DF")
                        .build());
        when(jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(), ResultsSharedV2.class)).thenReturn(resultsSharedV2);
        //when(newTargetToLegacyTargetConverter.convert(anyList())).thenReturn(resultsSharedV2.getTargets());
       publishResultsEventProcessor.resultsShared(event);

        verify(publishResultsDelegate).shareResults(event, sender, resultsSharedV2);

       }


    private Prosecutor prosecutorTemplate() {
        return Prosecutor.prosecutor()
                .withId(UUID.randomUUID().toString())
                .withFullName("Full Name")
                .withOucode("OU code")
                .withAddress(Address.address()
                        .withAddress1("Address line 1")
                        .withAddress2("Address line 2")
                        .withAddress3("Address line 3")
                        .withAddress4("Address line 4")
                        .withAddress5("Address line 5")
                        .withPostcode("MK9 2BQ")
                        .build())
                .withMajorCreditorCode("TFL2")
                .withInformantEmailAddress("informant@email.com")
                .build();
    }



    private void verifyOrganisationalUnitInformation(ResultsShared resultsShared) {

        assertThat("123ABCD", equalTo(resultsShared.getHearing().getCourtCentre().getCode()));
        assertThat("Welsh 1", equalTo(resultsShared.getHearing().getCourtCentre().getWelshAddress().getAddress1()));
        assertThat("Welsh 2", equalTo(resultsShared.getHearing().getCourtCentre().getWelshAddress().getAddress2()));
        assertThat("Welsh 3", equalTo(resultsShared.getHearing().getCourtCentre().getWelshAddress().getAddress3()));
        assertThat("Welsh 4", equalTo(resultsShared.getHearing().getCourtCentre().getWelshAddress().getAddress4()));
        assertThat("Welsh 5", equalTo(resultsShared.getHearing().getCourtCentre().getWelshAddress().getAddress5()));
        assertThat("LL55 2DF", equalTo(resultsShared.getHearing().getCourtCentre().getWelshAddress().getPostcode()));
        assertThat("Welsh LJA Name", equalTo(resultsShared.getHearing().getCourtCentre().getLja().getWelshLjaName()));
        assertTrue(resultsShared.getHearing().getCourtCentre().getWelshCourtCentre());
    }
}
