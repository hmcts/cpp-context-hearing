package uk.gov.moj.cpp.hearing.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.justice.progression.events.CaseDefendantDetails;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.defendant.Person;
import uk.gov.moj.cpp.hearing.domain.event.CaseDefendantDetailsWithHearings;

import java.util.Arrays;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataWithRandomUUID;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloper;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher.jsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.metadata;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payloadIsJson;
import static uk.gov.justice.services.test.utils.core.messaging.JsonEnvelopeBuilder.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_LOCAL_DATE;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;

@SuppressWarnings("unchecked")
@RunWith(MockitoJUnitRunner.class)
public class CaseDefendantDetailsChangeEventProcessorTest {

    @Spy
    private final Enveloper enveloper = createEnveloper();

    @Spy
    private final ObjectMapper objectMapper = new ObjectMapperProducer().objectMapper();

    @InjectMocks
    private final ObjectToJsonObjectConverter objectToJsonObjectConverter = new ObjectToJsonObjectConverter();

    @Mock
    private Sender sender;

    @Captor
    private ArgumentCaptor<JsonEnvelope> envelopeArgumentCaptor;

    @InjectMocks
    private CaseDefendantDetailsChangeEventProcessor caseDefendantDetailsChangeEventProcessor;

    @Test
    public void processPublicCaseDefendantChanged() {

        CaseDefendantDetails defendantDetailsChangedPublicEvent = CaseDefendantDetails.builder()
                .withCaseId(randomUUID())
                .addDefendant(uk.gov.moj.cpp.hearing.command.defendant.Defendant.builder()
                        .withId(randomUUID())
                        .withPerson(Person.builder()
                                .withId(randomUUID())
                                .withFirstName(STRING.next())
                                .withLastName(STRING.next())
                                .withNationality(STRING.next())
                                .withGender(STRING.next())
                                .withAddress(generateAddress())
                                .withDateOfBirth(PAST_LOCAL_DATE.next()))
                        .withBailStatus(STRING.next())
                        .withCustodyTimeLimitDate(PAST_LOCAL_DATE.next())
                        .withDefenceOrganisation(STRING.next())
                        .withInterpreter(uk.gov.moj.cpp.hearing.command.defendant.Interpreter.builder(STRING.next())))
                .build();

        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("public.progression.case-defendant-changed"),
                objectToJsonObjectConverter.convert(defendantDetailsChangedPublicEvent));

        caseDefendantDetailsChangeEventProcessor.processPublicCaseDefendantChanged(event);

        verify(this.sender).send(this.envelopeArgumentCaptor.capture());

        assertThat(
                this.envelopeArgumentCaptor.getValue(), jsonEnvelope(
                        metadata().withName("hearing.update-case-defendant-details"),
                        payloadIsJson(allOf(
                                withJsonPath("$.caseId", is(defendantDetailsChangedPublicEvent.getCaseId().toString()))
                                )
                        )
                )
        );
    }

    @Test
    public void enrichDefendantDetails() {

        final CaseDefendantDetailsWithHearings caseDefendantDetailsWithHearings = CaseDefendantDetailsWithHearings.builder()
                .withCaseId(randomUUID())
                .withDefendant(uk.gov.moj.cpp.hearing.command.defendant.Defendant.builder()
                        .withId(randomUUID())
                        .withPerson(Person.builder()
                                .withId(randomUUID())
                                .withFirstName(STRING.next())
                                .withLastName(STRING.next())
                                .withNationality(STRING.next())
                                .withGender(STRING.next())
                                .withAddress(generateAddress())
                                .withDateOfBirth(PAST_LOCAL_DATE.next()))
                        .withBailStatus(STRING.next())
                        .withCustodyTimeLimitDate(PAST_LOCAL_DATE.next())
                        .withDefenceOrganisation(STRING.next())
                        .withInterpreter(uk.gov.moj.cpp.hearing.command.defendant.Interpreter.builder(STRING.next()))
                )
                .withHearingIds(Arrays.asList(randomUUID(), randomUUID()))
                .build();

        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("hearing.update-case-defendant-details-enriched-with-hearing-ids"), objectToJsonObjectConverter.convert(caseDefendantDetailsWithHearings));

        caseDefendantDetailsChangeEventProcessor.enrichDefendantDetails(event);

        verify(this.sender).send(this.envelopeArgumentCaptor.capture());

        assertThat(
                this.envelopeArgumentCaptor.getValue(), jsonEnvelope(
                        metadata().withName("hearing.update-case-defendant-details-against-hearing-aggregate"),
                        payloadIsJson(allOf(
                                withJsonPath("$.caseId", is(caseDefendantDetailsWithHearings.getCaseId().toString()))
                                )
                        )
                )
        );
    }

    private uk.gov.moj.cpp.hearing.command.defendant.Address.Builder generateAddress() {
        return uk.gov.moj.cpp.hearing.command.defendant.Address.address()
                .withAddress1(STRING.next())
                .withAddress2(STRING.next())
                .withAddress3(STRING.next())
                .withAddress4(STRING.next())
                .withPostcode(STRING.next());
    }
}
