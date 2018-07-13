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
import uk.gov.moj.cpp.hearing.persist.NowsRepository;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Nows;
import uk.gov.moj.cpp.hearing.persist.entity.ha.NowsResult;
import uk.gov.moj.cpp.hearing.persist.entity.ha.NowsMaterial;

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
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.common.reflection.ReflectionUtils.setField;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;


@RunWith(MockitoJUnitRunner.class)
public class NowsRequestedEventListenerTest {

    @Mock
    private NowsRepository nowsRepository;

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
        UUID nowsId = randomUUID();
        UUID nowsTypeId = randomUUID();
        UUID sharedResultId = randomUUID();
        final String language = "english";

        final InputStream is = NowsRequestedEventListenerTest.class.getResourceAsStream("/hearing.events.nows-generated.json");
        NowsRequested nowsRequested = new ObjectMapperProducer().objectMapper().readValue(is, NowsRequested.class);
        nowsRequested.getHearing().setId(hearingId.toString());
        nowsRequested.getHearing().getNows().get(0).setDefendantId(defendantId.toString());
        nowsRequested.getHearing().getNows().get(0).setId(nowsId.toString());
        nowsRequested.getHearing().getNows().get(0).setNowsTypeId(nowsTypeId.toString());
        nowsRequested.getHearing().getNows().get(0).setNowsTemplateName(nowsTypeId.toString());
        nowsRequested.getHearing().getNows().get(0).getMaterials().get(0).getNowResult().get(0).setSharedResultId(sharedResultId.toString());
        nowsRequested.getHearing().getNows().get(0).getMaterials().get(0).getNowResult().get(0).setSequence(1);
        nowsRequested.getHearing().getNows().get(0).getMaterials().get(0).setId(materialId.toString());
        nowsRequested.getHearing().getNows().get(0).getMaterials().get(0).setLanguage(language);


        final List<Nows> nowsList = new ArrayList<>();
        Nows nows = Nows.builder().withHearingId(hearingId).withDefendantId(defendantId).withId(nowsId).withNowsTypeId(nowsTypeId).build();

        NowsMaterial nowsMaterial = NowsMaterial.builder().withUserGroups(Arrays.asList("LO", "CC"))
                .withId(materialId).withLanguage(language).withNows(nows).build();
        NowsResult nowResult = NowsResult.builder().withSharedResultId(sharedResultId).withSequence(1).withNowsMaterial(nowsMaterial).build();
        nows.getMaterial().add(nowsMaterial);
        nowsMaterial.getNowResult().add(nowResult);
        nowsList.add(nows);

        when(nowsRepository.findByHearingId(hearingId)).thenReturn(nowsList);

        nowsRequestedEventListener.nowsRequested(envelopeFrom(metadataWithRandomUUID("hearing.events.nows-requested"),
                objectToJsonObjectConverter.convert(nowsRequested)));

        ArgumentCaptor<Nows> nowsMaterialArgumentCaptor = ArgumentCaptor.forClass(Nows.class);
        verify(this.nowsRepository).save(nowsMaterialArgumentCaptor.capture());
        assertThat(nowsMaterialArgumentCaptor.getValue().getId(), is(nowsId));
        assertThat(nowsMaterialArgumentCaptor.getValue().getHearingId(), is(hearingId));
        assertThat(nowsMaterialArgumentCaptor.getValue().getDefendantId(), is(defendantId));
        assertThat(nowsMaterialArgumentCaptor.getValue().getNowsTypeId(), is(nowsTypeId));
        assertThat(nowsMaterialArgumentCaptor.getValue().getMaterial().get(0).getId(), is(nowsMaterial.getId()));
        assertThat(nowsMaterialArgumentCaptor.getValue().getMaterial().get(0).getStatus(), is("requested"));
        assertThat(nowsMaterialArgumentCaptor.getValue().getMaterial().get(0).getLanguage(), is(language));
        assertThat(nowsMaterialArgumentCaptor.getValue().getMaterial().get(0).getNowResult().get(0).getSharedResultId(), is(sharedResultId));
        assertThat(nowsMaterialArgumentCaptor.getValue().getMaterial().get(0).getNowResult().get(0).getSequence(), is(1));

    }
}