package uk.gov.moj.cpp.hearing.event.delegates;

import static java.util.Collections.singletonList;
import static java.util.Optional.of;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloper;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;

import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.core.courts.CourtApplicationParty;
import uk.gov.justice.core.courts.CourtApplicationType;
import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.MasterDefendant;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.justice.core.courts.ResultLine;
import uk.gov.justice.core.courts.Target;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.justice.services.test.utils.framework.api.JsonObjectConvertersFactory;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsShared;
import uk.gov.moj.cpp.hearing.nces.UpdateDefendantWithApplicationDetails;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.json.JsonObject;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;


public class UpdateDefendantWithApplicationDetailsDelegateTest {

    private static final String COMMAND_NAME = "hearing.command.update-defendant-with-application-details";

    private ResultsShared.Builder resultsSharedBuilder;

    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter = new JsonObjectConvertersFactory().jsonObjectToObjectConverter();

    @Spy
    private final ObjectToJsonObjectConverter objectToJsonObjectConverter = new JsonObjectConvertersFactory().objectToJsonObjectConverter();

    @Mock
    Sender sender;

    @Spy
    private final Enveloper enveloper = createEnveloper();

    @InjectMocks
    UpdateDefendantWithApplicationDetailsDelegate updateDefendantWithApplicationDetailsDelegateUnderTheTest;

    @Captor
    private ArgumentCaptor<JsonEnvelope> notifyNotificationArgumentCaptor;

    private UUID applicationId;
    private UUID applicationTypeId;
    private UUID applicationOutcomeTypeId;
    private UUID defendantId;


    @Before
    public void setUp(){
        MockitoAnnotations.initMocks(this);
        resultsSharedBuilder = ResultsShared.builder();
        applicationId = UUID.randomUUID();
        applicationOutcomeTypeId = UUID.randomUUID();
        applicationTypeId = UUID.randomUUID();
        defendantId = UUID.randomUUID();
    }

    @Test
    public void should_send_notification_when_there_is_at_least_one_result() {

        UUID identicalApplicationId = applicationId;
        addTargetWithResultLines(resultsSharedBuilder, identicalApplicationId, of(createResultLines(UUID.randomUUID())));
        addHearingWithCourApplications(resultsSharedBuilder,
                createCourtApplicationWithIds(identicalApplicationId, applicationTypeId,
                                                applicationOutcomeTypeId, defendantId));


        ResultsShared resultsShared = resultsSharedBuilder.build();
        JsonEnvelope event = envelopeFrom(metadataWithRandomUUID(COMMAND_NAME),
                objectToJsonObjectConverter.convert(resultsShared));

        updateDefendantWithApplicationDetailsDelegateUnderTheTest.execute(sender, event, resultsShared);

        verify(sender, times(1)).sendAsAdmin(notifyNotificationArgumentCaptor.capture());

        UpdateDefendantWithApplicationDetails updateDefendantWithApplicationDetails = getNotificationFromPayloadWeSent();

        Assert.assertThat(COMMAND_NAME, is(getCommandNameFromMetaDataWeSent()));
        Assert.assertThat(updateDefendantWithApplicationDetails.getApplicationTypeId(), is(applicationTypeId));
        Assert.assertThat(updateDefendantWithApplicationDetails.getDefendantId(), is(defendantId));
    }

    @Test
    public void should_NOT_send_notification_when_there_is_no_result() {

        UUID differentApplicationId = UUID.randomUUID();
        addTargetWithResultLines(resultsSharedBuilder, differentApplicationId, of(createResultLines(UUID.randomUUID())));
        addHearingWithCourApplications(resultsSharedBuilder,
                createCourtApplicationWithIds(applicationId, applicationTypeId,
                        applicationOutcomeTypeId, defendantId));


        ResultsShared resultsShared = resultsSharedBuilder.build();
        JsonEnvelope event = envelopeFrom(metadataWithRandomUUID(COMMAND_NAME),
                objectToJsonObjectConverter.convert(resultsShared));

        updateDefendantWithApplicationDetailsDelegateUnderTheTest.execute(sender, event, resultsShared);

        verify(sender, never()).sendAsAdmin(any());
    }

    @Test
    public void should_NOT_send_notification_when_there_is_result_with_NO_result_definition_id() {

        UUID identicalApplicationId = applicationId;
        addTargetWithResultLines(resultsSharedBuilder,
                                identicalApplicationId,
                                of(createResultLinesWithResultDefinitionId()));
        addHearingWithCourApplications(resultsSharedBuilder,
                                createCourtApplicationWithIds(identicalApplicationId, applicationTypeId,
                                                                applicationOutcomeTypeId, defendantId));


        ResultsShared resultsShared = resultsSharedBuilder.build();
        JsonEnvelope event = envelopeFrom(metadataWithRandomUUID(COMMAND_NAME),
                objectToJsonObjectConverter.convert(resultsShared));

        updateDefendantWithApplicationDetailsDelegateUnderTheTest.execute(sender, event, resultsShared);

        verify(sender, never()).sendAsAdmin(any());
    }

    private void addHearingWithCourApplications(ResultsShared.Builder resultsSharedBuilder, List<CourtApplication> courtApplications) {
        Hearing hearing = Hearing.hearing().build();
        hearing.setProsecutionCases(createProsecutionCases());
        hearing.setCourtApplications(courtApplications);
        resultsSharedBuilder.withHearing(hearing);
    }

    private List<ProsecutionCase> createProsecutionCases() {
        return singletonList(ProsecutionCase.prosecutionCase()
                .withDefendants(singletonList(Defendant.defendant()
                        .withId(UUID.randomUUID())
                        .build()))
                .build());
    }


    private void addTargetWithResultLines(ResultsShared.Builder resultsSharedBuilder, UUID applicationId,  Optional<List<ResultLine>> resultLinesOpt) {
        Target target = Target.target()
                                .withApplicationId(applicationId)
                                .build();
        resultLinesOpt.ifPresent(target::setResultLines);
        resultsSharedBuilder.withTargets(singletonList(target));
    }

    private List<ResultLine> createResultLines(UUID resultDefinitionId) {
        return singletonList(ResultLine.resultLine()
                                        .withResultDefinitionId(resultDefinitionId)
                                        .build());
    }

    private List<ResultLine> createResultLinesWithResultDefinitionId() {
        return createResultLines(null);
    }

    private List<CourtApplication> createCourtApplicationWithIds(UUID applicationId,
                                                                 UUID applicationTypeId,
                                                                 UUID applicationOutcomeTypeId,
                                                                 UUID defendantId) {
        CourtApplication.Builder courtApplicationBuilder = CourtApplication.courtApplication()
                                            .withId(applicationId)
                                            .withApplicant(CourtApplicationParty.courtApplicationParty()
                                                    .withMasterDefendant(MasterDefendant.masterDefendant()
                                                                            .withMasterDefendantId(defendantId)
                                                                            .build())
                                                    .build())
                                            .withType(CourtApplicationType.courtApplicationType()
                                                                            .withId(applicationTypeId).build());


        return singletonList(courtApplicationBuilder.build());
    }

    private UpdateDefendantWithApplicationDetails getNotificationFromPayloadWeSent() {
        JsonObject payload = notifyNotificationArgumentCaptor.getValue().payloadAsJsonObject();
        return jsonObjectToObjectConverter.convert(payload, UpdateDefendantWithApplicationDetails.class);
    }

    private String getCommandNameFromMetaDataWeSent() {
        Metadata metadata = notifyNotificationArgumentCaptor.getValue().metadata();
        return metadata.name();
    }

}
