package uk.gov.moj.cpp.hearing.event.listener;

import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.common.reflection.ReflectionUtils.setField;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;

import org.junit.Before;
import org.junit.Ignore;
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
import uk.gov.moj.cpp.hearing.domain.event.DefenceCounselUpsert;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingDay;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.repository.HearingRepository;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.UUID;

@RunWith(MockitoJUnitRunner.class)
public class DefenceCounselAddedEventListenerTest {

    @Mock
    private HearingRepository hearingRepository;

    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @InjectMocks
    private DefenceCounselAddedEventListener defenceCounselAddedEventListener;

    @Captor
    private ArgumentCaptor<Hearing> ahearingArgumentCaptor;

    @Before
    public void setUp() {
        setField(this.jsonObjectToObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
    }

    @Ignore("GPE-5825 - there is another story that addresses many issues around adding counselors - so we are suspending this until we play that.")
    @Test
    public void shouldStoreDefenceCounselOnAddEvent() {

        DefenceCounselUpsert defenceCounselUpsert = DefenceCounselUpsert.builder()
                .withAttendeeId(randomUUID())
                .withHearingId(randomUUID())
                .withFirstName("David")
                .withLastName("Davidson")
                .withTitle("Colonel")
                .withStatus("QC")
                .withDefendantIds(asList(randomUUID(), randomUUID()))
                .build();

        final Hearing hearing = new Hearing();
        hearing.setId(defenceCounselUpsert.getHearingId());
                /*.withLegalCases(
                        Arrays.asList(
                                ProsecutionCase.builder()
                                        .withDefendants(asList(
                                                Defendant.builder().withId(defenceCounselUpsert.getDefendantIds().get(0)).build(),
                                                Defendant.builder().withId(defenceCounselUpsert.getDefendantIds().get(1)).build())
                                        )

                                        .build()
                        )
                )*/


//                .build();
        final HearingDay hearingDay = new HearingDay();
        hearingDay.setId(new HearingSnapshotKey(UUID.randomUUID(), hearing.getId()));
        hearingDay.setSittingDay(ZonedDateTime.from(LocalDate.now()));
        hearingDay.setHearing(hearing);

        hearing.getHearingDays().add(hearingDay);

        when(this.hearingRepository.findBy(defenceCounselUpsert.getHearingId())).thenReturn(hearing);

        this.defenceCounselAddedEventListener.defenseCounselAdded(envelopeFrom(metadataWithRandomUUID("hearing.newdefence-counsel-added"),
                objectToJsonObjectConverter.convert(defenceCounselUpsert)));

        verify(this.hearingRepository).saveAndFlush(ahearingArgumentCaptor.capture());

        final Hearing savedHearing = ahearingArgumentCaptor.getValue();
        assertThat(savedHearing, is(hearing));
        //assertThat(savedHearing.getAttendees().size(), is(1));
//        assertThat(savedHearing.getAttendees().get(0), instanceOf(DefenceAdvocate.class));

//        final DefenceAdvocate defenceAdvocate = (DefenceAdvocate) hearing.getAttendees().get(0);

//        assertThat(defenceAdvocate.getId().getId(), is(defenceCounselUpsert.getAttendeeId()));
//        assertThat(defenceAdvocate.getId().getHearingId(), is(defenceCounselUpsert.getHearingId()));
//        assertThat(defenceAdvocate.getFirstName(), is(defenceCounselUpsert.getFirstName()));
//        assertThat(defenceAdvocate.getLastName(), is(defenceCounselUpsert.getLastName()));
//        assertThat(defenceAdvocate.getTitle(), is(defenceCounselUpsert.getTitle()));
//        assertThat(defenceAdvocate.getStatus(), is(defenceCounselUpsert.getStatus()));

//TODO what about defence advocates !!!
//        assertThat(hearing.getProsecutionCases().get(0).getDefendants().get(0).getDefenceAdvocates(), hasItems(defenceAdvocate));
//        assertThat(hearing.getProsecutionCases().get(0).getDefendants().get(1).getDefenceAdvocates(), hasItems(defenceAdvocate));

//        verify(this.attendeeHearingDateRespository).saveAndFlush(attendeeHearingDateArgumentCaptor.capture());
    }

}
