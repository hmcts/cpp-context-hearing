package uk.gov.moj.cpp.hearing.event.service;

import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static uk.gov.justice.hearing.courts.referencedata.HearingTypes.hearingTypes;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher.jsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.metadata;
import static uk.gov.moj.cpp.hearing.event.service.HearingTypeReverseLookup.GET_HEARING_TYPES_ID;

import uk.gov.justice.core.courts.HearingType;
import uk.gov.justice.hearing.courts.referencedata.HearingTypesResult;
import uk.gov.justice.services.messaging.JsonEnvelope;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class HearingTypeReverseLookupTest extends ReferenceDataClientTestBase {

    @InjectMocks
    private HearingTypeReverseLookup target;

    @Captor
    protected ArgumentCaptor<JsonEnvelope> envelopeCaptor;

    @Test
    public void hearingTypeByName() {
        final HearingTypesResult hearingTypesResult = HearingTypesResult.hearingTypesResult()
                .withHearingTypes(asList(
                        hearingTypes()
                                .withId(randomUUID().toString())
                                .withHearingDescription("abCdEf")
                                .build()
                ))
                .build();
        mockAdminQuery(envelopeCaptor, hearingTypesResult);

        HearingType hearingType = target.getHearingTypeByName(context, "abcdeF");

        assertEquals(hearingTypesResult.getHearingTypes().get(0).getId(), hearingType.getId().toString());

        assertThat(envelopeCaptor.getValue(), jsonEnvelope()
                .withMetadataOf(metadata().withName(GET_HEARING_TYPES_ID)));
    }
}
