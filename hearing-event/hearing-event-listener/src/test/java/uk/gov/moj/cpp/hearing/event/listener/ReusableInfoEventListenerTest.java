package uk.gov.moj.cpp.hearing.event.listener;

import static java.util.UUID.randomUUID;
import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;
import static org.mockito.Mockito.verify;
import static uk.gov.justice.services.messaging.Envelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithDefaults;
import static uk.gov.moj.cpp.hearing.domain.event.ReusableInfoSaved.reusableInfoSaved;

import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.moj.cpp.hearing.command.ReusableInfo;
import uk.gov.moj.cpp.hearing.command.ReusableInfoResults;
import uk.gov.moj.cpp.hearing.domain.event.ReusableInfoSaved;
import uk.gov.moj.cpp.hearing.repository.ReusableInfoRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ReusableInfoEventListenerTest {

    @Mock
    private ReusableInfoRepository reusableInfoRepository;

    @InjectMocks
    private ReusableInfoEventListener reusableInfoEventListener;

    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    private final ArgumentCaptor<uk.gov.moj.cpp.hearing.persist.entity.ha.ReusableInfo> notificationArgumentCaptor = ArgumentCaptor.forClass(uk.gov.moj.cpp.hearing.persist.entity.ha.ReusableInfo.class);


    @Test
    public void shouldSaveTheCache() {

        final UUID hearingId = randomUUID();
        final UUID defendantId = randomUUID();
        final UUID offenceId = randomUUID();

        final List<ReusableInfo> promptList = new ArrayList<>();
        final ReusableInfo prompt = ReusableInfo.builder()
                .withMasterDefendantId(defendantId)
                .withPromptRef("bailExceptionReason")
                .withType("TXT")
                .withValue("abcd")
                .withCacheable(1)
                .withCacheDataPath("path")
                .withOffenceId(offenceId)
                .build();
        promptList.add(prompt);

        final List<ReusableInfoResults> resultList = new ArrayList<>();
        final ReusableInfoResults result = ReusableInfoResults.builder()
                .withMasterDefendantId(defendantId)
                .withOffenceId(offenceId)
                .withShortCode("BAIC")
                .withValue("Curfew")
                .build();
        resultList.add(result);

        final ReusableInfoSaved reusableInfoSaved = reusableInfoSaved()
                .withResultsList(resultList)
                .withHearingId(hearingId)
                .withPromptList(promptList)
                .build();


        final JsonObjectBuilder cacheBuilder = createObjectBuilder();
        final JsonArrayBuilder promptArray = createArrayBuilder();
        promptArray.add(createPrompt("bailExceptionReason",defendantId,"TXT","abcd", offenceId));

        final JsonArrayBuilder baicList = createArrayBuilder();
        baicList.add(createResult(defendantId,"Curfew", offenceId));


        cacheBuilder.add("prompts",promptArray)
                .add("results",baicList)
                .build();

        final Envelope<ReusableInfoSaved> reusableInfoCachedEnvelope =
                envelopeFrom(metadataWithDefaults(), reusableInfoSaved);

        reusableInfoEventListener.saveReusableInfo(reusableInfoCachedEnvelope);

        verify(reusableInfoRepository).save(notificationArgumentCaptor.capture());

    }

    private JsonObject createPrompt(final String promptRef,final UUID masterDefendantId, final String type, final Object value, final UUID offenceId) {
        final JsonObjectBuilder prompt = createObjectBuilder();
        prompt.add("promptRef", promptRef);
        prompt.add("defendantId", masterDefendantId.toString());
        prompt.add("type", type);
        prompt.add("value", value.toString());
        prompt.add("offenceId", offenceId.toString());
        return prompt.build();
    }

    private JsonObject createResult(final UUID masterDefendantId, final String value, final UUID offenceId) {
        final JsonObjectBuilder baic = createObjectBuilder();
        baic.add("shortCode","BAIC");
        baic.add("defendantId", masterDefendantId.toString());
        final JsonArrayBuilder valueArray = createArrayBuilder();
        baic.add("value", valueArray.add(value));
        baic.add("offenceId", offenceId.toString());
        return baic.build();
    }
}