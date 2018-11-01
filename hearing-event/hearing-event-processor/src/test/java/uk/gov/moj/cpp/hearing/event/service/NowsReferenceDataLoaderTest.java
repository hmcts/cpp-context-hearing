package uk.gov.moj.cpp.hearing.event.service;

import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.common.reflection.ReflectionUtils.setField;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloperWithEvents;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_LOCAL_DATE;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.event.HearingInitiated;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.AllNows;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.NowDefinition;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.ResultDefinitions;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.AllResultDefinitions;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinition;
import uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.UUID;

import javax.json.JsonValue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class NowsReferenceDataLoaderTest {

    @Spy
    private final Enveloper enveloper = createEnveloperWithEvents(
            HearingInitiated.class
    );

    @Mock
    private Requester requester;

    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @InjectMocks
    NowsReferenceDataLoader target;

    @Before
    public void setup() {
        setField(this.jsonObjectToObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
    }


    @Test
    public void testLoadAllResultDefinitionData() {

        LocalDate referenceDate = PAST_LOCAL_DATE.next();

        AllResultDefinitions allResultDefinitionsIn = AllResultDefinitions.allResultDefinitions().setResultDefinitions(
                Arrays.asList(ResultDefinition.resultDefinition().setId(UUID.randomUUID()).setWelshLabel("Idris").setIsAvailableForCourtExtract(true))
        );

        final JsonEnvelope resultEnvelope = envelopeFrom(metadataWithRandomUUID("something"), objectToJsonObjectConverter.convert(allResultDefinitionsIn));


        when(requester.request(any())).thenReturn(resultEnvelope);

        AllResultDefinitions actual = target.loadAllResultDefinitions(envelopeFrom(metadataWithRandomUUID("something"), JsonValue.NULL), referenceDate);

        assertThat(actual.getResultDefinitions().get(0), BeanMatcher.isBean(ResultDefinition.class)
                .with(ResultDefinition::getId, is(actual.getResultDefinitions().get(0).getId()))
                .with(ResultDefinition::getWelshLabel, is(actual.getResultDefinitions().get(0).getWelshLabel()))
                .with(ResultDefinition::getIsAvailableForCourtExtract, is(actual.getResultDefinitions().get(0).getIsAvailableForCourtExtract()))

        );
    }


    @Test
    public void testLoadAllNowsData() {

        LocalDate referenceDate = PAST_LOCAL_DATE.next();

        AllNows data = AllNows.allNows()
                .setNows(singletonList(NowDefinition.now()
                        .setId(randomUUID())
                        .setResultDefinitions(singletonList(ResultDefinitions.resultDefinitions()
                                .setId(randomUUID())
                                .setMandatory(true)
                                .setPrimary(true)
                        ))
                        .setName(STRING.next())
                        .setTemplateName(STRING.next())
                ));


        final JsonEnvelope resultEnvelope = envelopeFrom(metadataWithRandomUUID("something"), objectToJsonObjectConverter.convert(data));

        when(requester.request(any())).thenReturn(resultEnvelope);

        AllNows actual = target.loadAllNowsReference(envelopeFrom(metadataWithRandomUUID("something"), JsonValue.NULL), referenceDate);

        assertThat(actual.getNows().get(0), BeanMatcher.isBean(NowDefinition.class)
                .with(NowDefinition::getId, is(data.getNows().get(0).getId()))
                .with(NowDefinition::getReferenceDate, is(referenceDate))
        );



    }

}
