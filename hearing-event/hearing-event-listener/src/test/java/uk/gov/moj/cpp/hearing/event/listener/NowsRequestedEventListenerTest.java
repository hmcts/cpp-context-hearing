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
import uk.gov.moj.cpp.hearing.nows.events.NowsRequested;
import uk.gov.moj.cpp.hearing.persist.NowsMaterialRepository;
import uk.gov.moj.cpp.hearing.persist.entity.ex.NowsMaterial;
import uk.gov.moj.cpp.hearing.persist.entity.ex.NowsMaterialStatus;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataWithRandomUUID;
import static uk.gov.justice.services.test.utils.common.reflection.ReflectionUtils.setField;
import static uk.gov.justice.services.test.utils.core.messaging.JsonEnvelopeBuilder.envelopeFrom;


@RunWith(MockitoJUnitRunner.class)
public class NowsRequestedEventListenerTest {

    @Mock
    private NowsMaterialRepository nowsMaterialRepository;

    @InjectMocks
    private NowsRequestedEventListener nowsRequestedEventListener;

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
    public void shouldUpdateNowsInformation() throws Exception {

        UUID hearingId = randomUUID();
        UUID defendantId = randomUUID();
        UUID materialId = randomUUID();

        final InputStream is = NowsRequestedEventListenerTest.class.getResourceAsStream("/hearing.events.nows-generated.json");
        NowsRequested nowsRequested =  new ObjectMapperProducer().objectMapper().readValue(is, NowsRequested.class);
        nowsRequested.getHearing().setId(hearingId.toString());
        nowsRequested.getHearing().getNows().get(0).setDefendantId(defendantId.toString());
        nowsRequested.getHearing().getNows().get(0).getMaterial().get(0).setId(materialId.toString());

        final List<NowsMaterial> hearingList = new ArrayList<>();
        NowsMaterial nowsMaterial = NowsMaterial.builder().withHearingId(hearingId).withDefendantId(defendantId)
                .withId(materialId).withStatus(NowsMaterialStatus.GENERATED).withUserGroups(Arrays.asList("LO", "CC")).build();
        hearingList.add(nowsMaterial);
        when(nowsMaterialRepository.findByHearingId(hearingId)).thenReturn(hearingList);


        when(this.nowsMaterialRepository.findByHearingId((hearingId))).thenReturn(hearingList);

        nowsRequestedEventListener.nowsRequested(envelopeFrom(metadataWithRandomUUID("hearing.offence-verdict-updated"),
                objectToJsonObjectConverter.convert(nowsRequested)));

        ArgumentCaptor<NowsMaterial> nowsMaterialArgumentCaptor= ArgumentCaptor.forClass(NowsMaterial.class);
        verify(this.nowsMaterialRepository).save(nowsMaterialArgumentCaptor.capture());
        assertThat(nowsMaterialArgumentCaptor.getValue().getId(), is(materialId));
        assertThat(nowsMaterialArgumentCaptor.getValue().getHearingId(), is(hearingId));
        assertThat(nowsMaterialArgumentCaptor.getValue().getDefendantId(), is(defendantId));


    }
}