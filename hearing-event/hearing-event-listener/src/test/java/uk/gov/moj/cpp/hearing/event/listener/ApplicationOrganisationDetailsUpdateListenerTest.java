package uk.gov.moj.cpp.hearing.event.listener;

import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.core.courts.AssociatedDefenceOrganisation.associatedDefenceOrganisation;
import static uk.gov.justice.core.courts.CourtApplication.courtApplication;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;

import uk.gov.justice.core.courts.AssociatedDefenceOrganisation;
import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.core.courts.CourtApplicationParty;
import uk.gov.justice.core.courts.DefenceOrganisation;
import uk.gov.justice.core.courts.FundingType;
import uk.gov.justice.core.courts.Organisation;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.moj.cpp.hearing.domain.event.ApplicationOrganisationDetailsUpdatedForHearing;
import uk.gov.moj.cpp.hearing.mapping.CourtApplicationsSerializer;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.repository.HearingRepository;

import java.io.StringReader;
import java.time.LocalDate;
import java.util.UUID;

import uk.gov.justice.services.messaging.JsonObjects;
import javax.json.JsonArray;
import javax.json.JsonObject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ApplicationOrganisationDetailsUpdateListenerTest {

    @Mock
    HearingRepository hearingRepository;
    @InjectMocks
    private ApplicationOrganisationDetailsUpdatedEventListener applicationOrganisationDetailsUpdatedEventListener;

    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @Spy
    private CourtApplicationsSerializer courtApplicationsSerializer;

    @BeforeEach
    void setup() {
        setField(this.jsonObjectToObjectConverter, "objectMapper", new ObjectMapperProducer().objectMapper());
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
        setField(this.courtApplicationsSerializer, "jsonObjectToObjectConverter", jsonObjectToObjectConverter);
        setField(this.courtApplicationsSerializer, "objectToJsonObjectConverter", objectToJsonObjectConverter);
    }

    @Test
    void testApplicationDefenceOrganisationUpdated() {
        final UUID hearingId = randomUUID();
        final UUID applicationId = randomUUID();
        final UUID applicationId2 = randomUUID();
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
        final ApplicationOrganisationDetailsUpdatedForHearing applicationOrganisationDetailsUpdatedForHearing =
                new ApplicationOrganisationDetailsUpdatedForHearing(applicationId, subjectId, associatedDefenceOrganisation, hearingId);

        final CourtApplication courtApplication = courtApplication()
                .withId(applicationId)
                .withSubject(CourtApplicationParty.courtApplicationParty()
                        .withId(subjectId)
                        .build())
                .build();

        final CourtApplication courtApplication2 = courtApplication()
                .withId(applicationId2)
                .withSubject(CourtApplicationParty.courtApplicationParty()
                        .withId(subjectId)
                        .build())
                .build();

        final Hearing hearing = new Hearing();
        hearing.setId(hearingId);
        hearing.setCourtApplicationsJson("{\"courtApplications\" : [" + objectToJsonObjectConverter.convert(courtApplication).toString() + ", "+objectToJsonObjectConverter.convert(courtApplication2).toString()+"]}");
        final JsonEnvelope envelope = createJsonEnvelope(applicationOrganisationDetailsUpdatedForHearing);
        when(hearingRepository.findBy(hearingId)).thenReturn(hearing);
        applicationOrganisationDetailsUpdatedEventListener.applicationOrganisationDetailsUpdated(envelope);
        final ArgumentCaptor<Hearing> hearingArgumentCaptor = ArgumentCaptor.forClass(Hearing.class);
        verify(hearingRepository).save(hearingArgumentCaptor.capture());
        final Hearing hearingOut = hearingArgumentCaptor.getValue();
        assertThat(hearingOut.getId(), is(hearingId));
        final JsonArray applicationsJsonArray = JsonObjects.createReader(new StringReader(hearingOut.getCourtApplicationsJson())).readObject().getJsonArray("courtApplications");
        assertThat(applicationsJsonArray.size(), is(2));
        assertThat(applicationsJsonArray.get(0).asJsonObject().getString("id"), is(applicationId.toString()));
        assertThat(applicationsJsonArray.get(0).asJsonObject().getJsonObject("subject").getJsonObject("associatedDefenceOrganisation").getString("applicationReference"), is(associatedDefenceOrganisation.getApplicationReference()));
        assertThat(applicationsJsonArray.get(1).asJsonObject().getString("id"), is(applicationId2.toString()));
        assertThat(applicationsJsonArray.get(1).asJsonObject().getJsonObject("subject").getJsonObject("associatedDefenceOrganisation"), nullValue());
    }
    private JsonEnvelope createJsonEnvelope(final ApplicationOrganisationDetailsUpdatedForHearing applicationOrganisationDetailsUpdatedForHearing) {
        final JsonObject jsonObject = objectToJsonObjectConverter.convert(applicationOrganisationDetailsUpdatedForHearing);
        return envelopeFrom((Metadata) null, jsonObject);
    }
}
