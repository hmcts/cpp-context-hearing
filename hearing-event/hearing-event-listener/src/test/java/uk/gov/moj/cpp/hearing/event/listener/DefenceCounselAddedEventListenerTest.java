package uk.gov.moj.cpp.hearing.event.listener;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.moj.cpp.hearing.domain.event.NewDefenceCounselAdded;
import uk.gov.moj.cpp.hearing.persist.entity.ex.Ahearing;
import uk.gov.moj.cpp.hearing.persist.entity.ex.DefenceAdvocate;
import uk.gov.moj.cpp.hearing.persist.entity.ex.Defendant;
import uk.gov.moj.cpp.hearing.persist.entity.ex.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.repository.AhearingRepository;

import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataWithRandomUUID;
import static uk.gov.justice.services.test.utils.common.reflection.ReflectionUtils.setField;
import static uk.gov.justice.services.test.utils.core.messaging.JsonEnvelopeBuilder.envelopeFrom;

@RunWith(MockitoJUnitRunner.class)
public class DefenceCounselAddedEventListenerTest {

    @Mock
    private AhearingRepository ahearingRepository;

    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @InjectMocks
    private DefenceCounselAddedEventListener defenceCounselAddedEventListener;

    @Captor
    private ArgumentCaptor<Ahearing> ahearingArgumentCaptor;

    @Before
    public void setUp() {
        setField(this.jsonObjectToObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
    }

    @Test
    public void shouldStoreDefenceCounselOnAddEvent() {

        NewDefenceCounselAdded newDefenceCounselAdded = NewDefenceCounselAdded.builder()
                .withAttendeeId(randomUUID())
                .withHearingId(randomUUID())
                .withFirstName("David")
                .withLastName("Davidson")
                .withTitle("Colonel")
                .withStatus("QC")
                .withDefendantIds(asList(randomUUID(), randomUUID()))
                .build();

        Ahearing ahearing = Ahearing.builder()
                .withId(newDefenceCounselAdded.getHearingId())
                .withDefendants(asList(
                        Defendant.builder().withId(new HearingSnapshotKey(newDefenceCounselAdded.getDefendantIds().get(0), newDefenceCounselAdded.getHearingId())).build(),
                        Defendant.builder().withId(new HearingSnapshotKey(newDefenceCounselAdded.getDefendantIds().get(1), newDefenceCounselAdded.getHearingId())).build()))
                .build();
        when(this.ahearingRepository.findBy(newDefenceCounselAdded.getHearingId())).thenReturn(ahearing);

        this.defenceCounselAddedEventListener.defenseCounselAdded(envelopeFrom(metadataWithRandomUUID("hearing.newdefence-counsel-added"),
                objectToJsonObjectConverter.convert(newDefenceCounselAdded)));

        verify(this.ahearingRepository).save(ahearingArgumentCaptor.capture());
        Ahearing savedHearing = ahearingArgumentCaptor.getValue();
        assertThat(savedHearing, is(ahearing));
        assertThat(savedHearing.getAttendees().size(), is(1));
        assertThat(savedHearing.getAttendees().get(0), instanceOf(DefenceAdvocate.class));

        DefenceAdvocate defenceAdvocate = (DefenceAdvocate) ahearing.getAttendees().get(0);

        assertThat(defenceAdvocate.getId().getId(), is(newDefenceCounselAdded.getAttendeeId()));
        assertThat(defenceAdvocate.getId().getHearingId(), is(newDefenceCounselAdded.getHearingId()));
        assertThat(defenceAdvocate.getFirstName(), is(newDefenceCounselAdded.getFirstName()));
        assertThat(defenceAdvocate.getLastName(), is(newDefenceCounselAdded.getLastName()));
        assertThat(defenceAdvocate.getTitle(), is(newDefenceCounselAdded.getTitle()));
        assertThat(defenceAdvocate.getStatus(), is(newDefenceCounselAdded.getStatus()));

        assertThat(ahearing.getDefendants().get(0).getDefenceAdvocates(), hasItems(defenceAdvocate));
        assertThat(ahearing.getDefendants().get(1).getDefenceAdvocates(), hasItems(defenceAdvocate));
    }

}
