package uk.gov.moj.cpp.hearing.event.listener;

import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.core.courts.CourtApplicationParty;
import uk.gov.justice.core.courts.DefendantCase;
import uk.gov.justice.core.courts.MasterDefendant;
import uk.gov.justice.core.courts.PersonDefendant;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.moj.cpp.hearing.domain.event.ApplicationDefendantsUpdatedForHearing;
import uk.gov.moj.cpp.hearing.domain.event.CaseDefendantsUpdatedForHearing;
import uk.gov.moj.cpp.hearing.mapping.CourtApplicationsSerializer;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Defendant;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCase;
import uk.gov.moj.cpp.hearing.repository.HearingRepository;

import javax.json.JsonObject;
import java.util.Arrays;
import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.asSet;

@RunWith(MockitoJUnitRunner.class)
public class CaseDefendantsUpdateListenerTest {

    @Mock
    HearingRepository hearingRepository;
    @InjectMocks
    private CaseDefendantsUpdateListener caseDefendantsUpdateListener;

    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @Spy
    private CourtApplicationsSerializer courtApplicationsSerializer;

    @Before
    public void setup() {
        setField(this.jsonObjectToObjectConverter, "objectMapper", new ObjectMapperProducer().objectMapper());
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
        setField(this.courtApplicationsSerializer, "jsonObjectToObjectConverter", jsonObjectToObjectConverter);
        setField(this.courtApplicationsSerializer, "objectToJsonObjectConverter", objectToJsonObjectConverter);
    }

    @Test
    public void testCaseDefendantsUpdated() {
        final UUID hearingId = randomUUID();
        final UUID defendantId = randomUUID();
        final UUID caseId = randomUUID();

        final CaseDefendantsUpdatedForHearing caseDefendantsUpdatedForHearing =
                CaseDefendantsUpdatedForHearing.caseDefendantsUpdatedForHearing()
                .withHearingId(hearingId)
                .withProsecutionCase(uk.gov.justice.core.courts.ProsecutionCase.prosecutionCase()
                        .withId(caseId)
                        .withCaseStatus("CLOSED")
                        .withDefendants(Arrays.asList(uk.gov.justice.core.courts.Defendant.defendant()
                                .withId(defendantId)
                                .withProceedingsConcluded(true)
                                .build()))
                        .build()).build();
        final Hearing hearing = new Hearing();
        hearing.setId(hearingId);

        final JsonEnvelope envelope = createJsonEnvelope(caseDefendantsUpdatedForHearing);

        final Defendant defendant = new Defendant();

        defendant.setId(new HearingSnapshotKey(defendantId, hearingId));

        final ProsecutionCase prosecutionCase = new ProsecutionCase();

        prosecutionCase.setId(new HearingSnapshotKey(caseId, hearingId));

        prosecutionCase.setDefendants(asSet(defendant));

        hearing.setProsecutionCases(asSet(prosecutionCase));

        when(hearingRepository.findBy(hearingId)).thenReturn(hearing);

        caseDefendantsUpdateListener.caseDefendantsUpdatedForHearing(envelope);

        final ArgumentCaptor<Hearing> hearingArgumentCaptor = ArgumentCaptor.forClass(Hearing.class);

        verify(hearingRepository).save(hearingArgumentCaptor.capture());

        final Hearing hearingOut = hearingArgumentCaptor.getValue();

        final ProsecutionCase prosecutionCaseOut = hearingOut.getProsecutionCases().stream().findFirst().get();

        final Defendant defendantOut = prosecutionCaseOut.getDefendants().stream().findFirst().get();

        assertThat("CLOSED", is(prosecutionCaseOut.getCaseStatus()));
        assertThat(true, is(defendantOut.isProceedingsConcluded()));
    }

    @Test
    public void testCaseDefendantsUpdatedWithDriverNumber() {
        final UUID hearingId = randomUUID();
        final UUID defendantId = randomUUID();
        final UUID caseId = randomUUID();

        final CaseDefendantsUpdatedForHearing caseDefendantsUpdatedForHearing =
                CaseDefendantsUpdatedForHearing.caseDefendantsUpdatedForHearing()
                        .withHearingId(hearingId)
                        .withProsecutionCase(uk.gov.justice.core.courts.ProsecutionCase.prosecutionCase()
                                .withId(caseId)
                                .withCaseStatus("CLOSED")
                                .withDefendants(Arrays.asList(uk.gov.justice.core.courts.Defendant.defendant()
                                        .withId(defendantId)
                                        .withProceedingsConcluded(true)
                                        .withPersonDefendant(PersonDefendant.personDefendant()
                                                .withDriverNumber("DVLA12345")
                                                .build())
                                        .build()))
                                .build()).build();
        final Hearing hearing = new Hearing();
        hearing.setId(hearingId);

        final JsonEnvelope envelope = createJsonEnvelope(caseDefendantsUpdatedForHearing);

        final Defendant defendant = new Defendant();

        defendant.setId(new HearingSnapshotKey(defendantId, hearingId));
        defendant.setPersonDefendant(new uk.gov.moj.cpp.hearing.persist.entity.ha.PersonDefendant());

        final ProsecutionCase prosecutionCase = new ProsecutionCase();

        prosecutionCase.setId(new HearingSnapshotKey(caseId, hearingId));

        prosecutionCase.setDefendants(asSet(defendant));

        hearing.setProsecutionCases(asSet(prosecutionCase));

        when(hearingRepository.findBy(hearingId)).thenReturn(hearing);

        caseDefendantsUpdateListener.caseDefendantsUpdatedForHearing(envelope);

        final ArgumentCaptor<Hearing> hearingArgumentCaptor = ArgumentCaptor.forClass(Hearing.class);

        verify(hearingRepository).save(hearingArgumentCaptor.capture());

        final Hearing hearingOut = hearingArgumentCaptor.getValue();

        final ProsecutionCase prosecutionCaseOut = hearingOut.getProsecutionCases().stream().findFirst().get();

        final Defendant defendantOut = prosecutionCaseOut.getDefendants().stream().findFirst().get();

        assertThat("CLOSED", is(prosecutionCaseOut.getCaseStatus()));
        assertThat(true, is(defendantOut.isProceedingsConcluded()));
        assertThat(defendantOut.getPersonDefendant().getDriverNumber(), is("DVLA12345"));
    }


