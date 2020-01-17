package uk.gov.moj.cpp.hearing.event.listener;

import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.justice.core.courts.Marker;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.moj.cpp.hearing.domain.event.CaseMarkersUpdated;
import uk.gov.moj.cpp.hearing.mapping.CaseMarkerJPAMapper;
import uk.gov.moj.cpp.hearing.persist.entity.ha.CaseMarker;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCase;
import uk.gov.moj.cpp.hearing.repository.ProsecutionCaseRepository;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@RunWith(MockitoJUnitRunner.class)
public class CaseMarkerEventListenerTest {

    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @InjectMocks
    private CaseMarkerEventListener caseMarkerEventListener;

    @Captor
    private ArgumentCaptor<ProsecutionCase> argumentCaptor;

    @Mock
    CaseMarkerJPAMapper caseMarkerJPAMapper;

    @Mock
    ProsecutionCaseRepository prosecutionCaseRepository;

    @Before
    public void setUp() {
        setField(this.jsonObjectToObjectConverter, "objectMapper", new ObjectMapperProducer().objectMapper());
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
    }

    @Test
    public void caseMarkersUpdated() {

        final UUID hearingId = randomUUID();
        final UUID prosecutionCaseId = randomUUID();
        final List<Marker> markers = Arrays.asList(Marker.marker()
                        .withId(randomUUID())
                        .withMarkerTypeid(randomUUID())
                        .withMarkerTypeCode(STRING.next())
                        .withMarkerTypeDescription(STRING.next())
                        .build());

        CaseMarkersUpdated caseMarkersUpdated = new CaseMarkersUpdated(randomUUID(), randomUUID(), markers);

        final Hearing hearing = new Hearing();
        hearing.setId(hearingId);

        final ProsecutionCase prosecutionCase = new ProsecutionCase();
        prosecutionCase.setId(new HearingSnapshotKey(prosecutionCaseId, hearingId));
        prosecutionCase.setHearing(hearing);

        final CaseMarker caseMarker = new CaseMarker();
        caseMarker.setId(new HearingSnapshotKey(randomUUID(), hearingId));
        Set<CaseMarker> caseMarkers = new HashSet<>();
        caseMarkers.add(caseMarker);

        when(this.prosecutionCaseRepository.findBy(any())).thenReturn(prosecutionCase);
        when(this.caseMarkerJPAMapper.toJPA(hearing, prosecutionCase, markers)).thenReturn(caseMarkers);
        this.caseMarkerEventListener.caseMarkersUpdated(envelopeFrom(metadataWithRandomUUID("hearing.events.case-markers-updated"),
                objectToJsonObjectConverter.convert(caseMarkersUpdated)));

        verify(this.prosecutionCaseRepository).save(argumentCaptor.capture());

        final ProsecutionCase savedProsecutionCase = argumentCaptor.getValue();
        assertThat(savedProsecutionCase, Matchers.is(prosecutionCase));
    }
}
