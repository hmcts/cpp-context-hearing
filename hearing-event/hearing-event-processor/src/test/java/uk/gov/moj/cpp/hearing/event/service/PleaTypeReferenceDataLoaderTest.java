package uk.gov.moj.cpp.hearing.event.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.Envelope;

import javax.json.JsonObject;
import java.util.Set;
import java.util.UUID;

import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.Envelope.envelopeFrom;
import static uk.gov.justice.services.messaging.Envelope.metadataBuilder;

@RunWith(MockitoJUnitRunner.class)
public class PleaTypeReferenceDataLoaderTest {
    private static final String FIELD_PLEA_STATUS_TYPES = "pleaStatusTypes";
    private static final String FIELD_PLEA_TYPE_GUILTY_FLAG = "pleaTypeGuiltyFlag";
    private static final String GUILTY_FLAG_YES = "Yes";
    private static final String GUILTY_FLAG_NO = "No";
    private static final String FIELD_PLEA_VALUE = "pleaValue";

    private static final String GUILTY = "GUILTY";
    private static final String NOT_GUILTY = "NOT_GUILTY";

    @Mock
    private Requester requester;

    @InjectMocks
    private PleaTypeReferenceDataLoader pleaTypeReferenceDataLoader;

    @Test
    public void shouldRetrieveGuiltyPleaTypes(){
        final Envelope envelope = envelopeFrom(metadataBuilder().withId(UUID.randomUUID()).withName("name").build(), buildPleaStatusTypesPayload());
        when(requester.requestAsAdmin(any(), eq(JsonObject.class))).thenReturn(envelope);
        Set<String> pleaTypes = pleaTypeReferenceDataLoader.retrieveGuiltyPleaTypes();
        assertThat(pleaTypes.size(), is(1));
        assertThat(pleaTypes.contains(GUILTY), is(true));
        assertThat(pleaTypes.contains(NOT_GUILTY), is(false));
    }

    private JsonObject buildPleaStatusTypesPayload(){
        return createObjectBuilder().add(FIELD_PLEA_STATUS_TYPES, createArrayBuilder()
                .add(createObjectBuilder().add(FIELD_PLEA_VALUE, GUILTY).add(FIELD_PLEA_TYPE_GUILTY_FLAG, GUILTY_FLAG_YES))
                .add(createObjectBuilder().add(FIELD_PLEA_VALUE, NOT_GUILTY).add(FIELD_PLEA_TYPE_GUILTY_FLAG, GUILTY_FLAG_NO)))
                .build();
    }
}