    @Test
    public void testApplicationDefendantsUpdatedWithDriverNumber() {
        final UUID hearingId = randomUUID();
        final UUID defendantId = randomUUID();
        final UUID applicationId = randomUUID();

        final ApplicationDefendantsUpdatedForHearing applicationDefendantsUpdatedForHearing =
                ApplicationDefendantsUpdatedForHearing.applicationDefendantsUpdatedForHearing()
                        .withHearingId(hearingId)
                        .withCourtApplication(CourtApplication.courtApplication()
                                .withId(applicationId)
                                .withApplicant(CourtApplicationParty.courtApplicationParty()
                                        .withMasterDefendant(MasterDefendant.masterDefendant()
                                                .withDefendantCase(Collections.singletonList(DefendantCase.defendantCase().build()))
                                                .withMasterDefendantId(defendantId)
                                                .withPersonDefendant(PersonDefendant.personDefendant()
                                                        .withDriverNumber("DRV12345")
                                                        .build())
                                                .build())
                                        .build())
                                .withSubject(CourtApplicationParty.courtApplicationParty()
                                        .withMasterDefendant(MasterDefendant.masterDefendant()
                                                .withDefendantCase(Collections.singletonList(DefendantCase.defendantCase().build()))
                                                .withMasterDefendantId(defendantId)
                                                .withPersonDefendant(PersonDefendant.personDefendant()
                                                        .withDriverNumber("DRV12345")
                                                        .build())
                                                .build())
                                        .build())
                                .build())
                        .build();
        final JsonEnvelope envelope = createJsonEnvelope(applicationDefendantsUpdatedForHearing);

        final Hearing hearing = new Hearing();
        hearing.setId(hearingId);

        final CourtApplication courtApplication = CourtApplication.courtApplication()
                .withId(applicationId)
                .withApplicant(CourtApplicationParty.courtApplicationParty()
                        .withMasterDefendant(MasterDefendant.masterDefendant()
                                .withDefendantCase(Collections.singletonList(DefendantCase.defendantCase().build()))
                                .withMasterDefendantId(defendantId)
                                .withPersonDefendant(PersonDefendant.personDefendant().build())
                                .build())
                        .build())
                .withSubject(CourtApplicationParty.courtApplicationParty()
                        .withMasterDefendant(MasterDefendant.masterDefendant()
                                .withDefendantCase(Collections.singletonList(DefendantCase.defendantCase().build()))
                                .withMasterDefendantId(defendantId)
                                .withPersonDefendant(PersonDefendant.personDefendant().build())
                                .build())
                        .build())
                .build();
        hearing.setCourtApplicationsJson("{\"courtApplications\" : [" + objectToJsonObjectConverter.convert(courtApplication).toString() + "]}");


        when(hearingRepository.findBy(hearingId)).thenReturn(hearing);

        caseDefendantsUpdateListener.applicationDefendantsUpdatedForHearing(envelope);

        final ArgumentCaptor<Hearing> hearingArgumentCaptor = ArgumentCaptor.forClass(Hearing.class);

        verify(hearingRepository).save(hearingArgumentCaptor.capture());

        final Hearing hearingOut = hearingArgumentCaptor.getValue();

        final CourtApplication courtApplicationOut = courtApplicationsSerializer.courtApplications(hearingOut.getCourtApplicationsJson()).get(0);

        assertThat(courtApplicationOut.getApplicant().getMasterDefendant().getPersonDefendant().getDriverNumber(), is("DRV12345"));
        assertThat(courtApplicationOut.getSubject().getMasterDefendant().getPersonDefendant().getDriverNumber(), is("DRV12345"));
    }

    private JsonEnvelope createJsonEnvelope(final CaseDefendantsUpdatedForHearing caseDefendantsUpdatedForHearing) {

        final JsonObject jsonObject = objectToJsonObjectConverter.convert(caseDefendantsUpdatedForHearing);

        return envelopeFrom((Metadata) null, jsonObject);
    }

    private JsonEnvelope createJsonEnvelope(final ApplicationDefendantsUpdatedForHearing applicationDefendantsUpdatedForHearing) {

        final JsonObject jsonObject = objectToJsonObjectConverter.convert(applicationDefendantsUpdatedForHearing);

        return envelopeFrom((Metadata) null, jsonObject);
    }
}
