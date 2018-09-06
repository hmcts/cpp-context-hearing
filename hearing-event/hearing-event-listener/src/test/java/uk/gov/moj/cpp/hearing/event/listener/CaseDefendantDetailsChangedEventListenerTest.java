package uk.gov.moj.cpp.hearing.event.listener;

import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.common.reflection.ReflectionUtils.setField;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_LOCAL_DATE;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.justice.json.schemas.core.Gender;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.justice.services.test.utils.core.random.RandomGenerator;
import uk.gov.moj.cpp.hearing.command.defendant.Address;
import uk.gov.moj.cpp.hearing.command.defendant.Interpreter;
import uk.gov.moj.cpp.hearing.command.defendant.Person;
import uk.gov.moj.cpp.hearing.domain.event.DefendantDetailsUpdated;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Defendant;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.persist.entity.ha.PersonDefendant;
import uk.gov.moj.cpp.hearing.repository.DefendantRepository;

import javax.json.JsonObject;
import java.util.UUID;

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

        final JsonEnvelope envelope = getDefendantJsonEnvelope(hearingId, defendantId);

        final Defendant defendant = new Defendant();

        uk.gov.moj.cpp.hearing.persist.entity.ha.Address address = new uk.gov.moj.cpp.hearing.persist.entity.ha.Address();
        address.setAddress1(STRING.next());
        address.setAddress2(STRING.next());
        address.setAddress3(STRING.next());
        address.setAddress4(STRING.next());
        address.setAddress5(STRING.next());
        address.setPostCode(STRING.next());

        uk.gov.moj.cpp.hearing.persist.entity.ha.Contact contact = new uk.gov.moj.cpp.hearing.persist.entity.ha.Contact();
        contact.setFax(STRING.next());
        contact.setSecondaryEmail(STRING.next());
        contact.setPrimaryEmail(STRING.next());
        contact.setMobile(STRING.next());

        uk.gov.moj.cpp.hearing.persist.entity.ha.Person person = new uk.gov.moj.cpp.hearing.persist.entity.ha.Person();
        person.setFirstName(STRING.next());
        person.setLastName(STRING.next());
        person.setAddress(address);
        person.setContact(contact);

        PersonDefendant personDefendant = new PersonDefendant();
        personDefendant.setPersonDetails(person);

        defendant.setPersonDefendant(personDefendant);

        when(defendantRepository.findBy(new HearingSnapshotKey(defendantId, hearingId))).thenReturn(defendant);

        caseDefendantDetailsChangedEventListener.defendantDetailsUpdated(envelope);

        ArgumentCaptor<Defendant> defendantexArgumentCaptor = ArgumentCaptor.forClass(Defendant.class);

        verify(defendantRepository).save(defendantexArgumentCaptor.capture());

        final Defendant defendantOut = defendantexArgumentCaptor.getValue();

        assertThat(defendant.getId(), is(defendantOut.getId()));
    }

    private JsonEnvelope getDefendantJsonEnvelope(final UUID hearingId, final UUID defendantId) {

        DefendantDetailsUpdated document = DefendantDetailsUpdated.defendantDetailsUpdated()
                .setCaseId(randomUUID())
                .setHearingId(hearingId)
                .setDefendant(uk.gov.moj.cpp.hearing.command.defendant.Defendant.defendant()
                        .setId(defendantId)
                        .setPerson(Person.person().setId(randomUUID())
                                .setFirstName(STRING.next())
                                .setLastName(STRING.next())
                                .setNationality(STRING.next())
                                .setGender(RandomGenerator.values(Gender.values()).next())
                                .setAddress(Address.address()
                                        .setAddress1(STRING.next())
                                        .setAddress2(STRING.next())
                                        .setAddress3(STRING.next())
                                        .setAddress4(STRING.next())
                                        .setPostCode(STRING.next())
                                )
                                .setFax(STRING.next())
                                .setHomeTelephone(STRING.next())
                                .setDateOfBirth(PAST_LOCAL_DATE.next()))
                        .setBailStatus(STRING.next())
                        .setCustodyTimeLimitDate(PAST_LOCAL_DATE.next())
                        .setDefenceOrganisation(STRING.next())
                        .setInterpreter(Interpreter.interpreter().setLanguage(STRING.next()))
                );

        JsonObject jsonObject = objectToJsonObjectConverter.convert(document);

        return envelopeFrom((Metadata) null, jsonObject);
    }

}
