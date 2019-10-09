package uk.gov.moj.cpp.hearing.event.listener;

import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.defendantTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.createCourtApplications;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.moj.cpp.hearing.domain.event.DefendantDetailsUpdated;
import uk.gov.moj.cpp.hearing.mapping.CourtApplicationsSerializer;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Defendant;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCase;
import uk.gov.moj.cpp.hearing.repository.DefendantRepository;
import uk.gov.moj.cpp.hearing.repository.HearingRepository;


import java.util.UUID;

import javax.json.JsonObject;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CaseDefendantDetailsUpdatedEventListenerTest {

    @InjectMocks
    private CaseDefendantDetailsUpdatedEventListener caseDefendantDetailsUpdatedEventListener;

    @Mock
    private DefendantRepository defendantRepository;

    @Mock
    HearingRepository hearingRepository;

    @Mock
    CourtApplicationsSerializer   courtApplicationsSerializer;

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


        final Hearing hearing = new Hearing();
        hearing.setId(hearingId);

        final JsonEnvelope envelope = createJsonEnvelope(defendantDetailsUpdated);

        final Defendant defendant = new Defendant();

        defendant.setId(new HearingSnapshotKey(defendantDetailsUpdated.getDefendant().getId(), hearingId));

        final ProsecutionCase prosecutionCase = new ProsecutionCase();

        prosecutionCase.setId(new HearingSnapshotKey(defendantDetailsUpdated.getDefendant().getProsecutionCaseId(), hearingId));

        defendant.setProsecutionCase(prosecutionCase);

        when(defendantRepository.findBy(defendant.getId())).thenReturn(defendant);

        when(hearingRepository.findBy(hearingId)).thenReturn(hearing);

        when(courtApplicationsSerializer.courtApplications(courtApplicationString)).thenReturn(createCourtApplications());

        caseDefendantDetailsUpdatedEventListener.defendantDetailsUpdated(envelope);

        final ArgumentCaptor<Defendant> defendantexArgumentCaptor = ArgumentCaptor.forClass(Defendant.class);

        verify(defendantRepository).save(defendantexArgumentCaptor.capture());

        final Defendant defendantOut = defendantexArgumentCaptor.getValue();

        assertThat(defendant.getId(), is(defendantOut.getId()));
    }

    private JsonEnvelope createJsonEnvelope(final DefendantDetailsUpdated defendantDetailsUpdated) {

        final JsonObject jsonObject = objectToJsonObjectConverter.convert(defendantDetailsUpdated);

        return envelopeFrom((Metadata) null, jsonObject);
    }

    final String  courtApplicationString = "{\"courtApplications\":[{\"applicant\":{\"id\":\"2ad94baf-0e75-477b-b4c7-8e4b71dbdca1\",\"" +
            "organisation\":{\"name\":\"OrganisationName\"},\"personDetails\":{\"firstName\":\"Lauren\",\"gender\":\"FEMALE\",\"lastName\":\"Michelle\",\"middleName\":\"Mia\"}},\"id\":\"84bf7fac-3189-432e-970d-de0f33b9fd29\",\"" +
            "respondents\":[{\"partyDetails\":{\"id\":\"347b5388-6aa7-4b4c-bc07-8ce3be090a79\",\"organisation\":{\"name\":\"OrganisationName\"},\"" +
            "personDetails\":{\"firstName\":\"Gerald\",\"gender\":\"MALE\",\"lastName\":\"Harrison\"}}}]}]}";

}
