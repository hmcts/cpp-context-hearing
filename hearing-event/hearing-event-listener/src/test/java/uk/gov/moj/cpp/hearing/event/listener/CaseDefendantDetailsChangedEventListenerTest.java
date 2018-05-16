package uk.gov.moj.cpp.hearing.event.listener;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.messaging.DefaultJsonEnvelope;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.defendant.Address;
import uk.gov.moj.cpp.hearing.command.defendant.Interpreter;
import uk.gov.moj.cpp.hearing.command.defendant.Person;
import uk.gov.moj.cpp.hearing.domain.event.DefendantDetailsUpdated;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Defendant;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.repository.DefendantRepository;

import javax.json.JsonObject;
import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.common.reflection.ReflectionUtils.setField;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_LOCAL_DATE;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;

@RunWith(MockitoJUnitRunner.class)
public class CaseDefendantDetailsChangedEventListenerTest {

    @InjectMocks
    private CaseDefendantDetailsChangedEventListener caseDefendantDetailsChangedEventListener;

    @Mock
    private DefendantRepository defendantRepository;

    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @Before
    public void setup() {
        setField(this.jsonObjectToObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
    }

    @Test
    public void shouldUpdateDefendant() {

        final UUID hearingId = randomUUID();

        final UUID defendantId = randomUUID();

        JsonEnvelope envelope = getDefendantJsonEnvelope(hearingId, defendantId);

        Defendant defendant = Defendant.builder()
                .withAddress(uk.gov.moj.cpp.hearing.persist.entity.ha.Address.builder()
                        .withAddress1(STRING.next())
                        .withAddress2(STRING.next())
                        .withAddress3(STRING.next())
                        .withAddress4(STRING.next())
                        .withPostCode(STRING.next())
                        .build())
                .withFirstName(STRING.next())
                .withLastName(STRING.next())
                .build();

        when(defendantRepository.findBy(new HearingSnapshotKey(defendantId, hearingId))).thenReturn(defendant);

        caseDefendantDetailsChangedEventListener.defendantDetailsUpdated(envelope);

        ArgumentCaptor<Defendant> defendantexArgumentCaptor = ArgumentCaptor.forClass(Defendant.class);

        verify(defendantRepository).saveAndFlush(defendantexArgumentCaptor.capture());

        final Defendant defendantOut = defendantexArgumentCaptor.getValue();

        assertThat(defendant.getId(), is(defendantOut.getId()));
    }

    private JsonEnvelope getDefendantJsonEnvelope(final UUID hearingId, final UUID defendantId) {

        DefendantDetailsUpdated document = DefendantDetailsUpdated.builder()
                .withCaseId(randomUUID())
                .withHearingId(hearingId)
                .withDefendant(
                        uk.gov.moj.cpp.hearing.command.defendant.Defendant.builder()
                                .withId(defendantId)
                                .withPerson(Person.builder().withId(randomUUID())
                                .withFirstName(STRING.next())
                                .withLastName(STRING.next())
                                .withNationality(STRING.next())
                                .withGender(STRING.next())
                                .withAddress(generateAddress())
                                .withDateOfBirth(PAST_LOCAL_DATE.next()))
                                .withBailStatus(STRING.next())
                                .withCustodyTimeLimitDate(PAST_LOCAL_DATE.next())
                                .withDefenceOrganisation(STRING.next())
                                .withInterpreter(Interpreter.builder(STRING.next())))
                .build();

        JsonObject jsonObject = objectToJsonObjectConverter.convert(document);

        return new DefaultJsonEnvelope(null, jsonObject);
    }

    private Address.Builder generateAddress() {
        return Address.address()
                .withAddress1(STRING.next())
                .withAddress2(STRING.next())
                .withAddress3(STRING.next())
                .withAddress4(STRING.next())
                .withPostcode(STRING.next());
    }

}
