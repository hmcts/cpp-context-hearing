package uk.gov.moj.cpp.hearing.event;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.lang.String.format;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.AllOf.allOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.core.courts.AssociatedDefenceOrganisation.associatedDefenceOrganisation;
import static uk.gov.justice.progression.events.ApplicationOrganisationDetails.applicationOrganisationDetails;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloper;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher.jsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.metadata;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payloadIsJson;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;

import java.time.LocalDate;
import java.util.UUID;

import javax.json.JsonObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.justice.core.courts.AssociatedDefenceOrganisation;
import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.core.courts.DefenceOrganisation;
import uk.gov.justice.core.courts.FundingType;
import uk.gov.justice.core.courts.LaaReference;
import uk.gov.justice.core.courts.Organisation;
import uk.gov.justice.progression.events.ApplicationOrganisationDetails;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.test.utils.framework.api.JsonObjectConvertersFactory;

import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
//import ch.qos.logback.classic.LoggerContext;

@SuppressWarnings("unchecked")
@ExtendWith(MockitoExtension.class)
public class ApplicationDetailChangeEventProcessorTest {
    @Spy
    private final Enveloper enveloper = createEnveloper();

    @Spy
    private JsonObjectToObjectConverter jsonObjectConverter = new JsonObjectConvertersFactory().jsonObjectToObjectConverter();

    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter = new JsonObjectConvertersFactory().objectToJsonObjectConverter();

    @Mock
    private Sender sender;

    @Mock
    private Logger logger;

    @Captor
    private ArgumentCaptor<JsonEnvelope> envelopeArgumentCaptor;

    @InjectMocks
    private ApplicationDetailChangeEventProcessor applicationDetailChangeEventProcessor;


    @Test
    public void handleCourtApplicationChanged() throws Exception {
        final CourtApplication arbitraryCourtApplication = CourtApplication.courtApplication()
                .withId(randomUUID())
                .build();

        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("public.progression.court-application-updated"),
                createObjectBuilder()
                        .add("courtApplication",
                                objectToJsonObjectConverter.convert(arbitraryCourtApplication)
                        )
                        .build());

        applicationDetailChangeEventProcessor.handleCourtApplicationChanged(event);

        verify(this.sender).send(this.envelopeArgumentCaptor.capture());

        assertThat(this.envelopeArgumentCaptor.getValue(),
                jsonEnvelope(metadata().withName("hearing.update-court-application"), payloadIsJson(CoreMatchers.allOf(
                        withJsonPath("$.courtApplication.id", is(arbitraryCourtApplication.getId().toString()))))));
    }


    @Test
    public void handleApplicationLaaReferenceUpdated(){
        final LaaReference laaReference = LaaReference.laaReference()
                .withStatusDescription("desc")
                .withStatusDate(LocalDate.now())
                .withStatusId(randomUUID())
                .withApplicationReference("reference")
                .build();

        final JsonEnvelope envelope = envelopeFrom(metadataWithRandomUUID("public.progression.application-offences-updated"),
                createObjectBuilder()
                        .add("applicationId", randomUUID().toString())
                        .add("offenceId", randomUUID().toString())
                        .add("subjectId", randomUUID().toString())
                        .add("laaReference",
                                objectToJsonObjectConverter.convert(laaReference)
                        )
                        .build());
        when(logger.isInfoEnabled()).thenReturn(true);
        applicationDetailChangeEventProcessor.handleApplicationOffenceUpdated(envelope);

        verify(this.sender).send(this.envelopeArgumentCaptor.capture());
        assertThat(this.envelopeArgumentCaptor.getValue(),
                jsonEnvelope(metadata().withName("hearing.update-laareference-for-application"), payloadIsJson(CoreMatchers.allOf(
                        withJsonPath("$.laaReference.statusId", is(laaReference.getStatusId().toString()))))));

    }

    @Test
    public void handleApplicationLaaReferenceForApplicationWhenLoggerIsDisabled() {
        final LaaReference laaReference = LaaReference.laaReference()
                .withStatusDescription("desc")
                .withStatusDate(LocalDate.now())
                .withStatusId(randomUUID())
                .withApplicationReference("reference")
                .build();

        final JsonEnvelope envelope = envelopeFrom(metadataWithRandomUUID("public.progression.application-offences-updated"),
                createObjectBuilder()
                        .add("applicationId", randomUUID().toString())
                        .add("offenceId", randomUUID().toString())
                        .add("subjectId", randomUUID().toString())
                        .add("courtApplication",
                                objectToJsonObjectConverter.convert(laaReference)
                        )
                        .build());
        when(logger.isInfoEnabled()).thenReturn(false);
        applicationDetailChangeEventProcessor.handleApplicationOffenceUpdated(envelope);

        verify(this.sender).send(this.envelopeArgumentCaptor.capture());
    }

    @Test
    void handleApplicationDefenceOrganisationUpdated() {
        final UUID applicationId = randomUUID();
        final UUID subjectId = randomUUID();
        final AssociatedDefenceOrganisation associatedDefenceOrganisation = associatedDefenceOrganisation()
                .withDefenceOrganisation(DefenceOrganisation.defenceOrganisation()
                        .withLaaContractNumber("LAA123")
                        .withOrganisation(Organisation.organisation()
                                .withName("Org1")
                                .build())
                        .build())
                .withApplicationReference("ABC1234")
                .withAssociationStartDate(LocalDate.now())
                .withAssociationEndDate(LocalDate.now().plusYears(1))
                .withFundingType(FundingType.REPRESENTATION_ORDER)
                .withIsAssociatedByLAA(true)
                .build();
        final ApplicationOrganisationDetails applicationOrganisationDetails = applicationOrganisationDetails()
                .setApplicationId(applicationId)
                .setSubjectId(subjectId)
                .setAssociatedDefenceOrganisation(associatedDefenceOrganisation);

        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("public.progression.application-organisation-changed"),
                createObjectBuilder(objectToJsonObjectConverter.convert(applicationOrganisationDetails)).build());

        applicationDetailChangeEventProcessor.handleApplicationDefenceOrganisationUpdated(event);

        verify(this.sender).send(this.envelopeArgumentCaptor.capture());

        assertThat(this.envelopeArgumentCaptor.getValue(),
                jsonEnvelope(metadata().withName("hearing.update-defence-organisation-for-application"),
                        payloadIsJson(allOf(withJsonPath("$.applicationId", is(applicationOrganisationDetails.getApplicationId().toString()))))));
    }
}
