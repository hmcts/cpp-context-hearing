package uk.gov.moj.cpp.hearing.event.listener;

import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.common.reflection.ReflectionUtils.setField;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.defendantTemplate;

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
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.moj.cpp.hearing.domain.event.DefendantDetailsUpdated;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Defendant;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCase;
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

        final DefendantDetailsUpdated defendantDetailsUpdated = DefendantDetailsUpdated.defendantDetailsUpdated()
                .setHearingId(hearingId)
                .setDefendant(defendantTemplate());

        final JsonEnvelope envelope = createJsonEnvelope(defendantDetailsUpdated);

        final Defendant defendant = new Defendant();

        defendant.setId(new HearingSnapshotKey(defendantDetailsUpdated.getDefendant().getId(), hearingId));

        final ProsecutionCase prosecutionCase = new ProsecutionCase();

        prosecutionCase.setId(new HearingSnapshotKey(defendantDetailsUpdated.getDefendant().getProsecutionCaseId(), hearingId));

        defendant.setProsecutionCase(prosecutionCase);

        when(defendantRepository.findBy(defendant.getId())).thenReturn(defendant);

        caseDefendantDetailsChangedEventListener.defendantDetailsUpdated(envelope);

        final ArgumentCaptor<Defendant> defendantexArgumentCaptor = ArgumentCaptor.forClass(Defendant.class);

        verify(defendantRepository).save(defendantexArgumentCaptor.capture());

        final Defendant defendantOut = defendantexArgumentCaptor.getValue();

        assertThat(defendant.getId(), is(defendantOut.getId()));
    }

    private JsonEnvelope createJsonEnvelope(final DefendantDetailsUpdated defendantDetailsUpdated) {

        final JsonObject jsonObject = objectToJsonObjectConverter.convert(defendantDetailsUpdated);

        return envelopeFrom((Metadata) null, jsonObject);
    }

}
