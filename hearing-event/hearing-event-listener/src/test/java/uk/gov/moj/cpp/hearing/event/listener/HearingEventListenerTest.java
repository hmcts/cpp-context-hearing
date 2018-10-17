package uk.gov.moj.cpp.hearing.event.listener;

import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.common.reflection.ReflectionUtils.setField;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.asSet;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.test.matchers.ElementAtListMatcher.first;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.moj.cpp.hearing.domain.event.result.DraftResultSaved;
import uk.gov.moj.cpp.hearing.mapping.TargetJPAMapper;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Target;
import uk.gov.moj.cpp.hearing.repository.HearingRepository;
import uk.gov.moj.cpp.hearing.test.CoreTestTemplates;

import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class HearingEventListenerTest {

    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Spy
    @InjectMocks
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @Spy
    private final ObjectMapper objectMapper = new ObjectMapperProducer().objectMapper();

    @InjectMocks
    private HearingEventListener hearingEventListener;

    @Mock
    private HearingRepository hearingRepository;

    @Mock
    private TargetJPAMapper targetJPAMapper;

    @Captor
    ArgumentCaptor<Hearing> saveHearingCaptor;

    @Before
    public void setUp() {
        setField(this.jsonObjectToObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
    }

    @Test
    public void draftResultSaved_shouldPersist() {

        final UUID hearingId = randomUUID();
        final Target targetOut = new Target();
        final DraftResultSaved draftResultSaved = new DraftResultSaved(CoreTestTemplates.target(hearingId, randomUUID(), randomUUID(), randomUUID()).build());
        final Hearing dbHearing = new Hearing()
                .setId(hearingId)
                .setTargets(asSet(new Target()
                        .setId(draftResultSaved.getTarget().getTargetId())
                ));

        when(hearingRepository.findBy(hearingId)).thenReturn(dbHearing);
        when(targetJPAMapper.toJPA(dbHearing, draftResultSaved.getTarget())).thenReturn(targetOut);

        hearingEventListener.draftResultSaved(envelopeFrom(metadataWithRandomUUID("hearing.draft-result-saved"),
                objectToJsonObjectConverter.convert(draftResultSaved)
        ));

        verify(this.hearingRepository).save(saveHearingCaptor.capture());

        assertThat(saveHearingCaptor.getValue(), isBean(Hearing.class)
                .with(Hearing::getId, is(hearingId))
                .with(Hearing::getTargets, hasSize(1))
                .with(Hearing::getTargets, first(is(targetOut)))
        );
    }


}