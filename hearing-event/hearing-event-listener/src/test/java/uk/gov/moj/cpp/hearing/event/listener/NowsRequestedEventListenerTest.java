package uk.gov.moj.cpp.hearing.event.listener;

import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.common.reflection.ReflectionUtils.setField;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.NowsRequestedTemplates.nowsRequestedTemplate;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.asList;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.asSet;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.test.matchers.CollectionSearchMatcher.findElement;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.Material;
import uk.gov.moj.cpp.hearing.nows.events.NowsRequested;
import uk.gov.moj.cpp.hearing.persist.NowsRepository;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Nows;
import uk.gov.moj.cpp.hearing.persist.entity.ha.NowsMaterial;
import uk.gov.moj.cpp.hearing.persist.entity.ha.NowsResult;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.moj.cpp.hearing.test.TestTemplates;


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
    public void shouldUpdateNowsInformation() {

        final NowsRequested nowsRequested = nowsRequestedTemplate();

        Nows nows = Nows.builder()
                .withHearingId(nowsRequested.getHearing().getId())
                .withDefendantId(nowsRequested.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getId())
                .withId(nowsRequested.getNows().get(0).getId())
                .withNowsTypeId(nowsRequested.getNowTypes().get(0).getId())
                .build();

        NowsMaterial nowsMaterial = NowsMaterial.builder()
                .withUserGroups(asSet("LO", "CC"))
                .withId(nowsRequested.getNows().get(0).getMaterials().get(0).getId())
                .withLanguage(nowsRequested.getNows().get(0).getMaterials().get(0).getLanguage())
                .withNows(nows)
                .build();

        NowsResult nowResult = NowsResult.builder()
                .withSharedResultId(randomUUID())
                .withSequence(1)
                .withNowsMaterial(nowsMaterial)
                .build();

        nows.getMaterial().add(nowsMaterial);
        nowsMaterial.getNowResult().add(nowResult);

        when(nowsRepository.findByHearingId(nowsRequested.getHearing().getId())).thenReturn(asList(nows));

        nowsRequestedEventListener.nowsRequested(envelopeFrom(metadataWithRandomUUID("hearing.events.nows-requested"), objectToJsonObjectConverter.convert(nowsRequested)));

        ArgumentCaptor<Nows> nowsMaterialArgumentCaptor = ArgumentCaptor.forClass(Nows.class);
        verify(this.nowsRepository).save(nowsMaterialArgumentCaptor.capture());
        Nows results = nowsMaterialArgumentCaptor.getValue();

        assertThat(results, isBean(Nows.class)
                .with(Nows::getId, is(nowsRequested.getNows().get(0).getId()))
                .with(Nows::getDefendantId, is(nowsRequested.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getId()))
                .with(Nows::getHearingId, is(nowsRequested.getHearing().getId()))
                .with(Nows::getNowsTypeId, is(nowsRequested.getNowTypes().get(0).getId()))
                .with(Nows::getMaterial, findElement(isBean(NowsMaterial.class),
                        isBean(NowsMaterial.class)
                                .with(NowsMaterial::getId, is(nowsMaterial.getId()))
                                .with(NowsMaterial::getStatus, is("requested"))
                                .with(NowsMaterial::getLanguage, is(nowsRequested.getNows().get(0).getMaterials().get(0).getLanguage()))
                                .with(NowsMaterial::getNowResult, findElement(
                                        /*predicate*/isBean(NowsResult.class).with(NowsResult::getSequence, is(1)),
                                        /*matcher*/isBean(NowsResult.class)
                                                .with(NowsResult::getSharedResultId, is(nowsRequested.getNows().get(0).getMaterials().get(0).getNowResult().get(0).getSharedResultId()))

                                ))

                ))
        );

    }
}