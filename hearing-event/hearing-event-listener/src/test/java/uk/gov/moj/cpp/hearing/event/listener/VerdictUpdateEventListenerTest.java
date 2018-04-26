package uk.gov.moj.cpp.hearing.event.listener;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.moj.cpp.hearing.domain.event.VerdictUpsert;
import uk.gov.moj.cpp.hearing.persist.entity.ex.Ahearing;
import uk.gov.moj.cpp.hearing.persist.entity.ex.Defendant;
import uk.gov.moj.cpp.hearing.persist.entity.ex.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.persist.entity.ex.Offence;
import uk.gov.moj.cpp.hearing.repository.AhearingRepository;

import java.util.UUID;

import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataWithRandomUUID;
import static uk.gov.justice.services.test.utils.common.reflection.ReflectionUtils.setField;
import static uk.gov.justice.services.test.utils.core.messaging.JsonEnvelopeBuilder.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.BOOLEAN;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.INTEGER;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_LOCAL_DATE;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;


@RunWith(MockitoJUnitRunner.class)
public class VerdictUpdateEventListenerTest {

    @Mock
    private AhearingRepository ahearingRepository;

    @InjectMocks
    private VerdictUpdateEventListener verdictUpdateEventListener;

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
    public void verdictUpdate_shouldUpdateTheVerdict() throws Exception {

        UUID hearingId = randomUUID();

        VerdictUpsert verdictUpsert = VerdictUpsert.builder()
                .withCaseId(randomUUID())
                .withHearingId(hearingId)
                .withOffenceId(randomUUID())
                .withVerdictId(randomUUID())
                .withVerdictValueId(randomUUID())
                .withCategory(STRING.next())
                .withCode(STRING.next())
                .withDescription(STRING.next())
                .withNumberOfJurors(INTEGER.next())
                .withNumberOfSplitJurors(INTEGER.next())
                .withUnanimous(BOOLEAN.next())
                .withVerdictDate(PAST_LOCAL_DATE.next())
                .build();

        Ahearing ahearing = Ahearing.builder()
                .withId(hearingId)
                .withDefendants(asList
                        (Defendant.builder()
                                .withOffences(asList(
                                        Offence.builder()
                                                .withId(new HearingSnapshotKey(verdictUpsert.getOffenceId(), hearingId))
                                                .build()
                                ))
                                .build()))
                .build();

        when(this.ahearingRepository.findById(hearingId)).thenReturn(ahearing);

        verdictUpdateEventListener.verdictUpdate(envelopeFrom(metadataWithRandomUUID("hearing.offence-verdict-updated"),
                objectToJsonObjectConverter.convert(verdictUpsert)));

        verify(this.ahearingRepository).save(ahearing);

        Offence offence = ahearing.getDefendants().get(0).getOffences().get(0);

        assertThat(offence.getId().getId(), is(verdictUpsert.getOffenceId()));
        assertThat(offence.getId().getHearingId(), is(verdictUpsert.getHearingId()));
        assertThat(offence.getVerdictCategory(), is(verdictUpsert.getCategory()));
        assertThat(offence.getVerdictCode(), is(verdictUpsert.getCode()));
        assertThat(offence.getVerdictDescription(), is(verdictUpsert.getDescription()));
        assertThat(offence.getNumberOfJurors(), is(verdictUpsert.getNumberOfJurors()));
        assertThat(offence.getNumberOfSplitJurors(), is(verdictUpsert.getNumberOfSplitJurors()));
        assertThat(offence.getUnanimous(), is(verdictUpsert.getUnanimous()));
        assertThat(offence.getVerdictDate(), is(verdictUpsert.getVerdictDate()));
    }
}